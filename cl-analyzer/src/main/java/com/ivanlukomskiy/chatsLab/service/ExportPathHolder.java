package com.ivanlukomskiy.chatsLab.service;

import lombok.Getter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.io.File;
import java.nio.file.Path;
import java.nio.file.Paths;

import static java.lang.String.valueOf;

/**
 * Created by ivanl <ilukomskiy@sbdagroup.com> on 19.10.2017.
 */
@Service
public class ExportPathHolder {

    @Getter
    private File exportDir;

    @Value("${chats-lab.output-dir}")
    private String outputDir;

    @PostConstruct
    public void createExportDir() {
        Path outputRoot = Paths.get(outputDir);
        exportDir = outputRoot.resolve(valueOf(System.currentTimeMillis())).toFile();
        if (!exportDir.exists()) {
            boolean created = exportDir.mkdirs();
            if (!created) {
                throw new IllegalArgumentException("Failed to create export folder " + exportDir);
            }
        }
    }
}
