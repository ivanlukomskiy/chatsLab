package com.ivanlukomskiy.chatsLab.service;

import com.ivanlukomskiy.chatsLab.model.pack.ChatDto;
import com.ivanlukomskiy.chatsLab.model.pack.MessageDto;
import com.ivanlukomskiy.chatsLab.model.pack.UserDto;

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
    void finalize();
}
