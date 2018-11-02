package com.ivanlukomskiy.chatsLab.service;

import com.ivanlukomskiy.chatsLab.model.Chat;
import com.ivanlukomskiy.chatsLab.model.ForcedMergeTask;
import com.ivanlukomskiy.chatsLab.repository.ChatRepository;
import com.ivanlukomskiy.chatsLab.repository.ForcedMergeJobRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.Date;
import java.util.List;

/**
 * Created by ivanl <ilukomskiy@sbdagroup.com> on 27.10.2018.
 */
@Component
public class ForcedMergeJob implements Job {

    @Autowired
    private ChatService chatService;

    @Autowired
    private ChatRepository chatRepository;

    @Autowired
    private ForcedMergeJobRepository forcedMergeJobRepository;

    @Autowired
    private MessagesService messagesService;

    @Override
    @Transactional
    public void run() {
        List<ForcedMergeTask> tasks = forcedMergeJobRepository.findAll();
        tasks.forEach(task -> {
            List<Chat> from = chatRepository.findByName(task.getNameFrom());
            List<Chat> to = chatRepository.findByName(task.getNameTo());
            if (from.size() != 1) {
                throw new RuntimeException("Size for chat " + task.getNameFrom() + " is " + from.size());
            }
            if (to.size() != 1) {
                throw new RuntimeException("Size for chat " + task.getNameTo() + " is " + to.size());
            }
            int fromId = from.get(0).getId();
            int toId = to.get(0).getId();
            Date fromDate = messagesService.getLastMessageDate(fromId);
            Date toDate = messagesService.getLastMessageDate(toId);
            if (fromDate.after(toDate)) {
                chatService.mergeTwoChats(toId, fromId);
            } else {
                chatService.mergeTwoChats(fromId, toId);
            }
        });
        forcedMergeJobRepository.deleteAll();
    }

    @Override
    public int getPriority() {
        return 0;
    }

    @Override
    public String getDescription() {
        return null;
    }
}
