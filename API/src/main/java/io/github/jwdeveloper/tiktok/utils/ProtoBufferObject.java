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
