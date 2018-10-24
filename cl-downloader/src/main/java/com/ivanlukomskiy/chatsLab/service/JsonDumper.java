package com.ivanlukomskiy.chatsLab.service;

import com.google.common.io.Resources;
import com.ivanlukomskiy.chatsLab.gui.ChatsListTableModel;
import com.ivanlukomskiy.chatsLab.model.ChatDto;
import com.ivanlukomskiy.chatsLab.model.MessageDto;
import com.ivanlukomskiy.chatsLab.model.MetaDto;
import com.ivanlukomskiy.chatsLab.model.UserDto;
import lombok.SneakyThrows;
import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.io.FileUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.codehaus.jackson.JsonEncoding;
import org.codehaus.jackson.JsonFactory;
import org.codehaus.jackson.JsonGenerator;
import org.codehaus.jackson.JsonNode;
import org.codehaus.jackson.util.DefaultPrettyPrinter;
import org.zeroturnaround.zip.ZipUtil;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static com.ivanlukomskiy.chatsLab.service.IOService.DOWNLOADS_FOLDER;
import static com.ivanlukomskiy.chatsLab.util.Constants.META_FILE_NAME;
import static com.ivanlukomskiy.chatsLab.util.Constants.TELEGRAM_FILE_NAME;
import static com.ivanlukomskiy.chatsLab.util.Constants.USERS_FILE_NAME;
import static com.ivanlukomskiy.chatsLab.util.JacksonUtils.OBJECT_MAPPER;
import static org.codehaus.jackson.map.SerializationConfig.Feature.INDENT_OUTPUT;

/**
 * Dumps messages to files in JSON format; performs zip packaging
 * Created by ivanl <ilukomskiy@sbdagroup.com> on 01.10.2017.
 */
public class JsonDumper implements Dumper {
    private static final Logger logger = LogManager.getLogger(VkService.class);
    private static final String VERSION_FILE_NAME = "version.info";

    private JsonFactory factory = new JsonFactory();
    private JsonGenerator generator = null;

    private static final File DOWNLOADS_TEMP = DOWNLOADS_FOLDER.toPath().resolve("temp").toFile();
    private File archiveFile;

    private int messagesCount = 0;
    private int chatsCount = 0;
    private Integer earlierDate = null;
    private Integer latestDate = null;
    private int usersCount = 0;

    @Override
    public void prepare() {
        try {
            FileUtils.deleteDirectory(DOWNLOADS_TEMP);
        } catch (IOException e) {
        }
    }

    @Override
    @SneakyThrows
    public void startWriting(ChatDto chat) {
        archiveFile = DOWNLOADS_FOLDER.toPath()
                .resolve("chats" + (System.currentTimeMillis() / 1000) + ".zip")
                .toFile();

        DOWNLOADS_TEMP.mkdirs();
        File currentFile = DOWNLOADS_TEMP.toPath().resolve(DigestUtils
                .md5Hex(String.valueOf(chat.getName())).substring(0, 8) + ".json").toFile();

        generator = factory.createJsonGenerator(currentFile, JsonEncoding.UTF8)
                .setPrettyPrinter(new DefaultPrettyPrinter())
                .setCodec(OBJECT_MAPPER);
        generator.writeStartObject();
        generator.writeFieldName("details");
        generator.writeObject(chat);
        generator.writeFieldName("messages");
        generator.writeStartArray();
        chatsCount++;
    }

    @Override
    @SneakyThrows
    public void writeMessage(MessageDto message) {
        generator.writeObject(message);
        messagesCount++;
        if (earlierDate == null || earlierDate > message.getTimestamp()) {
            earlierDate = message.getTimestamp();
        }
        if (latestDate == null || latestDate < message.getTimestamp()) {
            latestDate = message.getTimestamp();
        }
    }

    @SneakyThrows
    public void writeTelegramChats(List<JsonNode> chats) {
        OBJECT_MAPPER.enable(INDENT_OUTPUT);
        OBJECT_MAPPER.writeValue(DOWNLOADS_TEMP.toPath().resolve(TELEGRAM_FILE_NAME).toFile(), chats);
    }

    @Override
    @SneakyThrows
    public void finishWriting() {
        generator.writeEndArray();
        generator.writeEndObject();
        generator.close();
    }

    @Override
    @SneakyThrows
    public void writeUsers(Map<Integer, UserDto> users) {
        OBJECT_MAPPER.enable(INDENT_OUTPUT);
        OBJECT_MAPPER.writeValue(DOWNLOADS_TEMP.toPath().resolve(USERS_FILE_NAME).toFile(), users.values());
        usersCount += users.size();
    }

    @Override
    @SneakyThrows
    public void writeMetaInfo(int providerId) {
        MetaDto dto = new MetaDto();
        dto.setProviderId(providerId);
        dto.setChatsTotal(chatsCount);
        dto.setMessagesTotal(messagesCount);
        dto.setEarlierDate(new Date(((long) earlierDate) * 1000));
        dto.setLatestDate(new Date(((long) latestDate) * 1000));
        dto.setDownloadDate(new Date());
        dto.setUuid(UUID.randomUUID());
        dto.setUsersNumber(usersCount);
        URL url = Resources.getResource(VERSION_FILE_NAME);
        String version = Resources.toString(url, StandardCharsets.UTF_8);
        dto.setVersion(version);
        OBJECT_MAPPER.writeValue(DOWNLOADS_TEMP.toPath().resolve(META_FILE_NAME).toFile(), dto);
    }

    @Override
    @SneakyThrows
    public void finalize() {
        ZipUtil.pack(DOWNLOADS_TEMP, archiveFile);
        if (archiveFile.exists()) {
            FileUtils.deleteDirectory(DOWNLOADS_TEMP);
        } else {
            logger.error("Failed to do package");
        }
    }
}
