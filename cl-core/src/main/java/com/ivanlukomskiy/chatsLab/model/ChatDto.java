package com.ivanlukomskiy.chatsLab.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Overall chat info
 * Created by ivanl <ilukomskiy@sbdagroup.com> on 30.09.2017.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ChatDto {
    private int id;
    private String name;
    private Integer adminId;
}
