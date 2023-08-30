package io.github.jwdeveloper.tiktok.tools.collector.db;

public class SqlConsts
{
    public static String CREATE_MESSAGES_TABLE = """
               CREATE TABLE IF NOT EXISTS TikTokMessageModel (
                    id INTEGER PRIMARY KEY AUTOINCREMENT,
                    hostName TEXT,
                    type TEXT,
                    eventName TEXT,
                    eventContent TEXT,
                    createdAt TEXT
                );
            """;

    public static String CREATE_ERROR_TABLE = """
          CREATE TABLE IF NOT EXISTS  TikTokErrorModel (
                id INT AUTO_INCREMENT PRIMARY KEY,
                hostName VARCHAR(255),
                errorName VARCHAR(255),
                errorType VARCHAR(255),
                exceptionContent TEXT,
                message TEXT,
                response TEXT,
                createdAt DATETIME
            );
            """;

}
