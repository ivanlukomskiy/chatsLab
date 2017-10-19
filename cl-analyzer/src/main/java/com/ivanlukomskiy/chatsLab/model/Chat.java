package com.ivanlukomskiy.chatsLab.model;

import lombok.Data;

import javax.persistence.*;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * Created by ivanl <ilukomskiy@sbdagroup.com> on 07.10.2017.
 */
@Data
@Entity
@Table(name = "cl_chats")
public class Chat {
    @Id
    @GeneratedValue
    private int id;
    @Column(nullable = false)
    private String name;
    @ManyToOne
    @JoinColumn(name = "admin_id", nullable = false)
    private User admin;
    @Column(name = "update_time", nullable = false)
    private Date updateTime;
    @Column(name = "messages_number", nullable = false)
    private int messagesCount;
    @Column(name = "words_number", nullable = false)
    private int wordsNumber;
    @ManyToMany
    @JoinTable(
            name = "cl_chat_to_pack",
            joinColumns = @JoinColumn(name = "chat_id", referencedColumnName = "id"),
            inverseJoinColumns = @JoinColumn(name = "pack_id", referencedColumnName = "uuid"))
    private List<Pack> packs = new ArrayList<>();
}
