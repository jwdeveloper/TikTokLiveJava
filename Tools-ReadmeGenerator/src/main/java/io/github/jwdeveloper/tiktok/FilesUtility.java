
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
package io.github.jwdeveloper.tiktok;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

public class FilesUtility
{
    public static List<Path> getFiles(String input) {
        Path path = Paths.get(input);
        try (Stream<Path> paths = Files.list(path)) {
            return paths.filter(Files::isRegularFile).toList();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
    public static String getFileFromResource(Class clazz, String path)
    {
        try {
            var stream =clazz.getClassLoader().getResourceAsStream(path);
            var bytes=  stream.readAllBytes();
            stream.close();
            return new String(bytes);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
    public static List<Path> getFileContent(String input) {
        Path path = Paths.get(input);
        try (Stream<Path> paths = Files.list(path)) {
            return paths.filter(Files::isRegularFile).toList();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public static void saveFile(String path, String content)
    {
        Path filePath = Paths.get(path);
        try {
            // Write the content to the file
            Files.write(filePath, content.getBytes(StandardCharsets.UTF_8));

        } catch (IOException e) {

            e.printStackTrace();
        }
    }

    public   static boolean pathExists(String path) {
        var directory = new File(path);
        return directory.exists();
    }

    public static File ensurePath(String path) {
        var directory = new File(path);
        if (directory.exists()) {
            return directory;
        }
        directory.mkdirs();
        return directory;
    }

    public static void ensureFile(String paths) {
        var file =   new File(paths);
        if (!file.exists()) {
            try {
                file.createNewFile();
            } catch (IOException e)
            {
                e.printStackTrace();
            }
        }
    }


    public static String loadFileContent(String path) {
        ensureFile(path);
        Path pathh = Paths.get(path);
        try {
            return   new String(Files.readAllBytes(pathh));
        } catch (IOException e) {
            e.printStackTrace();
        }
        return "";
    }

}
