package com.ivanlukomskiy.chatsLab.model;

import lombok.Data;

import javax.persistence.*;
import java.util.Date;

/**
 * Created by ivanl <ilukomskiy@sbdagroup.com> on 07.10.2017.
 */
@Entity
@Data
@Table(name = "cl_users")
public class User {
    @Id
    private int id;
    @Column(nullable = false)
    private String firstName;
    @Column(nullable = false)
    private String lastName;
    @Column(nullable = false)
    private Date updated;
    @Column
    @Enumerated(value = EnumType.STRING)
    private Gender gender;

    public static User of(UserDto userDto, Date updated) {
        User user = new User();
        user.setId(userDto.getId());
        user.setFirstName(userDto.getFirstName());
        user.setLastName(userDto.getLastName());
        user.setUpdated(updated);
        return user;
    }
}
