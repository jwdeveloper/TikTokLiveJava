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
package io.github.jwdeveloper.tiktok.data.models.users;

import lombok.Getter;

@Getter
public class ListUser
{
    private final User user;
    private final LinkType linkType;
    private final long linkMicId, linkStatus, modifyTime, linkerId;
    private final int userPosition, silenceStatus, roleType;

    public ListUser(io.github.jwdeveloper.tiktok.messages.data.ListUser listUser) {
        this.user = User.map(listUser.getUser());
        this.linkMicId = listUser.getLinkmicId();
        this.linkStatus = listUser.getLinkStatus();
        this.linkType = LinkType.values()[listUser.getLinkTypeValue()];
        this.userPosition = listUser.getUserPosition();
        this.silenceStatus = listUser.getSilenceStatus();
        this.modifyTime = listUser.getModifyTime();
        this.linkerId = listUser.getLinkerId();
        this.roleType = listUser.getRoleType();
    }

    public static ListUser map(io.github.jwdeveloper.tiktok.messages.data.ListUser listUser) {
        return new ListUser(listUser);
    }

    public enum LinkType {
        UNKNOWN,
        AUDIO,
        VIDEO
    }

    @Override
    public String toString() {
        return "ListUser{" +
            "user=" + user +
            ", linkType=" + linkType +
            ", linkMicId=" + linkMicId +
            ", linkStatus=" + linkStatus +
            ", modifyTime=" + modifyTime +
            ", linkerId=" + linkerId +
            ", userPosition=" + userPosition +
            ", silenceStatus=" + silenceStatus +
            ", roleType=" + roleType +
            "}";
    }
}