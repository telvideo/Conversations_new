package im.conversations.android.database;

import im.conversations.android.database.model.ChatType;
import java.util.Arrays;
import org.jxmpp.jid.BareJid;

public interface KnownSender {

    default boolean isKnownSender() {
        final var chatType = getChatType();
        final var membersOnlyNonAnonymous = isMembersOnlyNonAnonymous();
        final var sender = getSender();
        return chatType == ChatType.INDIVIDUAL
                || (Arrays.asList(ChatType.MUC, ChatType.MUC_PM).contains(chatType)
                        && membersOnlyNonAnonymous
                        && sender != null);
    }

    ChatType getChatType();

    boolean isMembersOnlyNonAnonymous();

    BareJid getSender();
}
