package im.conversations.android.xmpp.model.sasl2;

import eu.siacs.conversations.crypto.sasl.SaslMechanism;
import eu.siacs.conversations.xmpp.XmppConnection;
import im.conversations.android.annotation.XmlElement;
import im.conversations.android.xmpp.model.AuthenticationRequest;
import im.conversations.android.xmpp.model.StreamElement;

@XmlElement
public class Authenticate extends AuthenticationRequest {

    public Authenticate() {
        super(Authenticate.class);
    }

    @Override
    public void setMechanism(final SaslMechanism mechanism) {
        this.setAttribute("mechanism", mechanism.getMechanism());
    }
}
