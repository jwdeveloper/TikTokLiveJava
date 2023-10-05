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
package io.github.jwdeveloper.tiktok.events.objects;

import io.github.jwdeveloper.tiktok.messages.webcast.WebcastEnvelopeMessage;
import lombok.Getter;

import java.util.List;

@Getter
public class User {
    private Long userId;
    private String uniqueId;
    private final String nickName;
    private String description;
    private Picture picture;
    private long following;
    private long followers;
    private long followsHost;
    private List<Badge> badges;

    public User(Long userId,
                String uniqueId,
                String nickName,
                String description,
                Picture profilePicture,
                Integer following,
                Integer followers,
                Integer followsHost,
                List<Badge> badges) {
        this.userId = userId;
        this.uniqueId = uniqueId;
        this.nickName = nickName;
        this.description = description;
        this.picture = profilePicture;
        this.following = following;
        this.followers = followers;
        this.followsHost = followsHost;
        this.badges = badges;
    }

    public User(String uniqueId,
                String nickName) {
        this.uniqueId = uniqueId;
        this.nickName = nickName;
    }

    public User(Long userId,
                String nickName,
                Picture picture) {
        this.userId = userId;
        this.nickName = nickName;
        this.picture = picture;
    }

    public User(io.github.jwdeveloper.tiktok.messages.data.User user) {
        assert user != null;
        userId = user.getId();
        uniqueId = user.getSpecialId();
        nickName = user.getNickname();
        description = user.getBioDescription();
        picture = Picture.Map(user.getAvatarThumb());
        following = user.getFollowInfo().getFollowingCount();
        followers = user.getFollowInfo().getFollowerCount();
        followsHost = user.getFollowInfo().getFollowStatus();
        badges = user.getBadgeListList().stream().map(Badge::new).toList();
    }


    public static User EMPTY = new User(0L,
            "",
            "",
            "",
            Picture.Empty(),
            0,
            0,
            0,
            Badge.EmptyList());

    public static User mapOrEmpty(io.github.jwdeveloper.tiktok.messages.data.User user) {
        if (user != null) {
            return new User(user);
        }
        return EMPTY;
    }

    public static User mapOrEmpty(io.github.jwdeveloper.tiktok.messages.data.VoteUser user) {
        return new User(user.getUserId()+"",user.getNickName());
    }

    public static User map(WebcastEnvelopeMessage.EnvelopeInfo envelopeInfo) {
        return new User(0L,
                envelopeInfo.getSendUserId(),
                envelopeInfo.getSendUserName(),
                "",
                Picture.Map(envelopeInfo.getSendUserAvatar()),
                0,
                0,
                0,
                Badge.EmptyList());
    }
}
