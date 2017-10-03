package com.ivanlukomskiy.chatsLab.exception;

public class LocalizationPlaceholderException extends RuntimeException {
    public LocalizationPlaceholderException(String text, int expectedNumber) {
        super("Not enough placeholders in text " + text + "; expected: " + expectedNumber);
    }
}
