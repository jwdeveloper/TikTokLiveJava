package io.github.jwdeveloper.tiktok.events.objects;

import lombok.Value;

@Value
public class EnumValue
{
    public int value;
    public String name;

    public static EnumValue Map(Enum<?> _enum)
    {
        return new EnumValue(_enum.ordinal() ,_enum.name());
    }
}
