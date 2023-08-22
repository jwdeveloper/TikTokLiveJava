package io.github.jwdeveloper.tiktok.events.objects;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.List;

@Getter
public class Badge {
    private  ComboBadge comboBadges;
    private final List<TextBadge> textBadges;
    private final List<ImageBadge> imageBadges;

    public Badge(io.github.jwdeveloper.tiktok.messages.Badge badge) {
        textBadges = badge.getTextBadgesList().stream().map(b -> new TextBadge(b.getType(), b.getName())).toList();
        imageBadges = badge.getImageBadgesList().stream().map(b -> new ImageBadge(b.getDisplayType(), new Picture(b.getImage()))).toList();
        comboBadges = new ComboBadge(new Picture("badge.getComplexBadge().getImageUrl()"), badge.getComplexBadge().getData());
    }


    @AllArgsConstructor
    @Getter
    public class TextBadge {
        private final String type;
        private final String name;
    }

    @AllArgsConstructor
    @Getter
    public class ImageBadge {
        private final Integer displayType;
        private final Picture image;
    }

    @AllArgsConstructor
    @Getter
    public class ComboBadge {
        private final Picture image;
        private final String data;
    }
}
