package com.ivanlukomskiy.chatsLab.service;

import com.ivanlukomskiy.chatsLab.model.dto.ChatDto;
import com.ivanlukomskiy.chatsLab.model.dto.MessageDto;
import com.ivanlukomskiy.chatsLab.model.dto.UserDto;

import java.util.Map;

/**
 * Created by ivanl <ilukomskiy@sbdagroup.com> on 30.09.2017.
 */
public interface Dumper {
    void startWriting(ChatDto chat);
    void writeMessage(MessageDto message);
    void finishWriting();
    void writeUsers(Map<Integer, UserDto> users);
    void finalize();
}
