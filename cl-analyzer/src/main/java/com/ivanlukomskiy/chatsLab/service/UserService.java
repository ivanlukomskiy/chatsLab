package com.ivanlukomskiy.chatsLab.service;

import com.ivanlukomskiy.chatsLab.model.User;
import com.ivanlukomskiy.chatsLab.model.UserDto;
import com.ivanlukomskiy.chatsLab.repository.UserRepository;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Created by ivanl <ilukomskiy@sbdagroup.com> on 09.10.2017.
 */
@Service
public class UserService {
    private static final Logger logger = LogManager.getLogger(UserService.class);


    @Autowired
    private UserRepository userRepository;

    /**
     * Inserts users to database in case they don't exist or update contains more recent data
     *
     * @param users
     */
    public Map<Integer, User> updateUsers(List<User> users) {
        List<Integer> ids = users.stream().map(User::getId).collect(Collectors.toList());
        List<User> existed = userRepository.findExistUsers(ids);

        Map<Integer, User> existedById = existed.stream().collect(Collectors.toMap(User::getId, user -> user));

        logger.debug("Users total: {}, exists: {} ", users.size(), existedById.size());

        List<User> toUpdate = users.stream().filter(user -> !existedById.containsKey(user.getId())
                || existedById.get(user.getId()).getUpdated().getTime() < user.getUpdated().getTime())
                .collect(Collectors.toList());
        if (!toUpdate.isEmpty()) {
            userRepository.save(toUpdate);
            logger.info("Updated {} users", toUpdate.size());
        } else {
            logger.info("No users to update");
        }

        for (User user : toUpdate) {
            existedById.put(user.getId(), user);
        }
        return existedById;
    }

    public User getById(Integer id) {
        return userRepository.findOne(id);
    }
}
