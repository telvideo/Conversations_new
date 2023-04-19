package im.conversations.android.database.model;

import com.google.common.base.Objects;
import com.google.common.net.MediaType;

public class MessageContent {

    public final String language;

    public final PartType partType;
    public MediaType mediaType;
    public Long size;

    public final String body;

    public final String url;

    public boolean cached;

    public MessageContent(
            String language,
            PartType partType,
            MediaType mediaType,
            Long size,
            String body,
            String url,
            boolean cached) {
        this.language = language;
        this.partType = partType;
        this.mediaType = mediaType;
        this.size = size;
        this.body = body;
        this.url = url;
        this.cached = cached;
    }

    public static MessageContent text(final String body, final String language) {
        return new MessageContent(language, PartType.TEXT, null, null, body, null, false);
    }

    public static MessageContent file(final String url) {
        return new MessageContent(null, PartType.FILE, null, null, null, url, false);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        MessageContent that = (MessageContent) o;
        return cached == that.cached
                && Objects.equal(language, that.language)
                && partType == that.partType
                && Objects.equal(mediaType, that.mediaType)
                && Objects.equal(size, that.size)
                && Objects.equal(body, that.body)
                && Objects.equal(url, that.url);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(language, partType, mediaType, size, body, url, cached);
    }
}
