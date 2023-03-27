package im.conversations.android.database.model;

import com.google.common.base.Objects;
import org.jxmpp.jid.Jid;

public class MessageReaction {

    public final Jid reactionBy;
    public final String reactionByResource;
    public final String occupantId;

    public final String reaction;

    public MessageReaction(
            Jid reactionBy, String reactionByResource, String occupantId, String reaction) {
        this.reactionBy = reactionBy;
        this.reactionByResource = reactionByResource;
        this.occupantId = occupantId;
        this.reaction = reaction;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        MessageReaction that = (MessageReaction) o;
        return Objects.equal(reactionBy, that.reactionBy)
                && Objects.equal(reactionByResource, that.reactionByResource)
                && Objects.equal(occupantId, that.occupantId)
                && Objects.equal(reaction, that.reaction);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(reactionBy, reactionByResource, occupantId, reaction);
    }
}
