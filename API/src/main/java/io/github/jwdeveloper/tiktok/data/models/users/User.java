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

import io.github.jwdeveloper.tiktok.data.models.Picture;
import io.github.jwdeveloper.tiktok.data.models.badges.Badge;
import io.github.jwdeveloper.tiktok.messages.webcast.*;
import lombok.*;

import java.util.*;

@Getter
public class User {
    private final Long id;
    private final String name;
    private String profileName;
    private Picture picture;
    private long following;
    private long followers;
    private List<Badge> badges;
    @Getter(AccessLevel.NONE)
    private final Set<UserAttribute> attributes = new HashSet<>();

    public List<UserAttribute> getAttributes() {
        return attributes.stream().toList();
    }

    public boolean hasAttribute(UserAttribute attribute) {
        return attributes.contains(attribute);
    }

    public void addAttribute(UserAttribute... attributes) {
        this.attributes.addAll(List.of(attributes));
    }

    public boolean isGiftGiver() {
        return hasAttribute(UserAttribute.GiftGiver);
    }

    public boolean isSubscriber() {
        return hasAttribute(UserAttribute.Subscriber);
    }

    public boolean isMutualFollowingWithHost() {
        return hasAttribute(UserAttribute.MutualFollowingWithLiveHost);
    }

    public boolean isFollower() {
        return hasAttribute(UserAttribute.Follower);
    }

    public boolean isAdmin() {
        return hasAttribute(UserAttribute.Admin);
    }

    public boolean isMuted() {
        return hasAttribute(UserAttribute.Muted);
    }

    public boolean isBlocked() {
        return hasAttribute(UserAttribute.Blocked);
    }

    public boolean isModerator() {
        return hasAttribute(UserAttribute.Moderator);
    }

    public boolean isLiveHost() {
        return hasAttribute(UserAttribute.LiveHost);
    }

    public User(Long userId,
                String nickName,
                Picture profilePicture,
                Integer following,
                Integer followers,
                List<Badge> badges) {
        this.id = userId;
        this.name = nickName;
        this.picture = profilePicture;
        this.following = following;
        this.followers = followers;
        this.badges = badges;
    }

    public User(Long id,
                String name,
                String profileName,
                Picture picture,
                long following,
                long followers,
                List<Badge> badges) {
        this.id = id;
        this.name = name;
        this.profileName = profileName;
        this.picture = picture;
        this.following = following;
        this.followers = followers;
        this.badges = badges;
    }

    public User(Long userId,
                String nickName) {
        this.id = userId;
        this.name = nickName;
    }

    public User(Long userId,
                String nickName,
                Picture picture) {
        this(userId, nickName);
        this.picture = picture;
    }

    public User(long id, String name, String profileId, Picture picture) {
        this(id, name, profileId, picture, 0, 0, List.of(Badge.empty()));
    }

    public User(WebcastLinkMicBattle.LinkMicBattleHost.HostGroup.Host host) {
        this(host.getId(), host.getName(), host.getProfileId(), Picture.map(host.getImages(0)));
    }

    public User(io.github.jwdeveloper.tiktok.messages.data.User user) {
        this(user.getId(), user.getDisplayId(), Picture.map(user.getAvatarThumb()));
        profileName = user.getNickname();
        following = user.getFollowInfo().getFollowingCount();
        followers = user.getFollowInfo().getFollowerCount();
        badges = user.getBadgeListList().stream().map(Badge::map).toList();
        if (user.getIsFollower()) {
            addAttribute(UserAttribute.Follower);
        }
        if (user.getSubscribeInfo() != null && user.getSubscribeInfo().getIsSubscribedToAnchor()) {
            addAttribute(UserAttribute.Subscriber);
        }
        if (user.getUserAttr().getIsAdmin()) {
            addAttribute(UserAttribute.Admin);
        }
        if (user.getUserAttr().getIsMuted()) {
            addAttribute(UserAttribute.Muted);
        }
        if (user.getIsBlock()) {
            addAttribute(UserAttribute.Blocked);
        }
    }

    public static User EMPTY = new User(0L,
        "",
        Picture.empty(),
        0,
        0,
        List.of(Badge.empty()));

    public static User map(io.github.jwdeveloper.tiktok.messages.data.User user) {
        return new User(user);
    }

    public static User map(io.github.jwdeveloper.tiktok.messages.data.User user,
                           io.github.jwdeveloper.tiktok.messages.data.UserIdentity userIdentity) {
        var outUser = map(user);

        if (userIdentity.getIsGiftGiverOfAnchor()) {
            outUser.addAttribute(UserAttribute.GiftGiver);
        }
        if (userIdentity.getIsSubscriberOfAnchor()) {
            outUser.addAttribute(UserAttribute.Subscriber);
        }
        if (userIdentity.getIsMutualFollowingWithAnchor()) {
            outUser.addAttribute(UserAttribute.MutualFollowingWithLiveHost);
        }
        if (userIdentity.getIsFollowerOfAnchor()) {
            outUser.addAttribute(UserAttribute.Follower);
        }
        if (userIdentity.getIsModeratorOfAnchor()) {
            outUser.addAttribute(UserAttribute.Moderator);
        }
        if (userIdentity.getIsAnchor()) {
            outUser.addAttribute(UserAttribute.LiveHost);
        }
        return outUser;
    }

    public static User map(io.github.jwdeveloper.tiktok.messages.data.VoteUser user) {
        return new User(user.getUserId(), user.getNickName());
    }

    public static User map(WebcastEnvelopeMessage.EnvelopeInfo envelopeInfo) {
        return new User(0L,
            //envelopeInfo.getSendUserId(),
            envelopeInfo.getSendUserName(),
            Picture.map(envelopeInfo.getSendUserAvatar()),
            0,
            0,
            List.of(Badge.empty()));
    }

    @Override
    public String toString() {
        return "User{" +
            "id=" + id +
            ", name='" + name + "'" +
            ", profileName='" + profileName + "'" +
            ", picture=" + picture +
            ", following=" + following +
            ", followers=" + followers +
            ", badges=" + badges +
            ", attributes=" + attributes +
			"}";
    }
}