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

import io.github.jwdeveloper.tiktok.exceptions.TikTokLiveException;
import lombok.Getter;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.*;
import java.net.URL;
import java.util.concurrent.CompletableFuture;

public class Picture {

    @Getter
    private final String link;

    private Image image;

    public Picture(String link) {
        this.link = link;
    }

    public static Picture map(io.github.jwdeveloper.tiktok.messages.data.Image profilePicture) {
        var index = profilePicture.getUrlCount() - 1;
        if (index < 0) {
            return new Picture("");
        }
        var url = profilePicture.getUrl(index);
        return new Picture(url);
    }

    public boolean isDownloaded() {
        return image != null;
    }

    public Image downloadImage() {
        if (isDownloaded()) {
            return image;
        }
        image = download(link);
        return image;
    }

    public CompletableFuture<Image> downloadImageAsync() {
        return CompletableFuture.supplyAsync(this::downloadImage);
    }

    private BufferedImage download(String urlString) {
        if (urlString.isEmpty()) {
            return null;
        }

        var baos = new ByteArrayOutputStream();
        try (var is = new URL(urlString).openStream()) {
            var byteChunk = new byte[4096];
            int n;

            while ((n = is.read(byteChunk)) > 0) {
                baos.write(byteChunk, 0, n);
            }
        } catch (IOException e) {
            throw new TikTokLiveException("Unable map downloaded image", e);
        }

        var bais = new ByteArrayInputStream(baos.toByteArray());
        try {
            return ImageIO.read(bais);
        } catch (IOException e) {
            throw new TikTokLiveException("Unable map downloaded image bytes to Image", e);
        }
    }

    public static Picture empty() {
        return new Picture("");
    }

    @Override
    public String toString() {
        return "Picture{link='" + link + "', image=" + image + "}";
    }
}