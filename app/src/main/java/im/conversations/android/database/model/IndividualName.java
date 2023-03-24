package im.conversations.android.database.model;

import org.jxmpp.jid.BareJid;

public interface IndividualName {

    default String individualName() {
        final var rosterName = individualRosterName();
        if (notNullNotEmpty(rosterName)) {
            return rosterName.trim();
        }
        final var nick = individualNick();
        if (notNullNotEmpty(nick)) {
            return nick.trim();
        }
        final var address = individualAddress();
        if (address == null) {
            return null;
        } else if (address.hasLocalpart()) {
            return address.getLocalpartOrThrow().toString();
        } else {
            return address.toString();
        }
    }

    String individualRosterName();

    String individualNick();

    BareJid individualAddress();

    private static boolean notNullNotEmpty(final String value) {
        return value != null && !value.trim().isEmpty();
    }
}
