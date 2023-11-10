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
package io.github.jwdeveloper.tiktok.data.models;

import io.github.jwdeveloper.tiktok.data.models.users.User;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Value;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.regex.Pattern;

@Getter
public class Text {
    String key;
    String pattern;
    List<TextPiece> textPieces;
    String value;

    public Text(String key, String pattern, List<TextPiece> textPieces) {
        this.key = key;
        this.pattern = pattern;
        this.textPieces = textPieces;
        this.value = computeValue();
    }


    public <T extends TextPiece> Optional<TextPiece> getTextPiece(Class<T> type)
    {
        return textPieces.stream().filter(e -> e.getClass().equals(type)).findFirst();
    }

    public static Text map(io.github.jwdeveloper.tiktok.messages.data.Text input) {
        var pieces = input.getPiecesListList().stream().map(Text::mapTextPiece).toList();
        return new Text(input.getKey(), input.getDefaultPattern(), pieces);
    }


    public static TextPiece mapTextPiece(io.github.jwdeveloper.tiktok.messages.data.Text.TextPiece input) {
        return switch (input.getType()) {
            case 11 -> {
                var user = User.map(input.getUserValue().getUser());
                yield new UserTextPiece(user);
            }
            case 12 -> new GiftTextPiece(input.getGiftValue().getGiftId());
            default -> new StringTextPiece(input.getStringValue());
        };
    }


    private String computeValue() {
        var regexPattern = Pattern.compile("\\{.*?\\}");
        var matcher = regexPattern.matcher(pattern);
        var format = matcher.replaceAll("%s");

        var output = new ArrayList<String>();
        for (var piece : textPieces)
        {
            output.add(piece.getText());
        }
        if(matcher.groupCount() != output.size())
        {
            return format;
        }
        return String.format(format, output.toArray());
    }


    @Getter
    @AllArgsConstructor
    public static class TextPiece {
        public String getText() {
            return "";
        }
    }

    @Value
    public static class StringTextPiece extends TextPiece {
        String text;

        public StringTextPiece(String text) {
            this.text = text;
        }
    }

    @Value
    public static class UserTextPiece extends TextPiece {
        User user;

        public UserTextPiece(User user) {
            this.user = user;
        }


        @Override
        public String getText() {
            return user.getProfileName();
        }
    }

    public static class GiftTextPiece extends TextPiece {

        int giftId;

        public GiftTextPiece(int giftId) {
            this.giftId = giftId;
        }

        @Override
        public String getText() {
            return giftId + "";
        }
    }
}
