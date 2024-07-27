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
package io.github.jwdeveloper.tiktok.data.requests;

import io.github.jwdeveloper.tiktok.data.models.users.User;
import lombok.*;

public class LiveUserData {

    @Getter
    public static class Request {
        private final String userName;

        public Request(String userName) {
            if (userName == null || userName.isBlank())
                throw new IllegalArgumentException("Invalid empty username!");
            this.userName = userName;
        }
    }

    @Getter
    @AllArgsConstructor
    public static class Response {
        private final String json;
        private final UserStatus userStatus;
        private final String roomId;
        private final long startTime;
        private final User user;

        public boolean isLiveOnline() {
            return userStatus == LiveUserData.UserStatus.Live || userStatus == LiveUserData.UserStatus.LivePaused;
        }

        public boolean isHostNameValid() {
            return userStatus != LiveUserData.UserStatus.NotFound;
        }
    }

    public enum UserStatus {
        NotFound,
        Offline,
        LivePaused,
        Live,
    }
}