package io.github.jwdeveloper.tiktok;

import io.github.jwdeveloper.tiktok.utils.FilesUtility;
import io.github.jwdeveloper.tiktok.utils.TemplateUtility;

import java.util.HashMap;
import java.util.regex.Pattern;

public class ReadmeGenerator
{

    public void generate()
    {
        var template = FilesUtility.getFileFromResource(Main.class,"template.md");
        var variables = new HashMap<String,Object>();

        var pomPath = "C:\\Users\\ja\\IdeaProjects\\TikTokLiveJava\\Tools-ReadmeGenerator\\pom.xml";

        variables.put("version", getCurrentVersion(pomPath));


        var exampleCodePath = "C:\\Users\\ja\\IdeaProjects\\TikTokLiveJava\\TestApplication\\src\\main\\java\\io\\github\\jwdeveloper\\tiktok\\SimpleExample.java";
        variables.put("Code-Example", getCodeExample(exampleCodePath));


        var exampleConfigurationPath = "C:\\Users\\ja\\IdeaProjects\\TikTokLiveJava\\TestApplication\\src\\main\\java\\io\\github\\jwdeveloper\\tiktok\\ConfigurationExample.java";
        variables.put("Configuration-Example", getCodeExample(exampleConfigurationPath));

        variables.put("Events", EventsListGenerator.GetEventsList());

        var listenerExamplePath = "C:\\Users\\ja\\IdeaProjects\\TikTokLiveJava\\TestApplication\\src\\main\\java\\io\\github\\jwdeveloper\\tiktok\\ListenerExample.java";
        variables.put("Listener-Example", getCodeExample(listenerExamplePath));

       // var liveClientPath = "C:\\Users\\ja\\IdeaProjects\\TikTokLiveJava\\API\\src\\main\\java\\io\\github\\jwdeveloper\\tiktok\\live\\LiveClient.java";
       // variables.put("methods", LiveClientMethodsGenerator.generate(liveClientPath));

        template = TemplateUtility.generateTemplate(template, variables);
        var outputPath = "C:\\Users\\ja\\IdeaProjects\\TikTokLiveJava\\Tools-ReadmeGenerator\\src\\main\\resources\\output.md";
        FilesUtility.saveFile(outputPath, template);
    }

    public String getCurrentVersion(String path)
    {
        var content =  FilesUtility.loadFileContent(path);
        var pattern = "<version>(.*?)<\\/version>";

        // Create a Pattern object
        var regex = Pattern.compile(pattern);

        // Create a Matcher object
        var matcher = regex.matcher(content);

        // Find the first match
        if (matcher.find()) {
            // Extract and print the version
           return matcher.group(1);
        }
       return "VERSION NOT FOUND";
    }

    public String getCodeExample(String path)
    {
        return  FilesUtility.loadFileContent(path);
    }

}
