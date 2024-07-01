package io.github.jwdeveloper.tiktok;

import com.google.common.base.Charsets;
import com.google.common.collect.Maps;
import com.google.common.io.Resources;
import com.hubspot.jinjava.Jinjava;

import java.io.IOException;
import java.util.HashMap;


public class Main2 {
    public static void main(String[] args) throws IOException {
        var version = System.getenv("VERSION");
        if (version == null || version.equals("")) {
            version = "[Replace with current version]";
        }

        var template = Resources.toString(Resources.getResource("my-template.html"), Charsets.UTF_8);

        var jinjava = new Jinjava();
        var context = new HashMap<String, Object>();
        context.put("version", version);

        var renderedTemplate = jinjava.render(template, context);
    }
}
