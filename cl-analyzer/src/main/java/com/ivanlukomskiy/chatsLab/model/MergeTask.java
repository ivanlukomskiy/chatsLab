package com.ivanlukomskiy.chatsLab.model;

import lombok.Data;

import javax.persistence.*;

/**
 * Created by ivanl <ilukomskiy@sbdagroup.com> on 25.10.2018.
 */
@Data
@Entity
@Table(name = "cl_merge_tasks")
public class MergeTask {

    @Id
    @GeneratedValue
    private Integer id;

    @Column(nullable = false, name = "telegram_username")
    private String telegramUsername;

    @Column(nullable = false)
    private Integer vkId;

    @Column(nullable = false)
    private Integer providerId;
}
