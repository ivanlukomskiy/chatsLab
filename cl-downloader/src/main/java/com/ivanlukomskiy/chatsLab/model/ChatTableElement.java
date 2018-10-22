package com.ivanlukomskiy.chatsLab.model;

/**
 * Created by ivanl <ilukomskiy@sbdagroup.com> on 22.10.2018.
 */
public interface ChatTableElement {
    String getName();

    boolean isDownload();

    void setDownload(boolean value);
}
