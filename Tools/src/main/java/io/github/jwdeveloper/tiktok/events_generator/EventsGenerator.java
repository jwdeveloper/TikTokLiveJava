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
package io.github.jwdeveloper.tiktok.events_generator;

import io.github.jwdeveloper.tiktok.utils.FilesUtility;

import java.io.File;
import java.io.IOException;

public class EventsGenerator
{


    public void compile(EventGeneratorSettings settings) throws IOException {
        var files = FilesUtility.getFiles(settings.getInputDictionary());

        var packageName = convertToPackageName(settings.getOutputDictionary());
        for(var file : files)
        {
            var fileName = file.getFileName().toString();
            if(settings.getIgnoredFiles().contains(fileName))
            {
                continue;
            }
            if(fileName.contains("meta"))
            {
                continue;
            }

            var parser = new CSharpClassParser();
            var cSharpClass =parser.parse(file);

            var name = settings.getPrefix()+cSharpClass.getClassName()+settings.getEndFix();
            cSharpClass.setClassName(name);
            var javaClassGenerator = new JavaClassGenerator();


            var result =javaClassGenerator.generate(cSharpClass, packageName,settings);
            System.out.println(result);

            var path = settings.getOutputDictionary()+ File.separator+cSharpClass.getClassName()+".java";
            FilesUtility.saveFile(path, result);
        }

    }


    public static String convertToPackageName(String path) {
        String marker = "src\\main\\java\\";
        int index = path.indexOf(marker);

        if (index != -1) {
            String packagePath = path.substring(index + marker.length());
            return packagePath.replace('\\', '.');
        }

        return null;
    }
}
