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
}