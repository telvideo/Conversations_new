package im.conversations.android.database.model;

import androidx.room.Relation;
import com.google.common.base.Objects;
import com.google.common.collect.Iterables;
import im.conversations.android.database.entity.MessageContentEntity;
import java.time.Instant;
import java.util.List;
import org.jxmpp.jid.Jid;

public class ChatOverviewItem extends ChatInfo {

    public long id;

    public Instant sentAt;

    public boolean outgoing;

    public Jid toBare;
    public String toResource;
    public Jid fromBare;
    public String fromResource;
    public long version;

    public String vCardPhoto;
    public String avatar;

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

    public Sender getSender() {
        if (outgoing) {
            return new SenderYou();
        } else if (type == ChatType.MUC) {
            if (fromResource != null) {
                return new SenderName(fromResource);
            } else {
                return null;
            }
        } else {
            return null;
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
                && accountId == that.accountId
                && outgoing == that.outgoing
                && version == that.version
                && unread == that.unread
                && Objects.equal(address, that.address)
                && type == that.type
                && Objects.equal(sentAt, that.sentAt)
                && Objects.equal(toBare, that.toBare)
                && Objects.equal(toResource, that.toResource)
                && Objects.equal(fromBare, that.fromBare)
                && Objects.equal(fromResource, that.fromResource)
                && Objects.equal(rosterName, that.rosterName)
                && Objects.equal(nick, that.nick)
                && Objects.equal(discoIdentityName, that.discoIdentityName)
                && Objects.equal(bookmarkName, that.bookmarkName)
                && Objects.equal(vCardPhoto, that.vCardPhoto)
                && Objects.equal(avatar, that.avatar)
                && Objects.equal(contents, that.contents);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(
                id,
                accountId,
                address,
                type,
                sentAt,
                outgoing,
                toBare,
                toResource,
                fromBare,
                fromResource,
                version,
                rosterName,
                nick,
                discoIdentityName,
                bookmarkName,
                vCardPhoto,
                avatar,
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
