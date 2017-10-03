package com.ivanlukomskiy.chatsLab.util;

import com.google.common.io.Resources;
import com.ivanlukomskiy.chatsLab.exception.LocalizationNotFoundException;
import com.ivanlukomskiy.chatsLab.exception.LocalizationPlaceholderException;
import lombok.SneakyThrows;
import org.codehaus.jackson.JsonNode;

import java.io.IOException;
import java.net.URL;
import java.nio.charset.StandardCharsets;

import static com.ivanlukomskiy.chatsLab.util.JacksonUtils.OBJECT_MAPPER;

/**
 * Created by ivanl <ilukomskiy@sbdagroup.com> on 04.10.2017.
 */

public class Localization {
    private final JsonNode localization;
    private final String locale;
    private final String resourcePath;

    @SneakyThrows
    public Localization(String resourcePath, String locale) {
        this.locale = locale;
        this.resourcePath = resourcePath;
        URL url = Resources.getResource(resourcePath);
        String text = Resources.toString(url, StandardCharsets.UTF_8);
        localization = OBJECT_MAPPER.readTree(text);
    }

    public String getText(String path) {
        String[] split = path.split("\\.");
        JsonNode currentNode = localization;
        for (String aSplit : split) {
            if (!currentNode.has(aSplit)) {
                throw new LocalizationNotFoundException(resourcePath, path, locale);
            }
            currentNode = currentNode.get(aSplit);
        }
        if (!currentNode.has(locale)) {
            throw new LocalizationNotFoundException(resourcePath, path, locale);
        }
        return currentNode.get(locale).asText();
    }

    public String getText(String path, Object... placeholders) {
        String text = getText(path);
        for(Object placeholder : placeholders) {
            if(!text.contains("{}")) {
                throw new LocalizationPlaceholderException(getText(path), placeholders.length);
            }
            text = text.replaceFirst("\\{}",placeholder.toString());
        }
        return text;
    }
}
