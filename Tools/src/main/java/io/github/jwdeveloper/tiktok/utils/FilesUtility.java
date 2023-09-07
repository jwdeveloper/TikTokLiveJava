package io.github.jwdeveloper.tiktok.utils;

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
