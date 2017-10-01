package com.ivanlukomskiy.chatsLab.model;

import lombok.Data;

/**
 * Created by ivanl <ilukomskiy@sbdagroup.com> on 30.09.2017.
 */
@Data
public class ChatGuiDto {
    public ChatGuiDto(int id, String name) {
        this.id = id;
        this.name = name;
    }

    final int id;
    final String name;
    boolean download = false;
}
