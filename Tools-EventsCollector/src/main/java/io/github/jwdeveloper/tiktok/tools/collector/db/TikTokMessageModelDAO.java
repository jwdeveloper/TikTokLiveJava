package io.github.jwdeveloper.tiktok.tools.collector.db;

import io.github.jwdeveloper.tiktok.tools.collector.tables.TikTokMessageModel;
import org.jdbi.v3.sqlobject.customizer.BindBean;
import org.jdbi.v3.sqlobject.statement.SqlUpdate;

public interface TikTokMessageModelDAO
{
    @SqlUpdate("INSERT INTO TikTokMessageModel (hostName, eventName,type, eventContent, createdAt) " +
            "VALUES (:hostName, :eventName, :type, :eventContent, :createdAt)")
    void insertTikTokMessage(@BindBean TikTokMessageModel message);
}
