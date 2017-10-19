package com.ivanlukomskiy.chatsLab.model.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.math.BigInteger;

/**
 * Created by ivanl <ilukomskiy@sbdagroup.com> on 20.10.2017.
 */
@Data
@AllArgsConstructor
public class UserToWords {

    private final int id;
    private final String firstName;
    private final String lastName;
    private final long words;
    private final long messages;
    private final double density;

    public UserToWords(Object[] obj) {
        id = (Integer)obj[0];
        firstName = (String)obj[1];
        lastName = (String)obj[2];
        words = ((BigInteger)obj[3]).longValue();
        messages = ((BigInteger)obj[4]).longValue();
        density = ((Long)words).doubleValue()/messages;
    }
}
