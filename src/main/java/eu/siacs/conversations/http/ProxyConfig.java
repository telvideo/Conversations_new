package eu.siacs.conversations.http;

public class ProxyConfig {
    public final boolean isSocks;
    public final String host;
    public final int port;

    public ProxyConfig(boolean isSocks, String host, int port) {
        this.isSocks = isSocks;
        this.host = host;
        this.port = port;
    }
}
