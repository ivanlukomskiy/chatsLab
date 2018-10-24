package com.ivanlukomskiy.chatsLab.service;

import com.ivanlukomskiy.chatsLab.model.*;
import com.ivanlukomskiy.chatsLab.repository.*;
import lombok.SneakyThrows;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.codehaus.jackson.JsonNode;
import org.codehaus.jackson.node.ArrayNode;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.transaction.support.TransactionTemplate;

import java.io.IOException;
import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.regex.Pattern;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

import static com.ivanlukomskiy.chatsLab.service.GatheringService.getWordsNumber;
import static com.ivanlukomskiy.chatsLab.util.JacksonUtils.OBJECT_MAPPER;
import static java.util.Optional.ofNullable;

/**
 * Created by ivanl <ilukomskiy@sbdagroup.com> on 24.10.2018.
 */
@Component
public class TelegramService {
    private static final Logger logger = LogManager.getLogger(GatheringJob.class);
    private static final SimpleDateFormat DATE_FORMAT = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:SS");
    private static final Random R = new Random();

    @Autowired
    private NameFpRepository nameFpRepository;

    @Autowired
    private ChatPositionFpRepository chatPositionFpRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private ChatRepository chatRepository;

    @Autowired
    private MessageRepository messageRepository;

    @Autowired
    private TransactionTemplate transactionTemplate;

    @Value("${chats-lab.sources-dirs}")
    private String sourcesDirs;

    public void loadChats(ZipFile file, ZipEntry entry, Date downloadDate, Pack pack)
            throws IOException {
        Map<String, User> usersMap = new HashMap<>();
        try (InputStream stream = file.getInputStream(entry)) {
            ArrayNode telegramData = (ArrayNode) OBJECT_MAPPER.readTree(stream);
            telegramData.forEach(chat -> loadChat(chat, downloadDate, usersMap, pack));
        }
    }

    @SneakyThrows
    private void loadChat(JsonNode chatNode, Date downloadDate, Map<String, User> usersMap, Pack pack) {

        Map<String, ChatPositionFingerprint> fingerprints = new HashMap<>();
        Set<NameFingerprint> nameFingerprints = new HashSet<>();

        String chatName = chatNode.get("name").asText();

        logger.debug("Loading telegram chat " + chatName);

        Chat chat;

        List<Chat> duplicates = chatRepository.findByNameAndSource(chatName, MessageSource.TELEGRAM);

        // todo for now suppose that if chats have identical names they are the same although it's not always true
        long startWritingPosition = 0;
        if (duplicates.size() > 1) {
            throw new IllegalStateException("Found more than one duplicate chat for name " + chatName);
        } else if (duplicates.size() == 1) {
            chat = duplicates.get(0);
            chat.setUpdateTime(downloadDate.after(chat.getUpdateTime()) ? downloadDate : chat.getUpdateTime());
            chat.getPacks().add(pack);
            startWritingPosition = ofNullable(messageRepository.findMaxTelegramId(chat.getId())).orElse(0L);
        } else {
            chat = new Chat();
            chat.setName(chatName);
            chat.setSource(MessageSource.TELEGRAM);
            chat.setUpdateTime(downloadDate);
            chat.getPacks().add(pack);
            chat = chatRepository.save(chat);
        }

        List<Message> messages = new ArrayList<>();

        for (JsonNode messageNode : chatNode.get("messages")) {
            if (!messageNode.has("from") || !messageNode.has("type")
                    || !"message".equals(messageNode.get("type").asText())
                    || !messageNode.has("text")) {
                continue;
            }
            long telegramId = messageNode.get("id").asLong();
            String from = messageNode.get("from").asText();

            User user;
            if (!usersMap.containsKey(from)) {
                user = new User();
                user.setId(-Math.abs(R.nextInt()));
                user.setFirstName(from);
                user.setLastName("");
                user.setUpdated(downloadDate);
                userRepository.save(user);
                usersMap.put(from, user);

                NameFingerprint fingerprint = new NameFingerprint();
                fingerprint.setSuspect(user);
                fingerprint.setProvider(pack.getProvider());
                fingerprint.setTelegramName(from);
                nameFingerprints.add(fingerprint);

            } else {
                user = usersMap.get(from);
            }

            if (!fingerprints.containsKey(from)) {
                ChatPositionFingerprint fingerprint = new ChatPositionFingerprint();
                fingerprint.setChat(chat);
                fingerprint.setPosition(telegramId);
                fingerprint.setSuspect(user);
                fingerprints.put(from, fingerprint);
            }

            if (telegramId <= startWritingPosition) {
                continue;
            }

            String content = textNodeToString(messageNode.get("text"));

            Message message = new Message();
            message.setContent(content);
            message.setTime(DATE_FORMAT.parse(messageNode.get("date").asText()));
            message.setPack(pack);
            message.setWordsNumber(getWordsNumber(content));
            message.setChat(chat);
            message.setSender(user);
            message.setTelegramId(telegramId);
            messages.add(message);
            chat.setMessagesCount(chat.getMessagesCount() + 1);
        }

        logger.debug("Saving {} messages, {} pos fp, {} name fp",
                messages.size(), fingerprints.size(), nameFingerprints.size());
        chatRepository.save(chat);
        chatPositionFpRepository.save(fingerprints.values());
        nameFpRepository.save(nameFingerprints);
        messageRepository.save(messages);
    }

    private String textNodeToString(JsonNode node) {
        if (node.isTextual()) return node.asText();
        if (node.isArray()) {
            ArrayNode elemArr = (ArrayNode) node;
            StringBuilder sb = new StringBuilder();
            for (JsonNode elem : elemArr) {
                if (elem.isTextual()) {
                    sb.append(elem.asText());
                } else if (elem.isObject() && elem.has("text")) {
                    sb.append(elem.get("text").asText());
                }
            }
            return sb.toString();
        }
        throw new RuntimeException("Invalid node " + node.toString() + "; " + node.getClass().getCanonicalName());
    }
}
