package com.ivanlukomskiy.chatsLab.service;

import com.google.common.collect.Iterators;
import com.google.common.collect.UnmodifiableIterator;
import com.ivanlukomskiy.chatsLab.model.*;
import lombok.SneakyThrows;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.codehaus.jackson.JsonNode;
import org.codehaus.jackson.node.ArrayNode;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.InputStream;
import java.nio.file.Path;
import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.regex.Pattern;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

import static com.ivanlukomskiy.chatsLab.util.Constants.META_FILE_NAME;
import static com.ivanlukomskiy.chatsLab.util.Constants.USERS_FILE_NAME;
import static com.ivanlukomskiy.chatsLab.util.JacksonUtils.OBJECT_MAPPER;

/**
 * Created by ivanl <ilukomskiy@sbdagroup.com> on 10.10.2017.
 */
@Service
public class GatheringService {
    private static final Logger logger = LogManager.getLogger(GatheringService.class);
    private static final Pattern CHAT_PATTERN = Pattern.compile("\\w{8}\\.json");

    @Autowired
    private UserService userService;

    @Autowired
    private MessagesService messagesService;

    @Autowired
    private PacksService packsService;

    @Autowired
    private ChatService chatService;

    private ExecutorService executorService;

    @Transactional
    @SneakyThrows
    void loadPack(Path path) {
        ZipFile zipFile = new ZipFile(path.toFile());
        ZipEntry metaFileEntry = zipFile.getEntry(META_FILE_NAME);
        if (metaFileEntry == null) {
            throw new RuntimeException("Missing " + META_FILE_NAME + " in package " + path);
        }
        try (InputStream inputStream = zipFile.getInputStream(metaFileEntry)) {
            MetaDto metaDto = OBJECT_MAPPER.readValue(inputStream, MetaDto.class);

            Map<Integer, User> usersMap = loadUsers(zipFile, zipFile.getEntry(USERS_FILE_NAME),
                    metaDto.getDownloadDate());

            User provider = userService.getById(metaDto.getProviderId());

            Pack pack = new Pack();
            pack.setProvider(provider);
            pack.setDownloadTime(metaDto.getDownloadDate());
            pack.setVersion(metaDto.getVersion());
            pack.setUuid(metaDto.getUuid().toString());
            packsService.save(pack);

            executorService = Executors.newFixedThreadPool(4);

            zipFile.stream()
                    .filter(entry -> CHAT_PATTERN.matcher(entry.getName()).matches())
                    .forEach(entry -> loadChat(zipFile, entry, metaDto.getDownloadDate(), usersMap, pack));

            executorService.shutdown();
            executorService.awaitTermination(5, TimeUnit.MINUTES);
            logger.info("Loading of messages pack {} finished", path);
        }
    }

    @SneakyThrows
    public boolean isLoaded(Path path) {

        ZipFile zipFile = new ZipFile(path.toFile());
        ZipEntry metaFileEntry = zipFile.getEntry(META_FILE_NAME);
        if (metaFileEntry == null) {
            throw new RuntimeException("Missing " + META_FILE_NAME + " in package " + path);
        }
        try (InputStream inputStream = zipFile.getInputStream(metaFileEntry)) {
            MetaDto metaDto = OBJECT_MAPPER.readValue(inputStream, MetaDto.class);
            return packsService.loaded(metaDto.getUuid().toString());
        }
    }

    @SneakyThrows
    private void loadChat(ZipFile file, ZipEntry entry, Date updateTime, Map<Integer, User> usersMap, Pack pack) {

        try (InputStream stream = file.getInputStream(entry)) {
            JsonNode jsonNode = OBJECT_MAPPER.readTree(stream);
            ChatDto details = OBJECT_MAPPER.treeToValue(jsonNode.get("details"), ChatDto.class);

            User admin = userService.getById(details.getAdminId());

            Chat chat = new Chat();
            chat.setName(details.getName());
            chat.setAdmin(admin);
            chat.setUpdateTime(updateTime);
            chat.getPacks().add(pack);

            ArrayNode messageNodes = (ArrayNode) jsonNode.get("messages");
            List<Message> messages = new ArrayList<>();
            int wordsInChat = 0;
            for (JsonNode messageNode : messageNodes) {
                MessageDto messageDto = OBJECT_MAPPER.treeToValue(messageNode, MessageDto.class);

                int wordsInMessage = getWordsNumber(messageDto.getContent());
                wordsInChat += wordsInMessage;

                Message message = new Message();
                message.setChat(chat);
                message.setContent(messageDto.getContent());
                message.setSender(usersMap.get(messageDto.getAuthor()));
                message.setPack(pack);
                message.setTime(new Date(((long)messageDto.getTimestamp()) * 1000));
                message.setWordsNumber(wordsInMessage);
                messages.add(message);
            }

            chat.setMessagesCount(messages.size());
            chat.setWordsNumber(wordsInChat);
            chatService.save(chat);

            Iterator<Message> messageIterator = messages.iterator();
            UnmodifiableIterator<List<Message>> partition = Iterators.partition(messageIterator, 5000);

            AtomicInteger batchesCounter = new AtomicInteger(0);

            while (partition.hasNext()) {
                List<Message> next = partition.next();
                executorService.execute(() -> {
                    logger.debug("Saving a batch of messages {}/{} for chat {}",
                            batchesCounter.getAndAdd(next.size()), messages.size(), chat.getName());
                    messagesService.loadMessages(next);
                });
            }
        }
    }

    public static int getWordsNumber(String content) {
        String trimmed = content.trim();
        if (trimmed.isEmpty()) {
            return 0;
        }
        return trimmed.split("[.,;\\s?!)(\"]+").length;
    }

    public static String[] getWordsSplit(String content) {
        String trimmed = content.trim();
        if (trimmed.isEmpty()) {
            return new String[]{};
        }
        return trimmed.split("[.,;\\s?!)(\"]+");
    }

    @SneakyThrows
    private Map<Integer, User> loadUsers(ZipFile file, ZipEntry entry, Date updateTime) {
        try (InputStream stream = file.getInputStream(entry)) {
            ArrayNode userNodes = (ArrayNode) OBJECT_MAPPER.readTree(stream);
            List<User> users = new ArrayList<>();
            for (JsonNode user : userNodes) {
                UserDto userDto = OBJECT_MAPPER.treeToValue(user, UserDto.class);
                users.add(User.of(userDto, updateTime));
            }
            return userService.updateUsers(users);
        }
    }
}
