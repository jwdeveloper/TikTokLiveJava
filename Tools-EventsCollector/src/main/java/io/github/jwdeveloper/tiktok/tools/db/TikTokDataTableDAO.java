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
import org.jdbi.v3.sqlobject.config.RegisterBeanMapper;
import org.jdbi.v3.sqlobject.customizer.Bind;
import org.jdbi.v3.sqlobject.customizer.BindBean;
import org.jdbi.v3.sqlobject.statement.SqlQuery;
import org.jdbi.v3.sqlobject.statement.SqlUpdate;

import java.util.List;
import java.util.Map;

@RegisterBeanMapper(TikTokDataTable.class)
public interface TikTokDataTableDAO {
    String query = """
            INSERT INTO TikTokData (sessionTag, tiktokUser, dataType, dataTypeName, content, createdAt)  VALUES (:sessionTag, :tiktokUser, :dataType, :dataTypeName, :content, :createdAt)  
            """;

    @SqlUpdate(query)
    void insertData(@BindBean TikTokDataTable data);

    @SqlQuery("SELECT * FROM TikTokData WHERE sessionTag = :sessionTag")
    List<TikTokDataTable> selectBySession(@Bind("sessionTag") String sessionTag);

    @SqlQuery("SELECT * FROM TikTokData WHERE dataType = :dataType AND sessionTag = :sessionTag AND  tiktokUser = :tiktokUser")
    List<TikTokDataTable> selectSessionData(@Bind("dataType") String dataType,
                                            @Bind("sessionTag") String sessionTag,
                                            @Bind("tiktokUser") String user);

    @SqlQuery("SELECT * FROM TikTokData WHERE sessionTag = :sessionTag AND  tiktokUser = :tiktokUser AND dataType = \"response\"")
    List<TikTokDataTable> selectResponces(@Bind("sessionTag") String sessionTag, @Bind("tiktokUser") String user);

    @SqlQuery("SELECT * FROM TikTokData WHERE sessionTag = :sessionTag AND  tiktokUser = :tiktokUser AND dataType = \"event\"")
    List<TikTokDataTable> selectBySessionEvents(@Bind("sessionTag") String sessionTag, @Bind("tiktokUser") String userName);

    @SqlQuery("SELECT * FROM TikTokData WHERE sessionTag = :sessionTag AND  tiktokUser = :tiktokUser AND dataType = \"message\"")
    List<TikTokDataTable> selectBySessionMessages(@Bind("sessionTag") String sessionTag, @Bind("tiktokUser") String userName);


    @SqlQuery("SELECT tiktokUser FROM TikTokData GROUP BY tiktokUser")
    List<String> getUsers();


    @SqlQuery("SELECT sessionTag FROM TikTokData WHERE tiktokUser = :tiktokUser  GROUP BY sessionTag")
    List<String> getSessionTagByUser(@Bind("tiktokUser") String tiktokUser);

    String groupByDataTypeNameQuery = """
            SELECT dataTypeName, COUNT(*) as count
            FROM TikTokData
            WHERE dataType = 'message' AND sessionTag = :sessionTag AND tiktokUser = :userName
            GROUP BY dataTypeName
            """;


}
