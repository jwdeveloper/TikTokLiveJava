/*
 * Copyright (c) 2023-2023 jwdeveloper jacekwoln@gmail.com
 *
 * Permission is hereby granted, free of charge, to any person obtaining
 * a copy of this software and associated documentation files (the
 * "Software"), to deal in the Software without restriction, including
 * without limitation the rights to use, copy, modify, merge, publish,
 * distribute, sublicense, and/or sell copies of the Software, and to
 * permit persons to whom the Software is furnished to do so, subject to
 * the following conditions:
 *
 * The above copyright notice and this permission notice shall be
 * included in all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND,
 * EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF
 * MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND
 * NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE
 * LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION
 * OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION
 * WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 */
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

    public static String CREATE_RESPONSE_MODEL = """
          CREATE TABLE IF NOT EXISTS  TikTokResponseModel (
                  id INTEGER PRIMARY KEY AUTOINCREMENT,
                    hostName TEXT,
                    response TEXT,
                    createdAt TEXT
            );
            """;

}
