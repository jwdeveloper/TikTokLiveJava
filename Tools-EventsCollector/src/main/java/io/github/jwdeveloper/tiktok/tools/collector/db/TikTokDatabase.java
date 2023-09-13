package io.github.jwdeveloper.tiktok.tools.collector.db;

import io.github.jwdeveloper.tiktok.tools.collector.tables.TikTokErrorModel;
import io.github.jwdeveloper.tiktok.tools.collector.tables.TikTokMessageModel;
import org.jdbi.v3.core.Jdbi;
import org.jdbi.v3.sqlobject.SqlObjectPlugin;

import java.sql.DriverManager;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

public class TikTokDatabase
{
    private final String database;
    private TikTokMessageModelDAO messagesTable;
    private TikTokErrorModelDAO errorTable;

    public TikTokDatabase(String database)  {
        this.database =database;
    }

    public void init() throws SQLException {
        var jdbcUrl ="jdbc:sqlite:"+database+".db";
        var connection = DriverManager.getConnection(jdbcUrl);
        var jdbi = Jdbi.create(jdbcUrl)
                .installPlugin(new SqlObjectPlugin());
        jdbi.useHandle(handle -> {
            handle.execute(SqlConsts.CREATE_MESSAGES_TABLE);
            handle.execute(SqlConsts.CREATE_ERROR_TABLE);
        });
     //   jdbi.registerRowMapper(new TikTokErrorModelMapper());
        messagesTable = jdbi.onDemand(TikTokMessageModelDAO.class);
        errorTable = jdbi.onDemand(TikTokErrorModelDAO.class);
    }

    public void insertMessage(TikTokMessageModel message)
    {
        var dateFormat = new SimpleDateFormat("dd:MM:yyyy HH:mm:ss.SSS");
        message.setCreatedAt(dateFormat.format(new Date()));
        messagesTable.insertTikTokMessage(message);
    }

    public void insertError(TikTokErrorModel message)
    {
        var dateFormat = new SimpleDateFormat("dd:MM:yyyy HH:mm:ss.SSS");
        message.setCreatedAt(dateFormat.format(new Date()));
        errorTable.insertTikTokMessage(message);
    }

    public List<TikTokErrorModel> selectErrors()
    {
       return errorTable.selectErrors();
    }
}
