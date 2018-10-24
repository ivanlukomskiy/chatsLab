package com.ivanlukomskiy.chatsLab.model;

import lombok.Data;

import javax.persistence.*;

/**
 * Created by ivanl <ilukomskiy@sbdagroup.com> on 24.10.2018.
 */
@Entity
@Data
@Table(name = "cl_chat_position_fingerprints")
public class ChatPositionFingerprint {

    @Id
    @GeneratedValue
    private int id;

    @ManyToOne
    @JoinColumn(name = "chat", nullable = false)
    private Chat chat;

    @Column(nullable = false)
    private Long position;

    @ManyToOne
    @JoinColumn(name = "suspect", nullable = false)
    private User suspect;
}
