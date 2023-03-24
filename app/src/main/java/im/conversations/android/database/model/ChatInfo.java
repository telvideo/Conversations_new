package im.conversations.android.database.model;

import org.jxmpp.jid.BareJid;
import org.jxmpp.jid.Jid;
import org.jxmpp.jid.impl.JidCreate;

public class ChatInfo implements IndividualName {

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

    private String mucName() {
        if (notNullNotEmpty(this.bookmarkName)) {
            return this.bookmarkName.trim();
        }
        if (notNullNotEmpty(this.discoIdentityName)) {
            return this.discoIdentityName.trim();
        }
        final var jid = getJidAddress();
        if (jid == null) {
            return this.address;
        } else if (jid.hasLocalpart()) {
            return jid.getLocalpartOrThrow().toString();
        } else {
            return jid.toString();
        }
    }

    @Override
    public String individualRosterName() {
        return this.rosterName;
    }

    @Override
    public String individualNick() {
        return nick;
    }

    @Override
    public BareJid individualAddress() {
        return address == null ? null : JidCreate.fromOrNull(address).asBareJid();
    }

    private static boolean notNullNotEmpty(final String value) {
        return value != null && !value.trim().isEmpty();
    }

    protected Jid getJidAddress() {
        return address == null ? null : JidCreate.fromOrNull(address);
    }
}
