package com.ivanlukomskiy.chatsLab.service;

import com.ivanlukomskiy.chatsLab.model.Chat;
import com.ivanlukomskiy.chatsLab.repository.ChatRepository;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionCallbackWithoutResult;
import org.springframework.transaction.support.TransactionTemplate;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Created by ivanl <ilukomskiy@sbdagroup.com> on 09.10.2017.
 */
@Service
public class ChatService {
    private static final Logger logger = LogManager.getLogger(ChatService.class);

    @Autowired
    private ChatRepository chatRepository;

    @Autowired
    private TransactionTemplate transactionTemplate;

    public void save(Chat chat) {
        chatRepository.save(chat);
    }

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

        if(chatNamesWithDuplicates.size() == 0) {
            logger.info("No duplicates found, nothing to merge");
            return;
        } else {
            logger.info("{} chats with duplicates found",chatNamesWithDuplicates.size());
        }
        int currentChat=0;
        for (String chatName : chatNamesWithDuplicates) {
            currentChat++;
            logger.info("Merging chat {}/{} with names {}", currentChat, chatNamesWithDuplicates.size(),
                    chatName);
            List<Chat> chatsToMerge = chatRepository.findByName(chatName);
            for (int i = chatsToMerge.size() - 2; i >= 0; i--) {
                mergeTwoChats(chatsToMerge.get(i + 1).getId(), chatsToMerge.get(i).getId());
            }
        }
        logger.info("All chats merged");
    }

    private void mergeTwoChats(int sourceId, int targetId) {
        logger.debug("Start merging chat {} to chat {}", sourceId, targetId);
        transactionTemplate.execute(new TransactionCallbackWithoutResult() {
            @Override
            protected void doInTransactionWithoutResult(TransactionStatus status) {
                chatRepository.mergeChats(sourceId, targetId);
                chatRepository.clearChatMessages(sourceId);
                chatRepository.updateChatMessagesNumber(targetId);
                chatRepository.updateChatWordsNumber(targetId);

                Chat source = chatRepository.findOne(sourceId);
                Chat target = chatRepository.findOne(targetId);
                target.getPacks().addAll(source.getPacks());
                target.setUpdateTime(new Date());
                chatRepository.save(target);

                chatRepository.delete(sourceId);
            }
        });
        logger.debug("Merge finished");
    }
}
