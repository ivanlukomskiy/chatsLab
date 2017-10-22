package com.ivanlukomskiy.chatsLab.model.dto;

import com.ivanlukomskiy.chatsLab.model.Gender;
import lombok.AllArgsConstructor;
import lombok.Data;

import java.math.BigInteger;

/**
 * Created by ivanl <ilukomskiy@sbdagroup.com> on 20.10.2017.
 */
@Data
@AllArgsConstructor
public class UserToWords {

    private int id;
    private String firstName;
    private String lastName;
    private Gender gender;
    private long words;
    private long messages;
    private double density;

    public UserToWords(Object[] obj) {
        id = (Integer)obj[0];
        firstName = (String)obj[1];
        lastName = (String)obj[2];
        gender = Gender.valueOf((String)obj[3]);
        words = ((BigInteger)obj[4]).longValue();
        messages = ((BigInteger)obj[5]).longValue();
        density = ((Long)words).doubleValue()/messages;
    }
}
