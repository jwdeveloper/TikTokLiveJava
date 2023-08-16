package io.github.jwdeveloper.tiktok.events_generator;

import io.github.jwdeveloper.tiktok.FilesUtility;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.List;

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
