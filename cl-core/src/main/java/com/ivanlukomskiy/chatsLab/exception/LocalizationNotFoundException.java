package com.ivanlukomskiy.chatsLab.exception;

public class LocalizationNotFoundException extends RuntimeException {
    public LocalizationNotFoundException(String resource, String path, String locale) {
        super("Localization not found for path " + path + " and locale " + locale + " at resource " + resource);
    }
}
