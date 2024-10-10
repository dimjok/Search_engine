package searchengine.config;

import lombok.*;
import org.jsoup.Jsoup;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.io.IOException;

@ConfigurationProperties(prefix = "config.connection-config", ignoreUnknownFields = false)
@Getter
@Setter
@Component
public class ConnectionConfig {

    private int timeout;

    private String userAgent;

    private boolean ignoreContentType;

    private boolean followRedirects;

    private boolean ignoreHttpErrors;

    private String referrer;

    public org.jsoup.Connection.Response connect(String url) throws IOException {
        org.jsoup.Connection.Response response = Jsoup.connect(url)
                .ignoreHttpErrors(this.ignoreHttpErrors).timeout(this.timeout).ignoreContentType(this.ignoreContentType)
                .followRedirects(this.followRedirects)
                .userAgent(this.userAgent)
                .referrer(this.referrer)
                .execute();
        return response;
    }
}
