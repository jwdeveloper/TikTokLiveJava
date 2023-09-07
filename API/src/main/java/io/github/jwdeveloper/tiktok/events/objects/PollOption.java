package io.github.jwdeveloper.tiktok.events.objects;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Value;

import java.util.List;

@Value
public class PollOption {

    private final User user;
    private final List<Option> options;

    @Value
    public static final class Option {
        private final String label;

        private final Integer total;
    }
}
