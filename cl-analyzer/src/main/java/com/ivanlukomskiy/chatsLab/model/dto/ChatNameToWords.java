package com.ivanlukomskiy.chatsLab.model.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.math.BigInteger;

/**
 * Created by ivanl <ilukomskiy@sbdagroup.com> on 19.10.2017.
 */
@Data
public class ChatNameToWords {
    private String chatName;
    private long words;

    public ChatNameToWords(Object[] obj) {
        chatName = (String)obj[0];
        words = ((BigInteger)obj[1]).longValue();
    }
}
