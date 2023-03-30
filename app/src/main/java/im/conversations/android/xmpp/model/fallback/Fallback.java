package im.conversations.android.xmpp.model.fallback;

import im.conversations.android.annotation.XmlElement;
import im.conversations.android.xmpp.model.Extension;

@XmlElement
public class Fallback extends Extension {

    public Fallback() {
        super(Fallback.class);
    }

    public Body getBody() {
        return this.getExtension(Body.class);
    }

    public String getFor() {
        return this.getAttribute("for");
    }
}
