package com.ivanlukomskiy.chatsLab.service;

import java.io.IOException;

/**
 * Created by ivanl <ilukomskiy@sbdagroup.com> on 10.10.2017.
 */
public interface Job {
    void run();
    int getPriority();
    String getDescription();
}
