package io.github.jwdeveloper.tiktok;


import org.apache.commons.io.FileUtils;

import java.io.File;
import java.io.IOException;

public class Main
{
    public static void main(String[] args) throws IOException {
        var version = System.getenv("VERSION");
        if (version == null || version.equals("")) {
            version = "[Replace with current version]";
        }

        var inputStream = Main.class.getResourceAsStream("/readme-template.html");
        var targetFile = new File("temp.file");
        FileUtils.copyInputStreamToFile(inputStream, targetFile);

        var output = System.getProperty("user.dir");



    }
}
