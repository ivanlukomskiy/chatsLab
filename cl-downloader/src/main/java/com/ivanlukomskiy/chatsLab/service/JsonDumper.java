package com.ivanlukomskiy.chatsLab.service;

import com.ivanlukomskiy.chatsLab.model.dto.ChatDto;
import com.ivanlukomskiy.chatsLab.model.dto.MessageDto;
import com.ivanlukomskiy.chatsLab.model.dto.UserDto;
import lombok.SneakyThrows;
import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.io.FileUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.codehaus.jackson.JsonEncoding;
import org.codehaus.jackson.JsonFactory;
import org.codehaus.jackson.JsonGenerator;
import org.codehaus.jackson.map.ObjectMapper;
import org.codehaus.jackson.util.DefaultPrettyPrinter;
import org.zeroturnaround.zip.ZipUtil;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Map;

import static com.ivanlukomskiy.chatsLab.service.IOService.DOWNLOADS_FOLDER;
import static org.codehaus.jackson.map.SerializationConfig.Feature.INDENT_OUTPUT;

/**
 * Created by ivanl <ilukomskiy@sbdagroup.com> on 01.10.2017.
 */
public class JsonDumper implements Dumper {
    private static final Logger logger = LogManager.getLogger(VkService.class);

    private static final ObjectMapper MAPPER = new ObjectMapper();

    private JsonFactory factory = new JsonFactory();
    private JsonGenerator generator = null;

    private static final File DOWNLOADS_TEMP = DOWNLOADS_FOLDER.toPath().resolve("temp").toFile();
    private File archiveFile;

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
                .setCodec(MAPPER);
        generator.writeStartObject();
        generator.writeFieldName("details");
        generator.writeObject(chat);
        generator.writeFieldName("messages");
        generator.writeStartArray();
    }

    @Override
    @SneakyThrows
    public void writeMessage(MessageDto message) {
        generator.writeObject(message);
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
        MAPPER.enable(INDENT_OUTPUT);
        MAPPER.writeValue(DOWNLOADS_TEMP.toPath().resolve("users.json").toFile(), users);
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
