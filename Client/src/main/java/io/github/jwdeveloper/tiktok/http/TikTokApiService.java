package io.github.jwdeveloper.tiktok.http;

import com.google.gson.Gson;
import io.github.jwdeveloper.tiktok.ClientSettings;
import io.github.jwdeveloper.tiktok.exceptions.TikTokLiveException;
import io.github.jwdeveloper.tiktok.exceptions.TikTokLiveOfflineHostException;
import io.github.jwdeveloper.tiktok.exceptions.TikTokLiveRequestException;
import io.github.jwdeveloper.tiktok.live.LiveRoomMeta;
import io.github.jwdeveloper.tiktok.mappers.LiveRoomMetaMapper;
import io.github.jwdeveloper.tiktok.messages.WebcastResponse;
import io.github.jwdeveloper.tiktok.models.gifts.TikTokGiftInfo;

import java.util.HashMap;
import java.util.Map;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class TikTokApiService {
    private final TikTokHttpClient tiktokHttpClient;
    private final Logger logger;
    private final ClientSettings clientSettings;

    public TikTokApiService(TikTokHttpClient apiClient, Logger logger, ClientSettings clientSettings) {
        this.tiktokHttpClient = apiClient;
        this.logger = logger;
        this.clientSettings = clientSettings;
    }


    public void updateSessionId()
    {
        if(clientSettings.getSessionId() == null)
        {
            return;
        }
        if(clientSettings.getSessionId().isEmpty())
        {
          return;
        }
        tiktokHttpClient.setSessionId(clientSettings.getSessionId());
    }

    public boolean sendMessage(String message, String sessionId) {
        if (sessionId.isEmpty()) {
            throw new TikTokLiveException("Session ID must not be Empty");
        }
        var roomId = clientSettings.getClientParameters().get("room_id");
        if (roomId == null) {
            throw new TikTokLiveException("Room ID must not be Empty");
        }
        logger.info("Sending message to chat");
        try {
            var params = new HashMap<String, Object>(clientSettings.getClientParameters());
            params.put("content", message);
            params.put("channel", "tiktok_web");
            params.remove("cursor");
            tiktokHttpClient.setSessionId(sessionId);
            tiktokHttpClient.postMessageToChat(params);
            return true;
        } catch (Exception e) {
            throw new TikTokLiveRequestException("Failed to fetch room id from WebCast, see stacktrace for more info.", e);
        }
    }

    public String fetchRoomId(String userName) {
        logger.info("Fetching room ID");
        String html;
        try {
            html = tiktokHttpClient.getLivestreamPage(userName);
        } catch (Exception e) {
            throw new TikTokLiveRequestException("Failed to fetch room id from WebCast, see stacktrace for more info.", e);
        }

        var firstPattern = Pattern.compile("room_id=([0-9]*)");
        var firstMatcher = firstPattern.matcher(html);
        var id = "";

        if (firstMatcher.find()) {
            id = firstMatcher.group(1);
        } else {
            var secondPattern = Pattern.compile("\"roomId\":\"([0-9]*)\"");
            var secondMatcher = secondPattern.matcher(html);

            if (secondMatcher.find()) {
                id = secondMatcher.group(1);
            }
        }

        if (id.isEmpty()) {
            throw new TikTokLiveOfflineHostException("Unable to fetch room ID, live host could be offline or name is misspelled");
        }

        clientSettings.getClientParameters().put("room_id", id);
        logger.info("RoomID -> " + id);
        return id;
    }


    public LiveRoomMeta fetchRoomInfo() {
        logger.info("Fetch RoomInfo");
        try {
            var response = tiktokHttpClient.getJObjectFromWebcastAPI("room/info/", clientSettings.getClientParameters());
            var mapper = new LiveRoomMetaMapper();
            var liveRoomMeta = mapper.mapFrom(response);
            logger.info("RoomInfo status -> " + liveRoomMeta.getStatus());
            return liveRoomMeta;
        } catch (Exception e) {
            throw new TikTokLiveRequestException("Failed to fetch room info from WebCast, see stacktrace for more info.", e);
        }
    }

    public WebcastResponse fetchClientData() {
        logger.info("Fetch ClientData");
        try {
            var response = tiktokHttpClient.getDeserializedMessage("im/fetch/", clientSettings.getClientParameters());
            clientSettings.getClientParameters().put("cursor", response.getCursor());
            clientSettings.getClientParameters().put("internal_ext", response.getAckIds());
            return response;
        } catch (Exception e) {
            throw new TikTokLiveRequestException("Failed to fetch client data", e);
        }
    }

    public Map<Integer, TikTokGiftInfo> fetchAvailableGifts() {
        try {
            var response = tiktokHttpClient.getJObjectFromWebcastAPI("gift/list/", clientSettings.getClientParameters());
            if (!response.has("data")) {
                return new HashMap<>();
            }
            var dataJson = response.getAsJsonObject("data");
            if (!dataJson.has("gifts")) {
                return new HashMap<>();
            }
            var giftsJsonList = dataJson.get("gifts").getAsJsonArray();
            var gifts = new HashMap<Integer, TikTokGiftInfo>();
            var gson = new Gson();
            for (var jsonGift : giftsJsonList) {
                var gift = gson.fromJson(jsonGift, TikTokGiftInfo.class);
                logger.info("Found Available Gift " + gift.getName() + " with ID " + gift.getId());
                gifts.put(gift.getId(), gift);
            }
            return gifts;
        } catch (Exception e) {
            throw new TikTokLiveRequestException("Failed to fetch giftTokens from WebCast, see stacktrace for more info.", e);
        }
    }
}
