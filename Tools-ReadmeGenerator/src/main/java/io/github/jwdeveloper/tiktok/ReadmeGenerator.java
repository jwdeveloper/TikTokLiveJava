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

        variables.put("version", getCurrentVersion());

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

    public String getCurrentVersion()
    {
        var version = System.getenv("version");;

       return version == null?"NOT_FOUND":version;
    }

    public String getCodeExample(String path)
    {
        return  FilesUtility.loadFileContent(path);
    }

}
