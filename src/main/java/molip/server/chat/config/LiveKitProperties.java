package molip.server.chat.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Component
@ConfigurationProperties(prefix = "livekit")
public class LiveKitProperties {

    private String url;
    private String apiKey;
    private String apiSecret;
    private String webhookSecret;
    private String stunUrls;
    private String turnUrls;
    private String turnUsername;
    private String turnPassword;
    private long tokenTtlSeconds = 1800L;

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public String getApiKey() {
        return apiKey;
    }

    public void setApiKey(String apiKey) {
        this.apiKey = apiKey;
    }

    public String getApiSecret() {
        return apiSecret;
    }

    public void setApiSecret(String apiSecret) {
        this.apiSecret = apiSecret;
    }

    public String getWebhookSecret() {
        return webhookSecret;
    }

    public void setWebhookSecret(String webhookSecret) {
        this.webhookSecret = webhookSecret;
    }

    public String getStunUrls() {
        return stunUrls;
    }

    public void setStunUrls(String stunUrls) {
        this.stunUrls = stunUrls;
    }

    public String getTurnUrls() {
        return turnUrls;
    }

    public void setTurnUrls(String turnUrls) {
        this.turnUrls = turnUrls;
    }

    public String getTurnUsername() {
        return turnUsername;
    }

    public void setTurnUsername(String turnUsername) {
        this.turnUsername = turnUsername;
    }

    public String getTurnPassword() {
        return turnPassword;
    }

    public void setTurnPassword(String turnPassword) {
        this.turnPassword = turnPassword;
    }

    public long getTokenTtlSeconds() {
        return tokenTtlSeconds;
    }

    public void setTokenTtlSeconds(long tokenTtlSeconds) {
        this.tokenTtlSeconds = tokenTtlSeconds;
    }
}
