package com.ivanlukomskiy.chatsLab.model.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

/**
 * Created by ivanl <ilukomskiy@sbdagroup.com> on 30.09.2017.
 */
@Data
@AllArgsConstructor
public class MessageDto {
    public long id;
    public int author;
    public int timestamp;
    public String content;
}
