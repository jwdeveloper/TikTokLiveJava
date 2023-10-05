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

import io.github.jwdeveloper.tiktok.messages.data.BadgeStruct;
import lombok.Value;

import java.util.ArrayList;
import java.util.List;

@Value
public class Badge {
    ComboBadge comboBadges;
    List<TextBadge> textBadges;
    List<ImageBadge> imageBadges;

    public Badge(ComboBadge comboBadges, List<TextBadge> textBadges, List<ImageBadge> imageBadges) {
        this.comboBadges = comboBadges;
        this.textBadges = textBadges;
        this.imageBadges = imageBadges;
    }

    public Badge(io.github.jwdeveloper.tiktok.messages.data.BadgeStruct badge)
    {
        comboBadges = ComboBadge.map(badge.getCombine());
        textBadges = TextBadge.mapAll(badge.getTextList());
        imageBadges = ImageBadge.mapAll(badge.getImageList());
    }

    @Value
    public static class TextBadge {
        EnumValue type;
        String name;

        public static TextBadge map(BadgeStruct.TextBadge input) {
            return new TextBadge(EnumValue.Map(input.getDisplayType()),input.getKey());
        }
        public static List<TextBadge> mapAll(List<BadgeStruct.TextBadge> list) {
            return list.stream().map(TextBadge::map).toList();
        }
    }

    @Value
    public static class ImageBadge {
        EnumValue displayType;
        Picture image;

        public static ImageBadge map(BadgeStruct.ImageBadge input) {
            return new ImageBadge(EnumValue.Map(input.getDisplayType()), Picture.Map(input.getImage()));
        }
        public static List<ImageBadge> mapAll(List<BadgeStruct.ImageBadge> list) {
            return list.stream().map(ImageBadge::map).toList();
        }
    }

    @Value
    public static class ComboBadge {
        Picture image;
        String data;

        public static ComboBadge map(BadgeStruct.CombineBadge input) {
            return new ComboBadge(Picture.Map(input.getIcon()),input.getStr());
        }
        public static List<ComboBadge> mapAll(List<BadgeStruct.CombineBadge> list) {
            return list.stream().map(ComboBadge::map).toList();
        }
    }

    public static Badge Empty() {
        var comboBadge = new ComboBadge(Picture.Empty(), "");
        var textBadges = new ArrayList<TextBadge>();
        var imageBadges = new ArrayList<ImageBadge>();
        return new Badge(comboBadge, textBadges, imageBadges);
    }

    public static List<Badge> EmptyList() {
        return new ArrayList<Badge>();
    }
}
