package com.ivanlukomskiy.chatsLab.exception;

/**
 * Created by ivanl <ilukomskiy@sbdagroup.com> on 04.10.2017.
 */
public class LocalizationNotFoundException extends RuntimeException {
    public LocalizationNotFoundException(String resource, String path, String locale) {
        super("Localization not found for path " + path + " and locale " + locale + " at resource " + resource);
    }
}
