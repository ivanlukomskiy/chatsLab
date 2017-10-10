package com.ivanlukomskiy.chatsLab.service;

import lombok.SneakyThrows;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.regex.Pattern;

/**
 * Created by ivanl <ilukomskiy@sbdagroup.com> on 07.10.2017.
 */
@Component
public class GatheringJob implements Job {
    private static final Logger logger = LogManager.getLogger(GatheringJob.class);
    private static final Pattern MESSAGES_PACK_PATTERN = Pattern.compile("chats\\d+\\.zip");

    @Autowired
    private GatheringService gatheringService;

    @Value("${chats-lab.sources-dir}")
    private String sourcesDir;

    @PostConstruct
    @SneakyThrows
    public void run() {
        logger.info("Import process started with sources dir \"{}\"", sourcesDir);
        Files.list(Paths.get(sourcesDir))
                .filter(path -> MESSAGES_PACK_PATTERN.matcher(path.getFileName().toString()).matches())
                .filter(path -> {
                    if (!gatheringService.isLoaded(path)) {
                        logger.info("Loading new package {}", path.getFileName());
                        return true;
                    } else {
                        logger.debug("Package {} is already loaded", path.getFileName());
                        return false;
                    }
                }).forEach(gatheringService::loadPack);
    }

    @Override
    public int getPriority() {
        return 100;
    }

    @Override
    public String getDescription() {
        return "Update messages packs";
    }
}
