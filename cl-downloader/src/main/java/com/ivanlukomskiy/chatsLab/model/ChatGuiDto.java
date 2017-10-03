package com.ivanlukomskiy.chatsLab.model;

import lombok.Data;

/**
 * Represents a chat which can be downloaded
 * Created by ivanl <ilukomskiy@sbdagroup.com> on 30.09.2017.
 */
@Data
public class ChatGuiDto {

    public ChatGuiDto(int id, String name, Integer adminId) {
        this.id = id;
        this.name = name;
        this.adminId = adminId;
    }

    final int id;
    final String name;
    final Integer adminId;

    /**
     * True in case user marked this chat to be downloaded
     */
    boolean download = false;
}
