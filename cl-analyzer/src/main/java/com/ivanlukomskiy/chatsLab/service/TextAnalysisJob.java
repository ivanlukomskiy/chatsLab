package com.ivanlukomskiy.chatsLab.service;

import com.ivanlukomskiy.chatsLab.model.Message;
import com.ivanlukomskiy.chatsLab.model.User;
import com.ivanlukomskiy.chatsLab.repository.MessageRepository;
import com.vdurmont.emoji.EmojiManager;
import lombok.SneakyThrows;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Component;

import java.io.File;
import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.regex.Pattern;

import static java.io.File.separator;
import static java.util.regex.Pattern.CASE_INSENSITIVE;
import static java.util.regex.Pattern.compile;

/**
 * Created by ivanl <ilukomskiy@sbdagroup.com> on 27.10.2017.
 */
@Component
public class TextAnalysisJob implements Job {
    private static final Logger logger = LogManager.getLogger(TextAnalysisJob.class);

    @Autowired
    private MessagesService messagesService;

    @Autowired
    private ExportPathHolder exportPathHolder;
    @Autowired
    private MessageRepository messageRepository;

    private static final String AHAHA_FILENAME = "ahaha_pattern.txt";
    private static final String EMOJI_FILENAME = "emoji_pattern.txt";

    private Date startYear;

    @Override
    @SneakyThrows
    public void run() {

        Date last = messageRepository.getMaxDate();
        Calendar cal = Calendar.getInstance();
        cal.setTime(last);
        cal.roll(Calendar.YEAR, -1);
        startYear = cal.getTime();

        Page<Message> messages = messagesService.getByPage(0);
        for (int i = 0; i < messages.getTotalPages(); i++) {
            logger.info("Querying page {} of {}", i, messages.getTotalPages());
            parseMessagesPage(messagesService.getByPage(i));
        }

        logger.info("All parsed");
        File exportDir = exportPathHolder.getExportDir();
        File ahahaFile = new File(exportDir + separator + AHAHA_FILENAME);
        try (ClWriter writer = new ClWriter(ahahaFile)) {
            List<Map.Entry<User, Integer>> ahaha = new LinkedList<>(patterntToUserToCount.get("ahaha").entrySet());
            ahaha.sort(Comparator.comparing(Map.Entry::getValue, Comparator.reverseOrder()));
            ahaha.forEach(entry -> {
                writer.write(entry.getKey().getFirstName(), entry.getKey().getLastName(), entry.getValue());
            });
        }
        File emojiFile = new File(exportDir + separator + EMOJI_FILENAME);
        try (ClWriter writer = new ClWriter(emojiFile)) {
            List<Map.Entry<User, Integer>> ahaha = new LinkedList<>(patterntToUserToCount.get("emoji").entrySet());
            ahaha.sort(Comparator.comparing(Map.Entry::getValue, Comparator.reverseOrder()));
            ahaha.forEach(entry -> {
                writer.write(entry.getKey().getFirstName(), entry.getKey().getLastName(), entry.getValue());
            });
        }
    }

    private Map<String, Map<User, Integer>> patterntToUserToCount = new HashMap<>();

    private void parseMessagesPage(Page<Message> messages) {
        for (Message message : messages) {
            if (message.getTime().before(startYear)) {
                continue;
            }

            String[] split = GatheringService.getWordsSplit(message.getContent());
            for (String word : split) {
                ahahaPattern(word, message.getSender());
            }
        }
    }

    private static final Pattern AHAHA = compile("^([ахпф]+)|([ah]+)$", CASE_INSENSITIVE);
    private static final Pattern LOL = compile("(^л[ао]+л)|(l[oa]+l)$", CASE_INSENSITIVE);
    private static final Pattern KEK = compile("^к[е]+к$", CASE_INSENSITIVE);


    private void ahahaPattern(String word, User user) {
        String trimmed = word.trim();
        if (matcherAhaha(trimmed) || matcherKek(trimmed) || matcherLol(trimmed)) {
            Map<User, Integer> ahaha = patterntToUserToCount.getOrDefault("ahaha", new HashMap<>());
            Integer count = ahaha.getOrDefault(user, 0);
            count += 1;
            ahaha.put(user, count);
            patterntToUserToCount.put("ahaha", ahaha);
        } else if (hasEmoji(trimmed)) {
            Map<User, Integer> emojies = patterntToUserToCount.getOrDefault("emoji", new HashMap<>());
            Integer count = emojies.getOrDefault(user, 0);
            count += 1;
            emojies.put(user, count);
            patterntToUserToCount.put("emoji", emojies);
        }
    }

    private static boolean matcherAhaha(String word) {
        return ((word.contains("х") && word.contains("а"))
                || (word.contains("h") && word.contains("a")))
                && AHAHA.matcher(word).matches();
    }

    private static boolean matcherLol(String word) {
        return LOL.matcher(word).matches();
    }

    private static boolean matcherKek(String word) {
        return KEK.matcher(word).matches();
    }

    private static final Pattern EMOJI_REGEX = compile("([:=]'?-?[()|PРOD]+)|(\\([:=])|([()]+)");

    private static boolean hasEmoji(String word) {
        return EmojiManager.isEmoji(word) || EMOJI_REGEX.matcher(word).matches();
    }

    @Override
    public int getPriority() {
        return 10000;
    }

    @Override
    public String getDescription() {
        return "Text analysing job";
    }
}
