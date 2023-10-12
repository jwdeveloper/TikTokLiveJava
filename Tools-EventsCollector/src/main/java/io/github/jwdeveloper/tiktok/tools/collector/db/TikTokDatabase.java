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

import io.github.jwdeveloper.tiktok.tools.collector.tables.TikTokErrorModel;
import io.github.jwdeveloper.tiktok.tools.collector.tables.TikTokMessageModel;
import io.github.jwdeveloper.tiktok.tools.collector.tables.TikTokResponseModel;
import org.jdbi.v3.core.Jdbi;
import org.jdbi.v3.sqlobject.SqlObjectPlugin;

import java.sql.DriverManager;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

public class TikTokDatabase {
    private final String database;
    private TikTokMessageModelDAO messagesTable;
    private TikTokErrorModelDAO errorTable;
    private TikTokResponseModelDAO responseTable;

    public TikTokDatabase(String database) {
        this.database = database;
    }

    public void init() throws SQLException {
        var jdbcUrl = "jdbc:sqlite:" + database + ".db";
        DriverManager.getConnection(jdbcUrl);
        var jdbi = Jdbi.create(jdbcUrl)
                .installPlugin(new SqlObjectPlugin());
        jdbi.useHandle(handle -> {
            handle.execute(SqlConsts.CREATE_MESSAGES_TABLE);
            handle.execute(SqlConsts.CREATE_ERROR_TABLE);
            handle.execute(SqlConsts.CREATE_RESPONSE_MODEL);
        });
        messagesTable = jdbi.onDemand(TikTokMessageModelDAO.class);
        errorTable = jdbi.onDemand(TikTokErrorModelDAO.class);
        responseTable = jdbi.onDemand(TikTokResponseModelDAO.class);
    }

    public void insertMessage(TikTokMessageModel message) {
        message.setCreatedAt(getTime());
        messagesTable.insertTikTokMessage(message);
    }

    public void insertError(TikTokErrorModel message) {
        message.setCreatedAt(getTime());
        errorTable.insertTikTokMessage(message);
    }

    public void insertResponse(TikTokResponseModel message) {
        message.setCreatedAt(getTime());
        responseTable.insert(message);
    }

    public List<TikTokErrorModel> selectErrors() {
        return errorTable.selectErrors();
    }

    public List<TikTokMessageModel> selectMessages() {
        return messagesTable.select();
    }

    public List<TikTokResponseModel> selectResponces() {
        return responseTable.select();
    }

    private String getTime() {
        return new SimpleDateFormat("dd:MM:yyyy HH:mm:ss.SSS").format(new Date());
    }
}
