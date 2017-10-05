package com.ivanlukomskiy.chatsLab.model.pack;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * User details
 * Created by ivanl <ilukomskiy@sbdagroup.com> on 30.09.2017.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class UserDto {
    private Integer id;
    private String firstName;
    private String lastName;
}
