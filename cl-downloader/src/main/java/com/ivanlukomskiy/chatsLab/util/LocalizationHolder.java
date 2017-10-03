package com.ivanlukomskiy.chatsLab.util;

/**
 * Provides access to current localization
 * Created by ivanl <ilukomskiy@sbdagroup.com> on 04.10.2017.
 */
public class LocalizationHolder {
    public static final String LOCALIZATION_RESOURCE = "localization.json";
    public static Localization localization = new Localization(LOCALIZATION_RESOURCE,"ru");
}
