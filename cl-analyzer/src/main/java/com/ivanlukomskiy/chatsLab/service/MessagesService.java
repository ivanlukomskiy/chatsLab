package com.ivanlukomskiy.chatsLab.service;

import com.ivanlukomskiy.chatsLab.model.Message;
import com.ivanlukomskiy.chatsLab.repository.MessageRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * Created by ivanl <ilukomskiy@sbdagroup.com> on 09.10.2017.
 */
@Service
public class MessagesService {

    @Autowired
    private MessageRepository messageRepository;

    public void loadMessages(List<Message> messages) {
        messageRepository.save(messages);
    }
}
