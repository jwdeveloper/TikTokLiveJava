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
package io.github.jwdeveloper.tiktok.tools.db;

import io.github.jwdeveloper.tiktok.tools.db.tables.TikTokDataTable;
import io.github.jwdeveloper.tiktok.tools.db.tables.TikTokErrorModel;
import lombok.Getter;
import org.jdbi.v3.core.Jdbi;
import org.jdbi.v3.sqlobject.SqlObjectPlugin;
import org.sqlite.SQLiteConfig;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class TikTokDatabase {
    private final String database;

    private TikTokErrorModelDAO errorTable;

    @Getter
    private TikTokDataTableDAO dataTableDAO;

    private Connection connection;


    public TikTokDatabase(String database) {
        this.database = database;
    }

    public boolean isConnected()
    {
        return connection != null;
    }

    public void connect() throws SQLException {
        var jdbcUrl = "jdbc:sqlite:" + database + ".db";
        var config = new SQLiteConfig();
        config.setEncoding(SQLiteConfig.Encoding.UTF8);
        connection = DriverManager.getConnection(jdbcUrl, config.toProperties());
        var jdbi = Jdbi.create(jdbcUrl).installPlugin(new SqlObjectPlugin());
        jdbi.useHandle(handle -> {
            handle.execute(SqlConsts.CREATE_DATA_TABLE);
            handle.execute(SqlConsts.CREATE_ERROR_TABLE);
        });
        dataTableDAO = jdbi.onDemand(TikTokDataTableDAO.class);
        errorTable = jdbi.onDemand(TikTokErrorModelDAO.class);
    }

    public void close() throws SQLException {
        connection.close();
    }

    public void insertData(TikTokDataTable tikTokDataTable) {
        tikTokDataTable.setCreatedAt(getTime());
        dataTableDAO.insertData(tikTokDataTable);
    }

    public List<TikTokDataTable> getSessionResponces(String sessionTag, String userName) {
        return dataTableDAO.selectResponces(sessionTag, userName);
    }

    public List<String> getDataNames(String dataType, String sessionTag, String userName) {

        try {
            var sb = new StringBuilder();
            sb.append("""
                        SELECT dataTypeName, COUNT(*) as count
                        FROM TikTokData
                    """);
            sb.append(" WHERE dataType = \""+dataType+"\" ");
            sb.append(" AND tiktokUser = \"" + userName + "\" ");
            sb.append(" AND sessionTag = \"" + sessionTag + "\" ");
            sb.append("GROUP BY dataTypeName");
            var statement = connection.prepareStatement(sb.toString());
            var resultSet = statement.executeQuery();
            List<String> dataTypeCounts = new ArrayList<>();
            while (resultSet.next()) {
                var dataTypeName = resultSet.getString("dataTypeName");
                dataTypeCounts.add(dataTypeName);
            }

            resultSet.close();
            statement.close();

            return dataTypeCounts;
        } catch (Exception e) {
            e.printStackTrace();
            return List.of("error");
        }


    }



    public List<TikTokDataTable> getSessionMessages(String sessionTag, String userName, int count) {
        return dataTableDAO.selectBySessionMessages(sessionTag, userName);
    }

    public void insertError(TikTokErrorModel message) {
        message.setCreatedAt(getTime());
        errorTable.insertTikTokMessage(message);
    }


    public List<TikTokErrorModel> selectErrors() {
        return errorTable.selectErrors();
    }

    private String getTime() {
        return new SimpleDateFormat("dd:MM:yyyy HH:mm:ss.SSS").format(new Date());
    }
}
