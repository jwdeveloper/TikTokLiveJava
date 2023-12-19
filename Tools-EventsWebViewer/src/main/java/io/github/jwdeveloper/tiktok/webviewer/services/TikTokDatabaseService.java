package io.github.jwdeveloper.tiktok.webviewer.services;

import io.github.jwdeveloper.tiktok.messages.webcast.WebcastResponse;
import io.github.jwdeveloper.tiktok.tools.db.TikTokDataTableDAO;
import io.github.jwdeveloper.tiktok.tools.db.TikTokDatabase;
import io.github.jwdeveloper.tiktok.tools.db.tables.TikTokDataTable;
import io.github.jwdeveloper.tiktok.tools.util.MessageUtil;
import io.github.jwdeveloper.tiktok.utils.JsonUtil;
import io.github.jwdeveloper.tiktok.utils.ProtocolUtils;

import java.util.Base64;
import java.util.List;

public class TikTokDatabaseService {
    public TikTokDatabase tikTokDatabase;
    private TikTokDataTableDAO table;


    public record DatabaseDataDto(String dataType, String dataName, String user, String sessionTag, boolean asJson) {
    }


    public TikTokDatabaseService(TikTokDatabase databaseName) {
        tikTokDatabase = databaseName;
        table = tikTokDatabase.getDataTableDAO();
    }

    public List<String> getUsers() {
        return table.getUsers();
    }

    public List<String> getSessionTag(String user) {
        return table.getSessionTagByUser(user);
    }

    public List<String> getDataNames(String dataType, String user, String sessionTag) {

        return tikTokDatabase.getDataNames(dataType, sessionTag, user);
    }

    public List<String> getData(DatabaseDataDto dataDto) {
        var data = tikTokDatabase.getDataTableDAO().selectSessionData(dataDto.dataType(), dataDto.sessionTag(), dataDto.user());
        switch (dataDto.dataType()) {
            case "message" -> {
                return getMessages(dataDto.dataName(), data, dataDto.asJson);
            }
            case "event" -> {
                return getEvents(dataDto.dataName(), data);
            }
            case "response" -> {
                return getResponse(data,dataDto.dataName());
            }
        }
        return List.of("unknown dataType");
    }


    private List<String> getMessages(String messageName, List<TikTokDataTable> data, boolean asJson) {

        var messages = data.stream()
                .filter(e -> e.getDataTypeName().equals(messageName))
                .map(e ->
                {

                    try {
                        var bytes = Base64.getDecoder().decode(e.getContent());
                        if (asJson == false) {
                            return ProtocolUtils.getProtocolBufferStructure(bytes).toJson();
                        }
                        var parsedMessage = MessageUtil.getContent(messageName, bytes);
                        return parsedMessage;
                    } catch (Exception ex) {
                        ex.printStackTrace();
                        return "";
                    }

                })
                .toList();

        return messages;
    }

    private List<String> getResponse(List<TikTokDataTable> data,String dataTypeName)
    {
        var messages = data.stream()
                .filter(e -> e.getDataTypeName().equals(dataTypeName))
                .map(e ->
                {
                    if(e.getDataTypeName().equals("Http"))
                    {
                        return e.getContent();
                    }
                    try
                    {
                        var bytes = Base64.getDecoder().decode(e.getContent());
                        var parsedMessage = WebcastResponse.parseFrom(bytes);
                        var json = JsonUtil.toJson(parsedMessage);
                        return json;
                    } catch (Exception ex) {
                        ex.printStackTrace();
                        return "error";
                    }

                })
                .toList();

        return messages;
    }

    private List<String> getEvents(String dataName, List<TikTokDataTable> data) {
        var messages = data.stream()
                .filter(e -> e.getDataTypeName().equals(dataName))
                .map(TikTokDataTable::getContent)
                .toList();

        return messages;
    }
}
