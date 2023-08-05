package io.github.jwdeveloper.tiktok;

import io.github.jwdeveloper.tiktok.http.TikTokApiService;
import io.github.jwdeveloper.tiktok.http.TikTokHttpApiClient;
import io.github.jwdeveloper.tiktok.http.TikTokHttpRequestFactory;
import io.github.jwdeveloper.tiktok.live.LiveClient;
import io.github.jwdeveloper.tiktok.live.TikTokLiveMeta;
import io.github.jwdeveloper.tiktok.websocket.TikTokWebsocketClient;

import java.time.Duration;
import java.util.Map;
import java.util.function.Consumer;
import java.util.logging.Logger;

public class TikTokClientBuilder {
    private String userName;
    private final ClientSettings clientSettings;
    private Map<String, Object> clientParameters;
    private Logger logger;

    public TikTokClientBuilder(String userName) {
        this.userName = userName;
        this.clientSettings = Constants.DefaultClientSettings();
        this.clientParameters = Constants.DefaultClientParams();
        this.logger = Logger.getLogger(TikTokLive.class.getName());
    }

    public TikTokClientBuilder clientSettings(Consumer<ClientSettings> consumer) {
        consumer.accept(clientSettings);
        return this;
    }

    public TikTokClientBuilder hostUserName(String userName) {
        this.userName = userName;
        return this;
    }

    public TikTokClientBuilder clientParameters(Map<String, Object> clientParameters) {
        this.clientParameters = clientParameters;
        return this;
    }

    public TikTokClientBuilder addClientParameters(String key, Object value) {
        this.clientParameters.put(key, value);
        return this;
    }

    private void validate() {

        if (clientSettings.getTimeout() == null) {
            clientSettings.setTimeout(Duration.ofSeconds(Constants.DEFAULT_TIMEOUT));
        }

        if (clientSettings.getPollingInterval() == null) {
            clientSettings.setPollingInterval(Duration.ofSeconds(Constants.DEFAULT_POLLTIME));
        }

        if (clientSettings.getClientLanguage() == null || clientSettings.getClientLanguage().equals("")) {
            clientSettings.setClientLanguage(Constants.DefaultClientSettings().getClientLanguage());
        }

        if (clientSettings.getSocketBufferSize() < 500_000) {
            clientSettings.setSocketBufferSize(Constants.DefaultClientSettings().getSocketBufferSize());
        }


        if (userName == null || userName.equals("")) {
            throw new RuntimeException("UserName can not be null");
        }

        if (clientParameters == null) {
            clientParameters = Constants.DefaultClientParams();
        }

        clientParameters.put("app_language", clientSettings.getClientLanguage());
        clientParameters.put("webcast_language", clientSettings.getClientLanguage());
    }


    public LiveClient build() {
        validate();


        var meta = new TikTokLiveMeta();
        meta.setUserName(userName);


        var requestFactory = new TikTokHttpRequestFactory();
        var apiClient = new TikTokHttpApiClient(clientSettings, requestFactory);
        var apiService = new TikTokApiService(apiClient, logger,clientParameters);
        var webSocketClient = new TikTokWebsocketClient(logger,clientParameters, clientSettings);
        var giftManager =new TikTokGiftManager(logger, apiService, clientSettings);
        return new TikTokLiveClient(meta,apiService, webSocketClient, giftManager, logger);
    }
}
