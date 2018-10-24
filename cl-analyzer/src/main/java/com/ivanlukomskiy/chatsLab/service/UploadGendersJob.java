package com.ivanlukomskiy.chatsLab.service;

import com.ivanlukomskiy.chatsLab.model.Gender;
import lombok.SneakyThrows;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.Set;

/**
 * Fixes backward compatibility for downloader versions before 2.1
 * Created by ivanl <ilukomskiy@sbdagroup.com> on 21.10.2017.
 */
//@Component
public class UploadGendersJob implements Job {
    private static final Logger logger = LogManager.getLogger(UploadGendersJob.class);

    @Autowired
    private UserService userService;

    @Override
    @SneakyThrows
    public void run() {
        Set<Integer> usersWithNotResolvedGender = userService.findUsersWithNotResolvedGender();
        if (usersWithNotResolvedGender.isEmpty()) {
            logger.info("Gender update is not needed");
            return;
        }
        logger.info("Begin resolving gender for {} users", usersWithNotResolvedGender.size());
        Map<Integer, Gender> resolved = GenderResolver.resolve(usersWithNotResolvedGender);
        userService.updateUsersGender(resolved);
        logger.info("User genders successfully updated");
    }

    @Override
    public int getPriority() {
        return 300;
    }

    @Override
    public String getDescription() {
        return "Update users gender";
    }
}
