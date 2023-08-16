package io.github.jwdeveloper.tiktok.events_generator;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class CSharpClassParser {
    private CSharpClassInfo classInfo;

    public CSharpClassInfo parse(Path filePath) throws IOException {
        classInfo = new CSharpClassInfo();

        List<String> lines = Files.readAllLines(filePath);
        String content = String.join("\n", lines);
        parseClassName(content);
        parseFields(content);
        parseConstructors(content);
        return classInfo;
    }


    private void parseClassName(String content) {
        Pattern pattern = Pattern.compile("\\b(?:sealed )?class\\s+(\\w+)");
        Matcher matcher = pattern.matcher(content);
        if (matcher.find()) {
            classInfo.setClassName(matcher.group(1));
        }
    }

    private void parseFields(String content) {
        Pattern pattern = Pattern.compile("\\b(public|private|protected)\\s+(readonly\\s+)?(\\w+\\.?\\w*)\\s+(\\w+);");
        Matcher matcher = pattern.matcher(content);
        while (matcher.find()) {
            var typeName = mapTypeToJava(matcher.group(3));
            var name = lowerCaseFirstLetter(matcher.group(4));
            classInfo.addField(typeName, name);
        }
    }

    private void parseConstructors(String content) {
        Pattern pattern = Pattern.compile("\\b(public|private|protected|internal)\\s+" + classInfo.getClassName() + "\\s*\\(([^)]*)\\)");
        Matcher matcher = pattern.matcher(content);
        while (matcher.find()) {
            List<CSharpClassInfo.FieldInfo> args = new ArrayList<>();
            String[] arguments = matcher.group(2).split(",");
            for (String argument : arguments) {
                if (argument.trim().length() > 0) {
                    String[] parts = argument.trim().split("\\s+");

                    if (parts.length != 2) {
                        args.add(new CSharpClassInfo.FieldInfo("Object", "error"));
                        continue;
                    }
                    var typeName = mapTypeToJava(parts[0]);
                    var name = lowerCaseFirstLetter(parts[1]);
                    args.add(new CSharpClassInfo.FieldInfo(typeName, name));
                }
            }
            classInfo.addConstructor(args);
        }
    }


    public String mapTypeToJava(String type) {
        if (type.equals("string")) {
            return "String";
        }
        if (type.equals("uint")) {
            return "Integer";
        }
        if (type.equals("int")) {
            return "Integer";
        }
        if (type.equals("ulong")) {
            return "Long";
        }
        if (type.equals("bool")) {
            return "Boolean";
        }
        if (type.contains("Models.Protobuf.Objects")) {
            return type.replace("Models.Protobuf.Objects", "io.github.jwdeveloper.tiktok.messages");
        }

        if(type.contains("Objects."))
        {
            return type.replace("Objects.","io.github.jwdeveloper.tiktok.events.objects.");
        }
        return type;
    }

    public static String lowerCaseFirstLetter(String str) {
        if (str == null || str.isEmpty()) {
            return str; // Return original string if it is empty or null
        }
        return Character.toLowerCase(str.charAt(0)) + str.substring(1);
    }

}
