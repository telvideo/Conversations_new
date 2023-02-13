package im.conversations.android.transformer;

import com.google.common.base.Preconditions;
import com.google.common.base.Strings;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Iterables;
import eu.siacs.conversations.xmpp.InvalidJid;
import im.conversations.android.database.ConversationsDatabase;
import im.conversations.android.database.model.Account;
import im.conversations.android.database.model.ChatIdentifier;
import im.conversations.android.database.model.MessageContent;
import im.conversations.android.database.model.MessageIdentifier;
import im.conversations.android.database.model.MessageState;
import im.conversations.android.database.model.Modification;
import im.conversations.android.xmpp.model.DeliveryReceipt;
import im.conversations.android.xmpp.model.axolotl.Encrypted;
import im.conversations.android.xmpp.model.correction.Replace;
import im.conversations.android.xmpp.model.jabber.Body;
import im.conversations.android.xmpp.model.markers.Displayed;
import im.conversations.android.xmpp.model.muc.user.MultiUserChat;
import im.conversations.android.xmpp.model.oob.OutOfBandData;
import im.conversations.android.xmpp.model.reactions.Reactions;
import im.conversations.android.xmpp.model.reply.Reply;
import im.conversations.android.xmpp.model.retract.Retract;
import im.conversations.android.xmpp.model.stanza.Message;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Objects;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Transformer {

    private static final Logger LOGGER = LoggerFactory.getLogger(Transformer.class);

    private final ConversationsDatabase database;
    private final Account account;

    public Transformer(final ConversationsDatabase database, final Account account) {
        Preconditions.checkArgument(account != null, "Account must not be null");
        this.database = database;
        this.account = account;
    }

    public boolean transform(final Transformation transformation) {
        return database.runInTransaction(() -> transform(database, transformation));
    }

    /**
     * @param transformation
     * @return returns true if there is something we want to send a delivery receipt for. Basically
     *     anything that created a new message in the database. Notably not something that only
     *     updated a status somewhere
     */
    private boolean transform(
            final ConversationsDatabase database, final Transformation transformation) {
        final var remote = transformation.remote;
        final var messageType = transformation.type;
        final var muc = transformation.getExtension(MultiUserChat.class);

        final ChatIdentifier chat =
                database.chatDao()
                        .getOrCreateChat(account, remote, messageType, Objects.nonNull(muc));

        if (messageType == Message.Type.ERROR) {
            if (transformation.outgoing()) {
                LOGGER.info("Ignoring outgoing error to {}", transformation.to);
                return false;
            }
            database.messageDao()
                    .insertMessageState(
                            chat, transformation.messageId, MessageState.error(transformation));
            return false;
        }
        final Replace messageCorrection = transformation.getExtension(Replace.class);
        final Reactions reactions = transformation.getExtension(Reactions.class);
        final Retract retract = transformation.getExtension(Retract.class);
        // TODO we need to remove fallbacks for reactions, retractions and potentially other things
        final List<MessageContent> contents = parseContent(transformation);

        final boolean identifiableSender =
                Arrays.asList(Message.Type.NORMAL, Message.Type.CHAT).contains(messageType)
                        || Objects.nonNull(transformation.occupantId);
        final boolean isReaction =
                Objects.nonNull(reactions)
                        && Objects.nonNull(reactions.getId())
                        && identifiableSender;
        final boolean isMessageCorrection =
                Objects.nonNull(messageCorrection)
                        && Objects.nonNull(messageCorrection.getId())
                        && identifiableSender;
        final boolean isRetraction =
                Objects.nonNull(retract) && Objects.nonNull(retract.getId()) && identifiableSender;
        // TODO in a way it would be more appropriate to move this into the contents.isEmpty block
        // but for that to work we would need to properly ignore the fallback body
        if (isRetraction) {
            final var messageIdentifier =
                    database.messageDao()
                            .getOrCreateVersion(
                                    chat, transformation, retract.getId(), Modification.RETRACTION);
            database.messageDao()
                    .insertMessageContent(messageIdentifier.version, MessageContent.RETRACTION);
            return true;
        } else if (contents.isEmpty()) {
            LOGGER.info("Received message from {} w/o contents", transformation.from);
            transformMessageState(chat, transformation);
            if (isReaction) {
                database.messageDao().insertReactions(chat, reactions, transformation);
            }
        } else {
            final MessageIdentifier messageIdentifier;
            try {
                if (isMessageCorrection) {
                    messageIdentifier =
                            database.messageDao()
                                    .getOrCreateVersion(
                                            chat,
                                            transformation,
                                            messageCorrection.getId(),
                                            Modification.CORRECTION);

                } else {
                    messageIdentifier =
                            database.messageDao().getOrCreateMessage(chat, transformation);
                }
            } catch (final IllegalStateException e) {
                LOGGER.warn("Could not get message identifier", e);
                return false;
            }
            database.messageDao().insertMessageContent(messageIdentifier.version, contents);
            final var reply = transformation.getExtension(Reply.class);
            if (Objects.nonNull(reply)
                    && Objects.nonNull(reply.getId())
                    && InvalidJid.isValid(reply.getTo())) {
                database.messageDao()
                        .setInReplyTo(
                                chat, messageIdentifier, messageType, reply.getTo(), reply.getId());
            }
            return true;
        }
        return true;
    }

    protected List<MessageContent> parseContent(final Transformation transformation) {
        final var encrypted = transformation.getExtension(Encrypted.class);
        final var encryptedWithPayload = encrypted != null && encrypted.hasPayload();
        final Collection<Body> bodies = transformation.getExtensions(Body.class);
        final Collection<OutOfBandData> outOfBandData =
                transformation.getExtensions(OutOfBandData.class);
        final ImmutableList.Builder<MessageContent> messageContentBuilder = ImmutableList.builder();

        // TODO decrypt

        if (bodies.size() == 1 && outOfBandData.size() == 1) {
            final String text = Iterables.getOnlyElement(bodies).getContent();
            final String url = Iterables.getOnlyElement(outOfBandData).getURL();
            if (!Strings.isNullOrEmpty(url) && url.equals(text)) {
                return ImmutableList.of(MessageContent.file(url));
            }
        }

        // TODO verify that body is not fallback
        for (final Body body : bodies) {
            final String text = body.getContent();
            if (Strings.isNullOrEmpty(text)) {
                continue;
            }
            messageContentBuilder.add(MessageContent.text(text, body.getLang()));
        }
        for (final OutOfBandData data : outOfBandData) {
            final String url = data.getURL();
            if (Strings.isNullOrEmpty(url)) {
                continue;
            }
            messageContentBuilder.add(MessageContent.file(url));
        }
        return messageContentBuilder.build();
    }

    private void transformMessageState(
            final ChatIdentifier chat, final Transformation transformation) {
        final var displayed = transformation.getExtension(Displayed.class);
        if (displayed != null) {
            if (transformation.outgoing()) {
                LOGGER.info(
                        "Received outgoing displayed marker for chat with {}",
                        transformation.remote);
                return;
            }
            database.messageDao()
                    .insertMessageState(
                            chat, displayed.getId(), MessageState.displayed(transformation));
        }
        final var deliveryReceipt = transformation.getExtension(DeliveryReceipt.class);
        if (deliveryReceipt != null) {
            if (transformation.outgoing()) {
                LOGGER.info("Ignoring outgoing delivery receipt to {}", transformation.to);
                return;
            }
            database.messageDao()
                    .insertMessageState(
                            chat, deliveryReceipt.getId(), MessageState.delivered(transformation));
        }
    }
}
