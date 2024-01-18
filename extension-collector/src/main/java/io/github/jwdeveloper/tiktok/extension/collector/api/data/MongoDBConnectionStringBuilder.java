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
package io.github.jwdeveloper.tiktok.extension.collector.api.data;

public class MongoDBConnectionStringBuilder {
    private String username;
    private String password;
    private String database;
    private String cluster;

    public MongoDBConnectionStringBuilder setUsername(String username) {
        this.username = username;
        return this;
    }

    public MongoDBConnectionStringBuilder setPassword(String password) {
        this.password = password;
        return this;
    }

    public MongoDBConnectionStringBuilder setDatabase(String database) {
        this.database = database;
        return this;
    }

    public MongoDBConnectionStringBuilder setCluster(String cluster) {
        this.cluster = cluster;
        return this;
    }

    public String build() {
        return String.format("mongodb+srv://%s:%s@%s/%s?retryWrites=true&w=majority",
                username, password, cluster, database);
    }
}
