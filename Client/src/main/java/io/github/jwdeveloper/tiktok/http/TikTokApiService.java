package io.github.jwdeveloper.tiktok.http;

import com.google.gson.Gson;
import com.google.gson.JsonParser;
import io.github.jwdeveloper.generated.WebcastResponse;
import io.github.jwdeveloper.tiktok.TikTokLiveException;
import io.github.jwdeveloper.tiktok.live.LiveRoomInfo;
import io.github.jwdeveloper.tiktok.live.models.gift.TikTokGift;

import java.util.HashMap;
import java.util.Map;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class TikTokApiService {
    private final TikTokHttpApiClient apiClient;
    private final Logger logger;
    private final Map<String, Object> clientParams;

    public TikTokApiService(TikTokHttpApiClient apiClient, Logger logger, Map<String, Object> clientParams) {
        this.apiClient = apiClient;
        this.logger = logger;
        this.clientParams = clientParams;
    }

    public String fetchRoomId(String userName) {
        logger.info("Fetching room ID");
        String html;
        try {
            html = apiClient.GetLivestreamPage(userName);
        } catch (Exception e) {
            throw new RuntimeException("Failed to fetch room id from WebCast, see stacktrace for more info.", e);
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
            throw new TikTokLiveException("Unable to fetch room ID");
        }

        clientParams.put("room_id", id);
        logger.info("RoomID -> "+id);
        return id;
    }


    public LiveRoomInfo fetchRoomInfo() {
        logger.info("Fetch RoomInfo");
        try {
            var response = apiClient.GetJObjectFromWebcastAPI("room/info/", clientParams);
            if (!response.has("data")) {
                return new LiveRoomInfo();
            }

            var data = response.getAsJsonObject("data");
            if (!data.has("status")) {
                return new LiveRoomInfo();
            }

            var status = data.get("status");

            var info = new LiveRoomInfo();
            info.setStatus(status.getAsInt());

            logger.info("RoomInfo status -> "+info.getStatus());
            return info;
        } catch (Exception e) {
            throw new TikTokLiveException("Failed to fetch room info from WebCast, see stacktrace for more info.", e);
        }
    }

    public WebcastResponse fetchClientData()
    {
        logger.info("Fetch ClientData");
        try {
            var response = apiClient.GetDeserializedMessage("im/fetch/", clientParams);
            clientParams.put("cursor",response.getCursor());
            clientParams.put("internal_ext", response.getInternalExt());
            return response;
        }
        catch (Exception e)
        {
            throw new TikTokLiveException("Failed to fetch client data", e);
        }
    }

    public Map<Integer, TikTokGift> fetchAvailableGifts() {
        try {
            var response = apiClient.GetJObjectFromWebcastAPI("gift/list/", clientParams);
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
            var gifts = new HashMap<Integer, TikTokGift>();
            var gson = new Gson();
            for(var jsonGift : giftsJsonList)
            {
                var gift = gson.fromJson(jsonGift, TikTokGift.class);
                logger.info("Found Available Gift "+ gift.getName()+ " with ID "+gift.getId());
                gifts.put(gift.getId(),gift);
            }
            return gifts;
        } catch (Exception e) {
            throw new TikTokLiveException("Failed to fetch giftTokens from WebCast, see stacktrace for more info.", e);
        }
    }
}
