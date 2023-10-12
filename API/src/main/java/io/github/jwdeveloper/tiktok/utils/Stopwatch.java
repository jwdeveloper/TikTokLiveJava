package io.github.jwdeveloper.tiktok.utils;

public class Stopwatch {
    private long startTime;
    private long stopTime;

    public void start() {
        startTime = System.nanoTime();
    }

    public long stop() {
        stopTime = System.nanoTime();
        return getElapsedTime();
    }

    public long getElapsedTime() {
        return stopTime - startTime;
    }

}