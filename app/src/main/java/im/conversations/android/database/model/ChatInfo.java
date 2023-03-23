package im.conversations.android.database.model;

import org.jxmpp.jid.Jid;
import org.jxmpp.jid.impl.JidCreate;

public class ChatInfo {

    public long accountId;
    public String address;
    public ChatType type;

    public String rosterName;
    public String nick;
    public String discoIdentityName;
    public String bookmarkName;

    public boolean membersOnlyNonAnonymous;

    public String name() {
        return switch (type) {
            case MUC -> mucName();
            case INDIVIDUAL -> individualName();
            default -> address;
        };
    }

    private String individualName() {
        if (notNullNotEmpty(rosterName)) {
            return rosterName.trim();
        }
        if (notNullNotEmpty(nick)) {
            return nick.trim();
        }
        return fallbackName();
    }

    private String fallbackName() {
        final Jid jid = getJidAddress();
        if (jid == null) {
            return this.address;
        }
        if (jid.hasLocalpart()) {
            return jid.getLocalpartOrThrow().toString();
        } else {
            return jid.toString();
        }
    }

    private String mucName() {
        if (notNullNotEmpty(this.bookmarkName)) {
            return this.bookmarkName.trim();
        }
        if (notNullNotEmpty(this.discoIdentityName)) {
            return this.discoIdentityName.trim();
        }
        return fallbackName();
    }

    private static boolean notNullNotEmpty(final String value) {
        return value != null && !value.trim().isEmpty();
    }

    protected Jid getJidAddress() {
        return address == null ? null : JidCreate.fromOrNull(address);
    }
}
