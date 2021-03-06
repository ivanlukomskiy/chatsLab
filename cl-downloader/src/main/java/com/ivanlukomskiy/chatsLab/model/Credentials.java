package com.ivanlukomskiy.chatsLab.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Represents data needs to be sent to VK API in order to authenticate
 * Created by ivanl <ilukomskiy@sbdagroup.com> on 30.09.2017.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Credentials {
    private Integer id;
    private String password;
}
