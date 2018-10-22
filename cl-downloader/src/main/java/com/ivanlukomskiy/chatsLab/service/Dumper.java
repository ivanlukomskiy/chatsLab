package com.ivanlukomskiy.chatsLab.service;

import com.ivanlukomskiy.chatsLab.model.ChatDto;
import com.ivanlukomskiy.chatsLab.model.MessageDto;
import com.ivanlukomskiy.chatsLab.model.UserDto;
import org.codehaus.jackson.JsonNode;

import java.io.IOException;
import java.util.List;
import java.util.Map;

/**
 * Provides methods to dump messages to disk
 * Created by ivanl <ilukomskiy@sbdagroup.com> on 30.09.2017.
 */
public interface Dumper {
    void prepare();
    void startWriting(ChatDto chat);
    void writeMessage(MessageDto message);
    void finishWriting();
    void writeUsers(Map<Integer, UserDto> users);
    void writeMetaInfo(int providerId);
    void writeTelegramChats(List<JsonNode> chats);
    void finalize();
}
