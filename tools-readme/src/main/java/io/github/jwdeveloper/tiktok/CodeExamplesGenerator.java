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



import java.util.HashMap;
import java.util.regex.Pattern;

public class CodeExamplesGenerator {
    public static void main(String[] args) {
        var result = new CodeExamplesGenerator().run();
        System.out.println(result);
    }

    public String run() {

        var content = FilesUtility.loadFileContent("C:\\Users\\ja\\IdeaProjects\\TikTokLiveJava\\Tools-ReadmeGenerator\\src\\main\\java\\io\\github\\jwdeveloper\\tiktok\\CodeExample.java");
        var p = "<code>(.*?)</code>";
        var r = Pattern.compile(p, Pattern.DOTALL);
        var m = r.matcher(content);


        var pattern = """
                ```java
                {{code}}
                ```
                3. Configure (optional)
                                
                ```java
                {{config}}
                ```
                """;


        var values = new HashMap<String, Object>();
        m.find();
        var code = m.group(0)
                .replace("<code>", "")
                .replace("//  </code>", "")
                .replaceAll("(?m)^ {8}", "");
        values.put("code", code);

        m.find();
        values.put("config", m.group(1));
        var result = TemplateUtility.generateTemplate(pattern, values);


        return result;
    }
}
