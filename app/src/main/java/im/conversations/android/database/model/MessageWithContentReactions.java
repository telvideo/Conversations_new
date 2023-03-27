package im.conversations.android.database.model;

import androidx.room.Relation;
import com.google.common.base.Objects;
import com.google.common.base.Strings;
import com.google.common.collect.Collections2;
import com.google.common.collect.ImmutableSortedSet;
import com.google.common.collect.Iterables;
import com.google.common.collect.Maps;
import com.google.common.collect.Multimaps;
import im.conversations.android.database.KnownSender;
import im.conversations.android.database.entity.MessageContentEntity;
import im.conversations.android.database.entity.MessageEntity;
import im.conversations.android.database.entity.MessageReactionEntity;
import im.conversations.android.database.entity.MessageStateEntity;
import java.time.Instant;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.jxmpp.jid.BareJid;
import org.jxmpp.jid.Jid;
import org.jxmpp.jid.impl.JidCreate;
import org.jxmpp.jid.parts.Resourcepart;
import org.whispersystems.libsignal.IdentityKey;

public final class MessageWithContentReactions
        implements IndividualName, KnownSender, MessageAdapterItem {

    public long accountId;

    public long id;

    public ChatType chatType;
    public boolean membersOnlyNonAnonymous;

    public Instant sentAt;

    public boolean outgoing;

    public BareJid toBare;
    public String toResource;
    public BareJid fromBare;
    public Resourcepart fromResource;

    public BareJid sender;
    public String senderVcardPhoto;
    public String senderAvatar;
    public String senderRosterName;
    public String senderNick;

    public String occupantVcardPhoto;
    public Resourcepart occupantResource;

    public Modification modification;
    public long version;
    public boolean acknowledged;
    public Long inReplyToMessageEntityId;
    public Encryption encryption;
    public IdentityKey identityKey;
    public Trust trust;

    @Relation(
            entity = MessageEntity.class,
            parentColumn = "inReplyToMessageEntityId",
            entityColumn = "id")
    public MessageEmbedded inReplyTo;

    @Relation(
            entity = MessageContentEntity.class,
            parentColumn = "version",
            entityColumn = "messageVersionId")
    public List<MessageContent> contents;

    @Relation(
            entity = MessageReactionEntity.class,
            parentColumn = "id",
            entityColumn = "messageEntityId")
    public List<MessageReaction> reactions;

    @Relation(
            entity = MessageStateEntity.class,
            parentColumn = "version",
            entityColumn = "messageVersionId")
    public List<MessageState> states;

    public Set<Map.Entry<String, Integer>> getAggregatedReactions() {
        final Map<String, Integer> aggregatedReactions =
                Maps.transformValues(
                        Multimaps.index(reactions, r -> r.reaction).asMap(), Collection::size);
        return ImmutableSortedSet.copyOf(
                (a, b) -> Integer.compare(b.getValue(), a.getValue()),
                aggregatedReactions.entrySet());
    }

    public String textContent() {
        final var content = Iterables.getFirst(this.contents, null);
        final var text = Strings.nullToEmpty(content == null ? null : content.body);
        return text;
        // return text.substring(0,Math.min(text.length(),20));
    }

    public AddressWithName getAddressWithName() {
        if (isKnownSender()) {
            return new AddressWithName(individualAddress(), individualName());
        } else {
            final Jid address;
            if (occupantResource != null && occupantResource.length() != 0) {
                address = JidCreate.fullFrom(fromBare, occupantResource);
            } else {
                address = JidCreate.fullFrom(fromBare, fromResource);
            }
            return new AddressWithName(address, address.getResourceOrEmpty().toString());
        }
    }

    public AvatarWithAccount getAvatar() {
        final var address = getAddressWithName();
        if (address == null) {
            return null;
        }
        if (isKnownSender()) {
            if (this.senderAvatar != null) {
                return new AvatarWithAccount(accountId, address, AvatarType.PEP, this.senderAvatar);
            }
            if (this.senderVcardPhoto != null) {
                return new AvatarWithAccount(
                        accountId, address, AvatarType.VCARD, this.senderVcardPhoto);
            }
        } else if (occupantVcardPhoto != null) {
            return new AvatarWithAccount(
                    accountId, address, AvatarType.VCARD, this.occupantVcardPhoto);
        }

        return null;
    }

    @Override
    public String individualRosterName() {
        return senderRosterName;
    }

    @Override
    public String individualNick() {
        return senderNick;
    }

    @Override
    public BareJid individualAddress() {
        return sender;
    }

    @Override
    public ChatType getChatType() {
        return this.chatType;
    }

    @Override
    public boolean isMembersOnlyNonAnonymous() {
        return membersOnlyNonAnonymous;
    }

    @Override
    public BareJid getSender() {
        return this.sender;
    }

    public String getSenderName() {
        final var addressWithName = getAddressWithName();
        return addressWithName.name;
    }

    public boolean isGroupChat() {
        return Arrays.asList(ChatType.MUC, ChatType.MULTICAST).contains(this.chatType);
    }

    public EncryptionTuple getEncryption() {
        return new EncryptionTuple(this.encryption, this.trust);
    }

    public State getState() {
        if (outgoing) {
            final var status =
                    Collections2.transform(
                            Collections2.filter(
                                    this.states, s -> s != null && s.fromBare.equals(toBare)),
                            s -> s.type);
            if (status.contains(StateType.DISPLAYED)) {
                return State.READ;
            } else if (status.contains(StateType.DELIVERED)) {
                return State.DELIVERED;
            } else if (acknowledged) {
                return State.DELIVERED_TO_SERVER;
            } else {
                return State.NONE;
            }
        } else {
            return State.NONE;
        }
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        MessageWithContentReactions that = (MessageWithContentReactions) o;
        return accountId == that.accountId
                && id == that.id
                && membersOnlyNonAnonymous == that.membersOnlyNonAnonymous
                && outgoing == that.outgoing
                && version == that.version
                && chatType == that.chatType
                && Objects.equal(sentAt, that.sentAt)
                && Objects.equal(toBare, that.toBare)
                && Objects.equal(toResource, that.toResource)
                && Objects.equal(fromBare, that.fromBare)
                && Objects.equal(fromResource, that.fromResource)
                && Objects.equal(sender, that.sender)
                && Objects.equal(senderVcardPhoto, that.senderVcardPhoto)
                && Objects.equal(senderAvatar, that.senderAvatar)
                && Objects.equal(senderRosterName, that.senderRosterName)
                && Objects.equal(senderNick, that.senderNick)
                && Objects.equal(occupantVcardPhoto, that.occupantVcardPhoto)
                && Objects.equal(occupantResource, that.occupantResource)
                && modification == that.modification
                && Objects.equal(inReplyToMessageEntityId, that.inReplyToMessageEntityId)
                && encryption == that.encryption
                && Objects.equal(identityKey, that.identityKey)
                && trust == that.trust
                && Objects.equal(inReplyTo, that.inReplyTo)
                && Objects.equal(contents, that.contents)
                && Objects.equal(reactions, that.reactions)
                && Objects.equal(states, that.states);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(
                accountId,
                id,
                chatType,
                membersOnlyNonAnonymous,
                sentAt,
                outgoing,
                toBare,
                toResource,
                fromBare,
                fromResource,
                sender,
                senderVcardPhoto,
                senderAvatar,
                senderRosterName,
                senderNick,
                occupantVcardPhoto,
                occupantResource,
                modification,
                version,
                inReplyToMessageEntityId,
                encryption,
                identityKey,
                trust,
                inReplyTo,
                contents,
                reactions,
                states);
    }

    public static class EncryptionTuple {
        public final Encryption encryption;
        public final Trust trust;

        public EncryptionTuple(Encryption encryption, Trust trust) {
            this.encryption = encryption;
            this.trust = trust;
        }
    }

    public enum State {
        NONE,
        DELIVERED_TO_SERVER,
        DELIVERED,
        READ,
        ERROR
    }
}
