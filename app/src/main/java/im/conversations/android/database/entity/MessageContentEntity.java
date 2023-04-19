package im.conversations.android.database.entity;

import androidx.annotation.NonNull;
import androidx.room.Entity;
import androidx.room.ForeignKey;
import androidx.room.Index;
import androidx.room.PrimaryKey;
import com.google.common.net.MediaType;
import im.conversations.android.database.model.MessageContent;
import im.conversations.android.database.model.PartType;

@Entity(
        tableName = "message_content",
        foreignKeys =
                @ForeignKey(
                        entity = MessageVersionEntity.class,
                        parentColumns = {"id"},
                        childColumns = {"messageVersionId"},
                        onDelete = ForeignKey.CASCADE),
        indices = {@Index(value = "messageVersionId")})
public class MessageContentEntity {

    @PrimaryKey(autoGenerate = true)
    public Long id;

    @NonNull public Long messageVersionId;

    public String language;

    public PartType partType;
    public MediaType mediaType;
    public Long size;

    public String body;

    public String url;

    public boolean cached;

    public static MessageContentEntity of(
            final long messageVersionId, final MessageContent content) {
        final var entity = new MessageContentEntity();
        entity.messageVersionId = messageVersionId;
        entity.language = content.language;
        entity.partType = content.partType;
        entity.mediaType = content.mediaType;
        entity.size = content.size;
        entity.body = content.body;
        entity.url = content.url;
        entity.cached = content.cached;
        return entity;
    }
}
