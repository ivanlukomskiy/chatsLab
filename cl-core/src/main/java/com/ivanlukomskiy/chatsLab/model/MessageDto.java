package com.ivanlukomskiy.chatsLab.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Message details
 * Created by ivanl <ilukomskiy@sbdagroup.com> on 30.09.2017.
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class MessageDto {
    public long id;
    public int author;
    public int timestamp;
    public String content;
}
