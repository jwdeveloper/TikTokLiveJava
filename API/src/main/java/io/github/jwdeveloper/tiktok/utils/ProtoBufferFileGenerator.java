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
package io.github.jwdeveloper.tiktok.utils;

import java.nio.file.Path;
import java.util.Map;
import java.util.TreeMap;

public class ProtoBufferFileGenerator {


    public static String generate(ProtoBufferObject protoBuffObj, String name) {

        StringBuilder sb = new StringBuilder();
        int offset = 2;
        sb.append("message ").append(name).append(" { \n");

        Map<String, ProtoBufferObject> objects = new TreeMap<String, ProtoBufferObject>();
        int objectCounter = 0;
        for (Map.Entry<Integer, ProtoBufferObject.ProtoBufferField> entry : protoBuffObj.getFields().entrySet()) {
            int index = entry.getKey();
            ProtoBufferObject.ProtoBufferField field = entry.getValue();
            String fieldName = field.type.toLowerCase() + "Value";
            Object value = field.value;
            if (field.value instanceof ProtoBufferObject) {
                ProtoBufferObject object = (ProtoBufferObject) field.value;
                fieldName += objectCounter;
                value = "";
                objects.put(fieldName,object);
                objectCounter++;

            }
            for (int i = 0; i < offset; i++) {
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
        for(Map.Entry<String, ProtoBufferObject> object : objects.entrySet())
        {
            String structure = generate(object.getValue(),object.getKey());
            sb.append(structure);
        }


        sb.append(" \n");
        sb.append("} \n");
        return sb.toString();
    }

}
