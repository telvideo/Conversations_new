package im.conversations.android.database.model;

import androidx.room.Relation;
import com.google.common.base.Objects;
import com.google.common.collect.Iterables;
import im.conversations.android.database.KnownSender;
import im.conversations.android.database.entity.MessageContentEntity;
import java.time.Instant;
import java.util.List;
import org.jxmpp.jid.BareJid;
import org.jxmpp.jid.Jid;
import org.jxmpp.jid.parts.Resourcepart;

public class ChatOverviewItem extends ChatInfo implements KnownSender {

    public long id;

    public Instant sentAt;

    public boolean outgoing;

    public Jid toBare;
    public String toResource;
    public Jid fromBare;
    public String fromResource;

    public BareJid sender;
    public String senderNick;
    public String senderRosterName;

    public long version;

    public String vCardPhoto;
    public String avatar;

    public Resourcepart occupantResource;

    public int unread;

    @Relation(
            entity = MessageContentEntity.class,
            parentColumn = "version",
            entityColumn = "messageVersionId")
    public List<MessageContent> contents;

    public String message() {
        final var firstMessageContent = Iterables.getFirst(contents, null);
        return firstMessageContent == null ? null : firstMessageContent.body;
    }

    @Override
    public ChatType getChatType() {
        return this.type;
    }

    @Override
    public boolean isMembersOnlyNonAnonymous() {
        return this.membersOnlyNonAnonymous;
    }

    @Override
    public BareJid getSender() {
        return this.sender;
    }

    public Sender getMessageSender() {
        if (outgoing) {
            return new SenderYou();
        } else if (type == ChatType.MUC) {
            if (isKnownSender()) {
                return new SenderName(individualName());
            } else {
                if (occupantResource != null && occupantResource.length() > 0) {
                    return new SenderName(occupantResource.toString());
                }
                if (fromResource != null && fromResource.length() > 0) {
                    return new SenderName(fromResource.toString());
                }
            }
        }
        return null;
    }

    @Override
    public String individualRosterName() {
        if (type == ChatType.INDIVIDUAL) {
            return super.individualRosterName();
        } else {
            return this.senderRosterName;
        }
    }

    @Override
    public String individualNick() {
        if (type == ChatType.INDIVIDUAL) {
            return super.individualNick();
        } else {
            return senderNick;
        }
    }

    @Override
    public BareJid individualAddress() {
        if (type == ChatType.INDIVIDUAL) {
            return super.individualAddress();
        } else {
            return this.sender;
        }
    }

    public AddressWithName getAddressWithName() {
        final Jid address = getJidAddress();
        final String name = name();
        if (address == null || name == null) {
            return null;
        }
        return new AddressWithName(address, name);
    }

    public AvatarWithAccount getAvatar() {
        final var address = getAddressWithName();
        if (address == null) {
            return null;
        }
        if (this.avatar != null) {
            return new AvatarWithAccount(accountId, address, AvatarType.PEP, this.avatar);
        }
        if (this.vCardPhoto != null) {
            return new AvatarWithAccount(accountId, address, AvatarType.VCARD, this.vCardPhoto);
        }
        return null;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ChatOverviewItem that = (ChatOverviewItem) o;
        return id == that.id
                && outgoing == that.outgoing
                && version == that.version
                && unread == that.unread
                && Objects.equal(sentAt, that.sentAt)
                && Objects.equal(toBare, that.toBare)
                && Objects.equal(toResource, that.toResource)
                && Objects.equal(fromBare, that.fromBare)
                && Objects.equal(fromResource, that.fromResource)
                && Objects.equal(sender, that.sender)
                && Objects.equal(senderNick, that.senderNick)
                && Objects.equal(senderRosterName, that.senderRosterName)
                && Objects.equal(vCardPhoto, that.vCardPhoto)
                && Objects.equal(avatar, that.avatar)
                && Objects.equal(occupantResource, that.occupantResource)
                && Objects.equal(contents, that.contents);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(
                id,
                sentAt,
                outgoing,
                toBare,
                toResource,
                fromBare,
                fromResource,
                sender,
                senderNick,
                senderRosterName,
                version,
                vCardPhoto,
                avatar,
                occupantResource,
                unread,
                contents);
    }

    public sealed interface Sender permits SenderYou, SenderName {}

    public static final class SenderYou implements Sender {}

    public static final class SenderName implements Sender {
        public final String name;

        public SenderName(String name) {
            this.name = name;
        }
    }
}
