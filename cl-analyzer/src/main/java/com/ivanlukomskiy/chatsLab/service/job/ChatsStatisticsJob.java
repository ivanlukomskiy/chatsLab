package com.ivanlukomskiy.chatsLab.service.job;

import com.ivanlukomskiy.chatsLab.model.dto.ChatNameToWords;
import com.ivanlukomskiy.chatsLab.service.ClWriter;
import com.ivanlukomskiy.chatsLab.service.ExportPathHolder;
import com.ivanlukomskiy.chatsLab.service.Job;
import com.ivanlukomskiy.chatsLab.service.dataAccess.MessagesService;
import lombok.SneakyThrows;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.File;
import java.util.List;

import static java.io.File.separator;

/**
 * Created by ivanl <ilukomskiy@sbdagroup.com> on 19.10.2017.
 */
@Component
public class ChatsStatisticsJob implements Job {
    private static final Logger logger = LogManager.getLogger(ChatsStatisticsJob.class);

    private static final String WORDS_BY_CHATS = "words_by_chats.txt";
    private static final String WORDS_BY_CHATS_LAST_YEAR = "words_by_chats_last_year.csv";

    @Autowired
    private ExportPathHolder exportPathHolder;

    @Autowired
    private MessagesService messagesService;

    @Override
    @SneakyThrows
    public void run() {
        File exportDir = exportPathHolder.getExportDir();
        File wordsByChatsFile = new File(exportDir + separator + WORDS_BY_CHATS);
        File wordsByChatsLastYearFile = new File(exportDir + separator + WORDS_BY_CHATS_LAST_YEAR);

        logger.info("Writing words by chats...");
        List<ChatNameToWords> wordsByChats = messagesService.getWordsByChats();
        try (ClWriter writer = new ClWriter(wordsByChatsFile)) {
            wordsByChats.forEach(wbc -> writer.write(wbc.getWords(),wbc.getChatName()));
        }

        logger.info("Writing words by chats last year...");
        List<ChatNameToWords> wordsByChatsLastYear = messagesService.getWordsByChatLastYear();
        try (ClWriter writer = new ClWriter(wordsByChatsLastYearFile)) {
            wordsByChatsLastYear.forEach(wbc -> writer.write(wbc.getWords(),wbc.getChatName()));
        }
    }

    @Override
    public int getPriority() {
        return 2000;
    }

    @Override
    public String getDescription() {
        return "Calculating chats statistics";
    }
}
