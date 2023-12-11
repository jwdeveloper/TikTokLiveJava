package io.github.jwdeveloper.tiktok.utils;

import java.nio.file.Path;
import java.util.Map;
import java.util.TreeMap;

public class ProtoBufferFileGenerator {


    public static String generate(ProtoBufferObject protoBuffObj, String name) {

        var sb = new StringBuilder();
        var offset = 2;
        sb.append("message ").append(name).append(" { \n");

        var objects = new TreeMap<String, ProtoBufferObject>();
        var objectCounter = 0;
        for (var entry : protoBuffObj.getFields().entrySet()) {
            var index = entry.getKey();
            var field = entry.getValue();
            var fieldName = field.type.toLowerCase() + "Value";
            var value = field.value;
            if (field.value instanceof ProtoBufferObject object) {
                fieldName += objectCounter;
                value = "";
                objects.put(fieldName,object);
                objectCounter++;

            }
            for (var i = 0; i < offset; i++) {
                sb.append(" ");
            }
            sb.append(field.type).append(" ").append(fieldName)
                    .append(" ")
                    .append("=")
                    .append(" ")
                    .append(index)
                    .append(";")
                    .append(" //")
                    .append(value)
                    .append("\n");
        }
        sb.append(" \n");
        for(var object : objects.entrySet())
        {
            var structure = generate(object.getValue(),object.getKey());
            sb.append(structure);
        }


        sb.append(" \n");
        sb.append("} \n");
        return sb.toString();
    }

}
