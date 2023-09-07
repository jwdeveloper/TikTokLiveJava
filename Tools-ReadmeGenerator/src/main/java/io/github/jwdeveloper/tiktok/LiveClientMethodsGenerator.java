package io.github.jwdeveloper.tiktok;

import io.github.jwdeveloper.tiktok.utils.FilesUtility;

import java.util.regex.Pattern;

public class LiveClientMethodsGenerator
{



    //| Method Name | Description |
    //| ----------- | ----------- |


    public static String generate(String path)
    {


        var sb = new StringBuilder();
        sb.append("| Method Name | Description |");
        sb.append(System.lineSeparator());
        sb.append("| ----------- | ----------- |");

        var content = FilesUtility.loadFileContent(path);
        var pattern = "// \\s*(.*?)\\n\\s*(\\w+)\\s*\\(.*?\\)";

        // Create a Pattern object
        var regex = Pattern.compile(pattern);

        // Create a Matcher object
        var matcher = regex.matcher(content);

        // Find and print method names and comments
        while (matcher.find()) {
            String comment = matcher.group(1);
            String methodName = matcher.group(2);
            sb.append(System.lineSeparator());
            sb.append("| ").append(methodName).append(" | ").append(comment).append(" |");
        }
        sb.append(System.lineSeparator());
        return sb.toString();
    }

}
