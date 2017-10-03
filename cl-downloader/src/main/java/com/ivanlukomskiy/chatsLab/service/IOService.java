package com.ivanlukomskiy.chatsLab.service;

import com.ivanlukomskiy.chatsLab.model.Credentials;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.awt.*;
import java.io.File;
import java.io.IOException;

import static com.ivanlukomskiy.chatsLab.util.JacksonUtils.OBJECT_MAPPER;

/**
 * Singleton object which provides helper IO methods
 * Created by ivanl <ilukomskiy@sbdagroup.com> on 30.09.2017.
 */
public enum IOService {
    INSTANCE;

    private static final Logger logger = LogManager.getLogger(IOService.class);
    private static final File CREDENTIALS_FILE = new File("auth.data");
    public static final File DOWNLOADS_FOLDER = new File("downloads");

    private Credentials credentials = null;

    public Integer getId() {
        return credentials == null ? null : credentials.getId();
    }

    public String getPassword() {
        return credentials == null ? null : credentials.getPassword();
    }

    public void setCredentials(Credentials credentials) {
        this.credentials = credentials;
    }

    public void serialize() throws IOException {
        OBJECT_MAPPER.writeValue(CREDENTIALS_FILE, credentials);
    }

    public void deserialize() throws IOException {
        logger.debug("Trying to load credentials...");
        try {
            credentials = OBJECT_MAPPER.readValue(CREDENTIALS_FILE, Credentials.class);
            logger.info("Credentials loaded");
        } catch (IOException e) {
            logger.info("Failed to load credentials: " + e.getMessage());
            throw e;
        }
    }

    public void openDownloadsFolder() {
        try {
            Desktop.getDesktop().open(DOWNLOADS_FOLDER);
        } catch (Exception e) {
            logger.error("Failed to open downloads folder", e);
        }
    }
}
