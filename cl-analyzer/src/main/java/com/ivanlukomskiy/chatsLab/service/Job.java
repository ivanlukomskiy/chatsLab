package com.ivanlukomskiy.chatsLab.service;

/**
 * Created by ivanl <ilukomskiy@sbdagroup.com> on 10.10.2017.
 */
public interface Job {
    void run();
    int getPriority();
    String getDescription();
}
