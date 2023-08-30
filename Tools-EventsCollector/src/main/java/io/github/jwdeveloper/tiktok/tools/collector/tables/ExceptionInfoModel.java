package io.github.jwdeveloper.tiktok.tools.collector.tables;

import java.io.PrintWriter;
import java.io.StringWriter;

public class ExceptionInfoModel
{
    private String message;
    private String stackTrace;

    public ExceptionInfoModel(Throwable throwable) {
        this.message = throwable.getMessage();
        this.stackTrace = getStackTraceAsString(throwable);
    }

    public static String getStackTraceAsString(Throwable throwable) {
        StringWriter sw = new StringWriter();
        PrintWriter pw = new PrintWriter(sw);
        throwable.printStackTrace(pw);
        return sw.toString();
    }

    // Getters for message and stackTrace
    public String getMessage() {
        return message;
    }

    public String getStackTrace() {
        return stackTrace;
    }
}
