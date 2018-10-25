package com.ivanlukomskiy.chatsLab.service;

import com.ivanlukomskiy.chatsLab.model.Chat;
import com.ivanlukomskiy.chatsLab.model.MessageSource;
import com.ivanlukomskiy.chatsLab.repository.ChatPositionFpRepository;
import com.ivanlukomskiy.chatsLab.repository.ChatRepository;
import lombok.SneakyThrows;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.support.TransactionCallbackWithoutResult;
import org.springframework.transaction.support.TransactionTemplate;

import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Created by ivanl <ilukomskiy@sbdagroup.com> on 09.10.2017.
 */
@Service
public class ChatService {
    private static final Logger logger = LogManager.getLogger(ChatService.class);
    private NumberFormat PERCENT_FORMAT = NumberFormat.getPercentInstance();

    @Autowired
    private ChatRepository chatRepository;

    @Autowired
    private TransactionTemplate transactionTemplate;

    public void save(Chat chat) {
        chatRepository.save(chat);
    }

    @SneakyThrows
    public void mergeAll() {
        logger.info("Merge session started. Searching for chats with duplicates...");
        List<Object[]> resultSet = chatRepository.getDuplicateChatNames();
        List<String> chatNamesWithDuplicates = new ArrayList<>();
        for (Object[] obj : resultSet) {
            Long repeats = (Long) obj[1];
            if (repeats == 1) {
                continue;
            }
            chatNamesWithDuplicates.add((String) obj[0]);
        }

        if (chatNamesWithDuplicates.size() == 0) {
            logger.info("No duplicates found, nothing to merge");
            return;
        } else {
            logger.info("{} chats with duplicates found", chatNamesWithDuplicates.size());
        }

        ExecutorService executor = Executors.newFixedThreadPool(4);

        AtomicInteger currentChat = new AtomicInteger(0);
        for (String chatName : chatNamesWithDuplicates) {
            executor.submit(() -> {
                logger.info("Merging chat {}/{} with names {}", currentChat.incrementAndGet(), chatNamesWithDuplicates.size(),
                        chatName);
                List<Chat> chatsToMerge = chatRepository.findByNameAndSource(chatName, MessageSource.VK);
                for (int i = chatsToMerge.size() - 2; i >= 0; i--) {
                    mergeTwoChats(chatsToMerge.get(i + 1).getId(), chatsToMerge.get(i).getId());
                }
            });
        }

        executor.shutdown();
        executor.awaitTermination(1, TimeUnit.DAYS);

        logger.info("All chats merged");
    }

    private void mergeTwoChats(int sourceId, int targetId) {
        logger.debug("Start merging chat {} to chat {}", sourceId, targetId);
        transactionTemplate.execute(new TransactionCallbackWithoutResult() {
            @Override
            protected void doInTransactionWithoutResult(TransactionStatus status) {

                Integer cropped = chatRepository.cropChat(sourceId, targetId);
                Integer moved = chatRepository.move(sourceId, targetId);
                chatRepository.updateChatMessagesNumber(targetId);
                chatRepository.updateChatWordsNumber(targetId);

                Chat source = chatRepository.findOne(sourceId);
                Chat target = chatRepository.findOne(targetId);
                target.getPacks().addAll(source.getPacks());
                target.setUpdateTime(new Date());
                chatRepository.save(target);
                chatRepository.delete(sourceId);

                logger.debug("Merge finished; increment: {}, cropped: {}, moved: {}",
                        PERCENT_FORMAT.format((moved.doubleValue()) / (cropped + moved))
                        , cropped, moved);
            }
        });
    }
}
