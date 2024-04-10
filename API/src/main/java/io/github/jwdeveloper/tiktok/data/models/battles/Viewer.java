package io.github.jwdeveloper.tiktok.data.models.battles;

import io.github.jwdeveloper.tiktok.data.models.Picture;
import io.github.jwdeveloper.tiktok.data.models.users.User;
import io.github.jwdeveloper.tiktok.messages.webcast.WebcastLinkMicBattle;
import lombok.Getter;

@Getter
public class Viewer {
    private final User user;
    private final int points;

    public Viewer(WebcastLinkMicBattle.LinkMicBattleTopViewers.TopViewerGroup.TopViewer topViewer) {
        this.user = new User(topViewer.getId(), null, topViewer.getProfileId(), Picture.map(topViewer.getImages(0)));
        this.points = topViewer.getPoints();
    }
}