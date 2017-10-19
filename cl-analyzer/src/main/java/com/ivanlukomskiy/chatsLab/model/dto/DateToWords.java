package com.ivanlukomskiy.chatsLab.model.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.math.BigInteger;

/**
 * Created by ivanl <ilukomskiy@sbdagroup.com> on 19.10.2017.
 */
@Data
public class DateToWords {
    private String formattedDate;
    private long words;

    public DateToWords(Object[] obj) {
        formattedDate = (String)obj[0];
        words = ((BigInteger)obj[1]).longValue();
    }
}
