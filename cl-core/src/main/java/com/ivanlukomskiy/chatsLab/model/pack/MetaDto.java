package com.ivanlukomskiy.chatsLab.model.pack;

import lombok.Data;

import java.util.Date;
import java.util.UUID;

/**
 * Overall info about messages pack
 * Created by ivanl <ilukomskiy@sbdagroup.com> on 06.10.2017.
 */
@Data
public class MetaDto {
    /**
     * Id of account chats are downloaded from
     */
    private int providerId;

    /**
     * Earlier message date
     */
    private Date earlierDate;

    /**
     * Latest message date
     */
    private Date latestDate;

    /**
     * Messages pack download time
     */
    private Date downloadDate;

    private int messagesTotal;
    private int chatsTotal;
    private int usersNumber;

    /**
     * Messages pack ID
     */
    private UUID uuid;

    /**
     * Version of downloader was used
     */
    private String version;
}
