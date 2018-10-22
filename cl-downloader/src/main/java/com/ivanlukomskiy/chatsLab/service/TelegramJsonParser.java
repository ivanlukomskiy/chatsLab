package com.ivanlukomskiy.chatsLab.service;

import com.ivanlukomskiy.chatsLab.util.JacksonUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.codehaus.jackson.JsonNode;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static java.util.Arrays.asList;

/**
 * Created by ivanl <ilukomskiy@sbdagroup.com> on 15.10.2018.
 */
public class TelegramJsonParser implements TelegramParser {
    private static final Logger logger = LogManager.getLogger(TelegramJsonParser.class);
    private static final SimpleDateFormat DATE_FORMAT = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss");

    private static final Set<String> ALLOWED_TYPES =
            new HashSet<>(asList("private_supergroup", "private_group"));

    public List<JsonNode> getChats(String filePath) throws IOException {
        JsonNode telegramData = JacksonUtils.OBJECT_MAPPER.readTree(new File(filePath));
        return selectValidChats(telegramData);
    }

    private List<JsonNode> selectValidChats(JsonNode telegramData) throws IOException {
        JsonNode chatsList = telegramData.get("chats").get("list");
        Set<String> types = new HashSet<>();

        List<JsonNode> validChats = new ArrayList<>();

        for (JsonNode chat : chatsList) {
            try {
                String type = chat.get("type").asText();
                System.out.println(type);
                types.add(type);
                if (!ALLOWED_TYPES.contains(type)) {
                    logger.debug("Skipping chat 'cause it has type " + type);
                    continue;
                }
                JsonNode nameNode = chat.get("name");
                if (nameNode == null || nameNode.isNull()) {
                    logger.warn("Skipping chat with type " + type + ", 'cause it has null name");
                    continue;
                }
                logger.info("Chat: " + nameNode);
                validChats.add(chat);
            } catch (Exception e) {
                logger.debug("Failed to parse one of the chats", e);
            }
        }
        logger.info("Types detected: ", String.join(", ", types));

        return validChats;
    }
}
