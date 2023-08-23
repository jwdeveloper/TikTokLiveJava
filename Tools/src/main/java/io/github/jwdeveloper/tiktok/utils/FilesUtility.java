package io.github.jwdeveloper.tiktok.utils;

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


}
