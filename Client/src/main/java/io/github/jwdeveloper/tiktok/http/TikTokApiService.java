package io.github.jwdeveloper.tiktok.http;

import com.google.gson.Gson;
import io.github.jwdeveloper.tiktok.ClientSettings;
import io.github.jwdeveloper.tiktok.exceptions.TikTokLiveOfflineHostException;
import io.github.jwdeveloper.tiktok.exceptions.TikTokLiveRequestException;
import io.github.jwdeveloper.tiktok.live.LiveRoomMeta;
import io.github.jwdeveloper.tiktok.models.gifts.TikTokGiftInfo;
import io.github.jwdeveloper.tiktok.messages.WebcastResponse;

import java.util.HashMap;
import java.util.Map;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class TikTokApiService {
    private final TikTokHttpApiClient apiClient;
    private final Logger logger;
    private final ClientSettings clientSettings;

    public TikTokApiService(TikTokHttpApiClient apiClient, Logger logger, ClientSettings clientSettings) {
        this.apiClient = apiClient;
        this.logger = logger;
        this.clientSettings = clientSettings;
    }

    public String fetchRoomId(String userName) {
        logger.info("Fetching room ID");
        String html;
        try {
            html = apiClient.GetLivestreamPage(userName);
        } catch (Exception e) {
            throw new TikTokLiveRequestException("Failed to fetch room id from WebCast, see stacktrace for more info.", e);
        }

        Pattern firstPattern = Pattern.compile("room_id=([0-9]*)");
        Matcher firstMatcher = firstPattern.matcher(html);
        String id = "";

        if (firstMatcher.find()) {
            id = firstMatcher.group(1);
        } else {
            Pattern secondPattern = Pattern.compile("\"roomId\":\"([0-9]*)\"");
            Matcher secondMatcher = secondPattern.matcher(html);

            if (secondMatcher.find()) {
                id = secondMatcher.group(1);
            }
        }

        if (id.isEmpty()) {
            throw new TikTokLiveOfflineHostException("Unable to fetch room ID, live host could be offline or name is misspelled");
        }

        clientSettings.getClientParameters().put("room_id", id);
        logger.info("RoomID -> "+id);
        return id;
    }


    public LiveRoomMeta fetchRoomInfo() {
        logger.info("Fetch RoomInfo");
        try {
            var response = apiClient.GetJObjectFromWebcastAPI("room/info/", clientSettings.getClientParameters());
            if (!response.has("data")) {
                return new LiveRoomMeta();
            }

            var data = response.getAsJsonObject("data");
            if (!data.has("status")) {
                return new LiveRoomMeta();
            }

            var status = data.get("status");

            var info = new LiveRoomMeta();
            info.setStatus(status.getAsInt());

            logger.info("RoomInfo status -> "+info.getStatus());
            return info;
        } catch (Exception e) {
            throw new TikTokLiveRequestException("Failed to fetch room info from WebCast, see stacktrace for more info.", e);
        }
    }

    public WebcastResponse  fetchClientData()
    {
        logger.info("Fetch ClientData");
        try {
            var response = apiClient.GetDeserializedMessage("im/fetch/", clientSettings.getClientParameters());
            clientSettings.getClientParameters().put("cursor",response.getCursor());
            clientSettings.getClientParameters().put("internal_ext", response.getAckIds());
            return response;
        }
        catch (Exception e)
        {
            throw new TikTokLiveRequestException("Failed to fetch client data", e);
        }
    }

    public Map<Integer, TikTokGiftInfo> fetchAvailableGifts() {
        try {
            var response = apiClient.GetJObjectFromWebcastAPI("gift/list/", clientSettings.getClientParameters());
            if(!response.has("data"))
            {
                return new HashMap<>();
            }
            var dataJson = response.getAsJsonObject("data");
            if(!dataJson.has("gifts"))
            {
                return new HashMap<>();
            }
            var giftsJsonList = dataJson.get("gifts").getAsJsonArray();
            var gifts = new HashMap<Integer, TikTokGiftInfo>();
            var gson = new Gson();
            for(var jsonGift : giftsJsonList)
            {
                var gift = gson.fromJson(jsonGift, TikTokGiftInfo.class);
                logger.info("Found Available Gift "+ gift.getName()+ " with ID "+gift.getId());
                gifts.put(gift.getId(),gift);
            }
            return gifts;
        } catch (Exception e) {
            throw new TikTokLiveRequestException("Failed to fetch giftTokens from WebCast, see stacktrace for more info.", e);
        }
    }
}
