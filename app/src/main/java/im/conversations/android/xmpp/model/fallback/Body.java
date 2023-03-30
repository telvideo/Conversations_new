package im.conversations.android.xmpp.model.fallback;

import im.conversations.android.annotation.XmlElement;
import im.conversations.android.xmpp.model.Extension;

@XmlElement
public class Body extends Extension {

    public Body() {
        super(Body.class);
    }

    public int getStart() {
        return this.getOptionalIntAttribute("start").or(0);
    }

    public int getEnd() {
        return this.getOptionalIntAttribute("end").or(0);
    }
}
