package io.github.jwdeveloper.tiktok.tools.collector.db;

import io.github.jwdeveloper.tiktok.tools.collector.tables.TikTokErrorModel;

import org.jdbi.v3.sqlobject.config.RegisterBeanMapper;
import org.jdbi.v3.sqlobject.customizer.BindBean;
import org.jdbi.v3.sqlobject.statement.SqlQuery;
import org.jdbi.v3.sqlobject.statement.SqlUpdate;

import java.util.List;


@RegisterBeanMapper(TikTokErrorModel.class)
public interface TikTokErrorModelDAO
{
    @SqlUpdate("INSERT INTO TikTokErrorModel (hostName, errorName, errorType, exceptionContent, message, response, createdAt) " +
            "VALUES (:hostName, :errorName, :errorType, :exceptionContent, :message, :response, :createdAt)")
    void insertTikTokMessage(@BindBean TikTokErrorModel message);

    @SqlQuery("SELECT * FROM TikTokErrorModel")
    List<TikTokErrorModel> selectErrors();
}
