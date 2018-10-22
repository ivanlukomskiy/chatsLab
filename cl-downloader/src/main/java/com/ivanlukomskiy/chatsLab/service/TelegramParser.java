package com.ivanlukomskiy.chatsLab.service;

import org.codehaus.jackson.JsonNode;
import org.codehaus.jackson.node.ArrayNode;

import java.io.IOException;
import java.util.List;
import java.util.Set;

/**
 * Created by ivanl <ilukomskiy@sbdagroup.com> on 22.10.2018.
 */
public interface TelegramParser {

    List<JsonNode> getChats(String filePath) throws IOException;
}
