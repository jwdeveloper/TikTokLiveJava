package io.github.jwdeveloper.tiktok.events_generator;

import lombok.Data;

import java.util.ArrayList;
import java.util.List;

@Data
public class CSharpClassInfo
{
    private String className;
    private List<FieldInfo> fields = new ArrayList<>();
    private List<ConstructorInfo> constructors = new ArrayList<>();

    public void addField(String type, String fields)
    {
        this.fields.add(new FieldInfo(type,fields));
    }

    public void addConstructor(List<FieldInfo> arguments)
    {
        this.constructors.add(new ConstructorInfo(arguments));
    }

    public record FieldInfo(String type, String name){};

    public record ConstructorInfo(List<FieldInfo> arguemtns){};
}
