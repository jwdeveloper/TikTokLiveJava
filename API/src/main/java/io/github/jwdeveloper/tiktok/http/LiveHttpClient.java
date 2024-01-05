package io.github.jwdeveloper.tiktok.http;

import io.github.jwdeveloper.tiktok.data.requests.GiftsData;
import io.github.jwdeveloper.tiktok.data.requests.LiveConnectionData;
import io.github.jwdeveloper.tiktok.data.requests.LiveData;
import io.github.jwdeveloper.tiktok.data.requests.LiveUserData;

public interface LiveHttpClient {


    /**
     * @return list of gifts that are available in your country
     */
    GiftsData.Response fetchGiftsData();

    /**
     * Returns information about user that is having a livestream
     *
     * @param userName
     * @return
     */
    LiveUserData.Response fetchLiveUserData(String userName);

    LiveUserData.Response fetchLiveUserData(LiveUserData.Request request);

    /**
     * @param roomId can be obtained from browsers cookies or by invoked fetchLiveUserData
     * @return
     */
    LiveData.Response fetchLiveData(String roomId);

    LiveData.Response fetchLiveData(LiveData.Request request);


    /**
     * @param roomId can be obtained from browsers cookies or by invoked fetchLiveUserData
     * @return
     */
    LiveConnectionData.Response fetchLiveConnectionData(String roomId);

    LiveConnectionData.Response fetchLiveConnectionData(LiveConnectionData.Request request);
}
