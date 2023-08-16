package io.github.jwdeveloper.tiktok.events.objects;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.List;

@Getter
@AllArgsConstructor
public class PollOption {

    private final User user;
    private final List<Option> options;

    @Getter
    @AllArgsConstructor
    public static final class Option {
        private final String label;

        private final Integer total;
    }
}
