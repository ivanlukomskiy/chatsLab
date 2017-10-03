package com.ivanlukomskiy.chatsLab.exception;

/**
 * Created by ivanl <ilukomskiy@sbdagroup.com> on 04.10.2017.
 */
public class LocalizationPlaceholderException extends RuntimeException {
    public LocalizationPlaceholderException(String text, int expectedNumber) {
        super("Not enough placeholders in text " + text + "; expected: " + expectedNumber);
    }
}
