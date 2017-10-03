package com.ivanlukomskiy.chatsLab.model.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;

/**
 * Created by ivanl <ilukomskiy@sbdagroup.com> on 30.09.2017.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ChatDto {
    private int id;
    private String name;
    private Date downloadTime;
    private Integer adminId;
}
