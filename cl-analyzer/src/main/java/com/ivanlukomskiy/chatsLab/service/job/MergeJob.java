package com.ivanlukomskiy.chatsLab.service.job;

import com.ivanlukomskiy.chatsLab.service.dataAccess.ChatService;
import com.ivanlukomskiy.chatsLab.service.Job;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * Created by ivanl <ilukomskiy@sbdagroup.com> on 10.10.2017.
 */
@Component
public class MergeJob implements Job {

    @Autowired
    private ChatService chatService;

    @Override
    public void run() {
        chatService.mergeAll();
    }

    @Override
    public int getPriority() {
        return 200;
    }

    @Override
    public String getDescription() {
        return "Merging chats";
    }
}
