package io.github.jwdeveloper.tiktok.tools.collector.db;

import io.github.jwdeveloper.tiktok.tools.collector.tables.TikTokErrorModel;

import org.jdbi.v3.sqlobject.customizer.BindBean;
import org.jdbi.v3.sqlobject.statement.SqlUpdate;

public interface TikTokErrorModelDAO
{
    @SqlUpdate("INSERT INTO TikTokErrorModel (hostName, errorName, errorType, exceptionContent, message, response, createdAt) " +
            "VALUES (:hostName, :errorName, :errorType, :exceptionContent, :message, :response, :createdAt)")
    void insertTikTokMessage(@BindBean TikTokErrorModel message);
}
