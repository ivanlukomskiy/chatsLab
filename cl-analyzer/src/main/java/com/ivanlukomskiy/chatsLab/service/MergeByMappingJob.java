package com.ivanlukomskiy.chatsLab.service;

import com.ivanlukomskiy.chatsLab.model.MergeTask;
import com.ivanlukomskiy.chatsLab.model.User;
import com.ivanlukomskiy.chatsLab.repository.MergeTaskRepository;
import com.ivanlukomskiy.chatsLab.repository.UserRepository;
import lombok.SneakyThrows;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by ivanl <ilukomskiy@sbdagroup.com> on 25.10.2018.
 */
@Component
public class MergeByMappingJob implements Job {

    @Autowired
    private MergeTaskRepository mergeTaskRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private TelegramChatsMerger merger;

    @Override
    @Transactional
    @SneakyThrows
    public void run() {
        List<MergeTask> tasks = mergeTaskRepository.findAll();
        for (MergeTask task : tasks) {
            merger.mergeByMapping(task.getVkId(), task.getTelegramUsername(), task.getProviderId());
        }
        mergeTaskRepository.deleteAll();
    }

    @Override
    public int getPriority() {
        return 120;
    }

    @Override
    public String getDescription() {
        return "Merge by mapping";
    }
}
