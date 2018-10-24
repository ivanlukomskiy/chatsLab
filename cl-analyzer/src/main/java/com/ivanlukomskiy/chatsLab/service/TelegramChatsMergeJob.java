package com.ivanlukomskiy.chatsLab.service;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Set;

/**
 * Created by ivanl <ilukomskiy@sbdagroup.com> on 24.10.2018.
 */
@Component
public class TelegramChatsMergeJob implements Job {
    private static final Logger logger = LogManager.getLogger(GatheringJob.class);

    @Autowired
    private TelegramChatsMerger merger;

    @Override
    public void run() {
        logger.info("Merging by position...");
        merger.removeOutdatedUsers(merger.mergeByPositions());
        logger.info("Merging by name...");
        merger.removeOutdatedUsers(merger.mergeByNames());
    }

    @Override
    public int getPriority() {
        return 150;
    }

    @Override
    public String getDescription() {
        return "Merging telegram chats";
    }
}
