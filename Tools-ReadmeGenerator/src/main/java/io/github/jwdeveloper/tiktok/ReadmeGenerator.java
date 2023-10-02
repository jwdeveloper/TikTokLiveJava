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
