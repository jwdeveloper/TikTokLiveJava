/*
 * Copyright (c) 2023-2023 jwdeveloper jacekwoln@gmail.com
 *
 * Permission is hereby granted, free of charge, to any person obtaining
 * a copy of this software and associated documentation files (the
 * "Software"), to deal in the Software without restriction, including
 * without limitation the rights to use, copy, modify, merge, publish,
 * distribute, sublicense, and/or sell copies of the Software, and to
 * permit persons to whom the Software is furnished to do so, subject to
 * the following conditions:
 *
 * The above copyright notice and this permission notice shall be
 * included in all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND,
 * EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF
 * MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND
 * NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE
 * LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION
 * OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION
 * WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 */
package io.github.jwdeveloper.tiktok.http;

import io.github.jwdeveloper.tiktok.data.requests.GiftsData;
import io.github.jwdeveloper.tiktok.data.requests.LiveConnectionData;
import io.github.jwdeveloper.tiktok.data.requests.LiveData;
import io.github.jwdeveloper.tiktok.data.requests.LiveUserData;

public interface LiveHttpClient
{
    /**
     * @return {@link GiftsData.Response} list of gifts that are compiled and available on github
     */
    GiftsData.Response fetchGiftsData();

    /**
     * @return {@link GiftsData.Response} list of gifts that are available in your region / livestream
     */
    GiftsData.Response fetchRoomGiftsData(String room_id);

    /**
     * Returns information about user that is having a livestream
     * @param userName name of user
     * @return {@link LiveUserData.Response}
     */
    default LiveUserData.Response fetchLiveUserData(String userName) {
        return fetchLiveUserData(new LiveUserData.Request(userName));
    }

    LiveUserData.Response fetchLiveUserData(LiveUserData.Request request);

    /**
     * @param roomId can be obtained from browsers cookies or by invoked fetchLiveUserData
     * @return {@link LiveData.Response}
     */
    default LiveData.Response fetchLiveData(String roomId) {
        return fetchLiveData(new LiveData.Request(roomId));
    }

    LiveData.Response fetchLiveData(LiveData.Request request);

    /**
     * @param roomId can be obtained from browsers cookies or by invoked fetchLiveUserData
     * @return {@link LiveConnectionData.Response}
     */
    default LiveConnectionData.Response fetchLiveConnectionData(String roomId) {
        return fetchLiveConnectionData(new LiveConnectionData.Request(roomId));
    }

    LiveConnectionData.Response fetchLiveConnectionData(LiveConnectionData.Request request);
}