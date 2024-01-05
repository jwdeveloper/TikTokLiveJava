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

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.Map;
import java.util.TreeMap;

public class ProtoBufferObject {
    @Getter
    private final Map<Integer, ProtoBufferField> fields;

    public ProtoBufferObject() {
        this.fields = new TreeMap<>();
    }

    public Object getField(int index)
    {
        return fields.get(index);
    }

    public void addField(int index, String type, Object value) {
        fields.put(index, new ProtoBufferField(type, value));
    }

    public void addField(int index, ProtoBufferField value) {
        fields.put(index, value);
    }


    public String toProtoFile()
    {
        return ProtoBufferFileGenerator.generate(this,"UnknownMessage");
    }

    public String toJson()
    {
        return ProtoBufferJsonGenerator.generate(this);
    }
    @Override
    public String toString() {
        return toString(0, true);
    }

    public String toString(int offset ,boolean nested) {

        var sb = new StringBuilder();
        sb.append("\n");
        for (var entry : fields.entrySet()) {
            var index = entry.getKey();
            var field = entry.getValue();

            for(var i =0;i<offset;i++)
            {
                sb.append(" ");
            }
            sb.append(index).append(" ")
                    .append(field.type).append(" ");

            var value = field.value;
            if (value instanceof ProtoBufferObject child) {
                sb.append(child.toString(offset+2,nested));
            } else {
                sb.append(entry.getValue().value);
            }

            sb.append("\n");
        }
        return sb.toString();
    }




    @AllArgsConstructor
    public class ProtoBufferField {
        public String type;

        public Object value;
    }
}
