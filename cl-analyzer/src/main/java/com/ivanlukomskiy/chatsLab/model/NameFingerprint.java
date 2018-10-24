package com.ivanlukomskiy.chatsLab.model;

import lombok.Data;

import javax.persistence.*;

/**
 * Created by ivanl <ilukomskiy@sbdagroup.com> on 24.10.2018.
 */
@Entity
@Data
@Table(name = "cl_name_fingerprints")
public class NameFingerprint {

    @Id
    @GeneratedValue
    private int id;

    @ManyToOne
    @JoinColumn(name = "provider", nullable = false)
    private User provider;

    @Column(nullable = false)
    private String telegramName;

    @ManyToOne
    @JoinColumn(name = "suspect", nullable = false)
    private User suspect;
}
