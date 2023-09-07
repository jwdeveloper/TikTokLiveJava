package io.github.jwdeveloper.tiktok.utils;

import java.util.Map;

public class TemplateUtility
{
    public static String generateTemplate(String template, Map<String, Object> values) {
        for(var entry : values.entrySet())
        {
            template = doReplacement(template,entry.getKey(), entry.getValue().toString());
        }
        return template;
    }

    public static String generateTemplate2(String template, Map<String, Object> values) {
        for(var entry : values.entrySet())
        {
            template = doReplacement2(template,entry.getKey(), entry.getValue().toString());
        }
        return template;
    }

    private static String doReplacement(String template, String keyword, String value)
    {
        var key = "(\\{\\{)"+keyword+"(}})";
        return template.replaceAll(key, value);
    }

    private static String doReplacement2(String template, String keyword, String value)
    {
        var key = "(\\$)("+keyword+")(\\$)";
        return template.replaceAll(key, value);
    }
}
