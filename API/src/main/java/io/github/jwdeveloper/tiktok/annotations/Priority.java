package io.github.jwdeveloper.tiktok.annotations;

/**
 * HIGHEST 1
 * HIGH 2
 * NORMAL 3
 * LOW 4
 * LOWEST 5
 */
public enum Priority {
    LOWEST(2), LOW(1), NORMAL(0), HIGH(-1), HIGHEST(-2);

    public final int value;

    Priority(int value) {
        this.value = value;
    }
}