package com.ivanlukomskiy.chatsLab.service;

import com.ivanlukomskiy.chatsLab.model.Chat;
import com.ivanlukomskiy.chatsLab.model.ChatPositionFingerprint;
import com.ivanlukomskiy.chatsLab.model.NameFingerprint;
import com.ivanlukomskiy.chatsLab.model.User;
import com.ivanlukomskiy.chatsLab.repository.ChatPositionFpRepository;
import com.ivanlukomskiy.chatsLab.repository.MessageRepository;
import com.ivanlukomskiy.chatsLab.repository.NameFpRepository;
import com.ivanlukomskiy.chatsLab.repository.UserRepository;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;
import java.util.*;
import java.util.function.Supplier;
import java.util.stream.Collectors;

import static java.util.Comparator.comparingLong;
import static java.util.stream.Collectors.groupingBy;
import static java.util.stream.Collectors.joining;

/**
 * Created by ivanl <ilukomskiy@sbdagroup.com> on 24.10.2018.
 */
@Component
public class TelegramChatsMerger {
    private static final Logger logger = LogManager.getLogger(TelegramChatsMerger.class);

    @Autowired
    private ChatPositionFpRepository chatPositionFpRepository;

    @Autowired
    private MessageRepository messageRepository;

    @Autowired
    private NameFpRepository nameFpRepository;

    @Autowired
    private UserRepository userRepository;

    @Transactional
    public void mergeByNames() {
        Set<Integer> outdatedFps = new HashSet<>();

        List<NameFingerprint> fps = nameFpRepository.findAll();

        Supplier<TreeSet<NameFingerprint>> supplier = () -> new TreeSet<>(comparingLong(fp -> fp.getSuspect().getId()));

        Map<String, TreeSet<NameFingerprint>> fpsByName =
                fps.stream().collect(groupingBy(NameFingerprint::getTelegramName, Collectors.toCollection(supplier)));

        fpsByName.forEach((name, nameFps) -> {
            if (nameFps.size() > 1) {
                List<NameFingerprint> fpsList = new ArrayList<>(nameFps);
                logger.debug("There are {} to merge by name {}", fpsList.size(), name);
                for (int i = 0; i < fpsList.size() - 1; i++) {
                    NameFingerprint from = fpsList.get(i);
                    NameFingerprint to = fpsList.get(i + 1);
                    if (from.getSuspect().getId() == to.getSuspect().getId()) {
                        continue;
                    }
                    outdatedFps.add(fpsList.get(i).getId());
                    merge(fpsList.get(i).getSuspect().getId(), fpsList.get(i + 1).getSuspect().getId());
                }
            }
        });

        List<NameFingerprint> positionFingerprintsToDelete
                = nameFpRepository.findAll(outdatedFps);
        nameFpRepository.delete(positionFingerprintsToDelete);
    }

    @Transactional
    public void removeOutdatedUsers(Set<Integer> ids) {
        List<User> usersToDelete = userRepository.findAll(ids);
        logger.info("Users to delete: " + ids.stream().map(String::valueOf).collect(joining(", ")));
        userRepository.delete(usersToDelete);
    }

    @Transactional
    public void mergeByPositions() {
        Set<Integer> outdatedFps = new HashSet<>();

        List<ChatPositionFingerprint> fps = chatPositionFpRepository.findAll();
        Map<Chat, List<ChatPositionFingerprint>> fingerprintsByChats
                = fps.stream().collect(groupingBy(ChatPositionFingerprint::getChat));

        fingerprintsByChats.forEach((chat, chatFps) -> {
            Map<Long, List<ChatPositionFingerprint>> fpByPosition
                    = chatFps.stream().collect(groupingBy(ChatPositionFingerprint::getPosition));
            fpByPosition.forEach((index, indFps) -> {
                if (indFps.size() > 1) {
                    logger.debug("There are {} to merge in chat {}, index is {}", indFps.size(), chat.getName(), index);
                    String names = indFps.stream().map(fp -> fp.getSuspect().getFirstName()).collect(Collectors.joining(", "));
                    logger.debug("Suspects {} seem to be identical", names);
                    for (int i = 0; i < indFps.size() - 1; i++) {
                        outdatedFps.add(indFps.get(i).getId());
                        merge(indFps.get(i).getSuspect().getId(), indFps.get(i + 1).getSuspect().getId());
                    }
                }
            });
        });

        List<ChatPositionFingerprint> positionFingerprintsToDelete
                = chatPositionFpRepository.findAll(outdatedFps);
        chatPositionFpRepository.delete(positionFingerprintsToDelete);
    }

    private User getUserRecursive(Integer id) {
        return getUserRecursive(id, new HashSet<>());
    }

    private User getUserRecursive(Integer id, Set<Integer> visitedIds) {
        User user = userRepository.getOne(id);
        if(visitedIds.contains(user.getId())) {
            throw new RuntimeException("Circular merging, id " + id);
        }
        if(user.getMergedTo() != null) {
            visitedIds.add(id);
            return getUserRecursive(user.getMergedTo(), visitedIds);
        }
        return user;
    }

    private boolean merge(Integer fromId, Integer toId) {
        User from = getUserRecursive(fromId);
        User to = getUserRecursive(toId);
        if(from.getId() > to.getId()) {
            User tmp = from;
            from = to;
            to = tmp;
        }

        if (from.getId() == to.getId()) {
            logger.warn("Merging identical ids {}", from.getId());
            return false;
        }

        messageRepository.changeMessagesSender(from.getId(), to.getId());
        chatPositionFpRepository.changeSuspect(from.getId(), to.getId());
        nameFpRepository.changeSuspect(from.getId(), to.getId());
        from.setMergedTo(to.getId());
        userRepository.save(from);
        logger.info("Merged {} to {}", from, to);
        return true;
    }

    @Transactional
    public User mergeByMapping(Integer vkId, String telegramName, Integer providerId) throws IOException {
        User to = userRepository.findOne(vkId);
        if (to == null) {
            throw new RuntimeException("Failed to find user with id " + vkId);
        }
        logger.info("get users by {}", telegramName);
        List<User> matches = userRepository.getTelegramUser(telegramName, providerId);
        if (matches.size() == 0) {
            throw new RuntimeException("No matches for " + telegramName);
        }
        if (matches.size() > 1) {
            throw new RuntimeException("Too many matches for " + telegramName);
        }
        User from = matches.get(0);

        logger.debug("Merging {} to {}", from, to);
        merge(from.getId(), to.getId());

        return from;
    }
}
