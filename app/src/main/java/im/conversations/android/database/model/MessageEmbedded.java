package im.conversations.android.database.model;

import androidx.room.Relation;
import com.google.common.base.Objects;
import im.conversations.android.database.entity.MessageContentEntity;
import java.time.Instant;
import java.util.List;
import org.jxmpp.jid.Jid;

public class MessageEmbedded {

    public long id;
    public Jid fromBare;
    public String fromResource;
    public Instant sentAt;

    public Long latestVersion;

    @Relation(
            entity = MessageContentEntity.class,
            parentColumn = "latestVersion",
            entityColumn = "messageVersionId")
    public List<MessageContent> contents;

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        MessageEmbedded that = (MessageEmbedded) o;
        return id == that.id
                && Objects.equal(fromBare, that.fromBare)
                && Objects.equal(fromResource, that.fromResource)
                && Objects.equal(sentAt, that.sentAt)
                && Objects.equal(latestVersion, that.latestVersion)
                && Objects.equal(contents, that.contents);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(id, fromBare, fromResource, sentAt, latestVersion, contents);
    }
}
