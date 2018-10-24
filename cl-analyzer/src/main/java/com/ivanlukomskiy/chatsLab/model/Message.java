package com.ivanlukomskiy.chatsLab.model;

import lombok.Data;

import javax.persistence.*;
import java.util.Date;

/**
 * Created by ivanl <ilukomskiy@sbdagroup.com>+ on 07.10.2017.
 */
@Data
@Entity
@Table(name = "cl_messages",
        indexes = @Index(name = "cl_messages_index", columnList = "sender_id,time,chat_id"))
public class Message {

    @Id
    @GeneratedValue
    private int id;

    @Column(nullable = false)
    private Date time;

    @ManyToOne
    @JoinColumn(name = "sender_id", nullable = false)
    private User sender;

    @Column(nullable = false)
    private int wordsNumber;

    @Column(length = 10485760, nullable = false)
    private String content;

    @Column
    private Long telegramId;

    @ManyToOne
    @JoinColumn(name = "messages_pack_id", nullable = false)
    private Pack pack;

    @ManyToOne
    @JoinColumn(name = "chat_id", nullable = false)
    private Chat chat;
}
