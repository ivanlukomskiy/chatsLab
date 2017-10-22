package com.ivanlukomskiy.chatsLab.service.dataAccess;

import com.ivanlukomskiy.chatsLab.model.Gender;
import com.ivanlukomskiy.chatsLab.model.User;
import com.ivanlukomskiy.chatsLab.model.dto.DateToWords;
import com.ivanlukomskiy.chatsLab.model.dto.UserToWords;
import com.ivanlukomskiy.chatsLab.repository.UserRepository;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Created by ivanl <ilukomskiy@sbdagroup.com> on 09.10.2017.
 */
@Service
public class UserService {
    private static final Logger logger = LogManager.getLogger(UserService.class);

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private MessagesService messagesService;

    @Value("${chats-lab.active-threshold-overall}")
    private static final int activeThresholdOverall = 300;

    /**
     * Inserts users to database in case they don't exist or update contains more recent data
     */
    @Transactional
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

    private Map<Integer, DateToWords> userToWordsByYears;

//    public void loadUsersToWordsByYear() {
//        // Select all users and filter out inactive
//        List<UserToWords> wordsByUser = messagesService.getWordsByUser()
//                .stream()
//                .filter(utw -> utw.getMessages() > activeThresholdOverall)
//                .collect(Collectors.toList());
//
//        // Request activity by year for each user
//        for (UserToWords userToWords : wordsByUser) {
//            messagesService.getWordsByUserAndYears()
//
//
//        }
//
//    }

    public Set<Integer> findUsersWithNotResolvedGender() {
        return userRepository.findUsersWithNotResolvedGender();
    }

    @Transactional
    public void updateUsersGender(Map<Integer, Gender> idToGender) {
        Set<Integer> males = new HashSet<>();
        Set<Integer> females = new HashSet<>();
        Set<Integer> unknown = new HashSet<>();

        idToGender.forEach((id, gender) -> {
            switch (gender) {
                case MALE:
                    males.add(id);
                    break;
                case FEMALE:
                    females.add(id);
                    break;
                default:
                    unknown.add(id);
            }
        });

        if (!males.isEmpty()) {
            userRepository.updateSetMale(males);
        }
        if (!females.isEmpty()) {
            userRepository.updateSetFemale(females);
        }
        if (!unknown.isEmpty()) {
            userRepository.updateSetUnknown(unknown);
        }
    }

    public User getById(Integer id) {
        return userRepository.findOne(id);
    }
}
