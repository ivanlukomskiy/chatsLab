package com.ivanlukomskiy.chatsLab.model;

import lombok.Data;

import javax.persistence.*;
import java.util.Date;
import java.util.UUID;

/**
 * Created by ivanl <ilukomskiy@sbdagroup.com> on 07.10.2017.
 */
@Data
@Entity
@Table(name = "cl_packs")
public class Pack {
    @Id
    private String uuid;
    @ManyToOne
    @JoinColumn(nullable = false, name = "provider_id")
    private User provider;
    @Column(name = "download_time", nullable = false)
    private Date downloadTime;
    @Column(nullable = false)
    private String version;
}
