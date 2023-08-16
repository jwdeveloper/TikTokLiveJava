package io.github.jwdeveloper.tiktok.events_generator;

import lombok.Data;

import java.util.ArrayList;
import java.util.List;

@Data
public class EventGeneratorSettings
{
    private String inputDictionary;

    private String outputDictionary;
    private List<String> ignoredFiles = new ArrayList<>();

    private String prefix;

    private String endFix;

    private boolean isTikTokEvent;

    public void addIgnoredClass(String name)
    {
        ignoredFiles.add(name);
    }
}
