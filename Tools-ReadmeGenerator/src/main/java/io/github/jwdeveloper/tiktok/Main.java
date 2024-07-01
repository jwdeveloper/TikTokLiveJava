package io.github.jwdeveloper.tiktok;

import io.github.jwdeveloper.descrabble.api.DescriptionGenerator;
import io.github.jwdeveloper.descrabble.framework.Descrabble;
import io.github.jwdeveloper.descrabble.plugin.github.DescrabbleGithub;
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

        DescriptionGenerator generator = Descrabble.create()
                .withTemplate(targetFile)
                .withVariable("version", version)
                .withDecorator(new EventsDecorator())
                .withPlugin(DescrabbleGithub.plugin("README.md"))
                .build();


        generator.generate(output);
        targetFile.delete();
        inputStream.close();
    }
}
