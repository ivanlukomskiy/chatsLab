package com.ivanlukomskiy.chatsLab.service;

import com.ivanlukomskiy.chatsLab.model.dto.ChatNameToWords;
import com.ivanlukomskiy.chatsLab.model.dto.UserToWords;
import lombok.SneakyThrows;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.File;
import java.text.DecimalFormat;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static java.io.File.separator;

/**
 * Created by ivanl <ilukomskiy@sbdagroup.com> on 20.10.2017.
 */
@Component
public class UserStatisticsJob implements Job {
    private static final Logger logger = LogManager.getLogger(UserStatisticsJob.class);
    private static final DecimalFormat FORMAT_DENSITY = new DecimalFormat("#.#");

    private static final String WORDS_BY_USER = "words_by_user.txt";
    private static final String WORDS_BY_USER_LAST_YEAR = "words_by_user_last_year.csv";
    private static final String USERS_BY_DENSITY = "users_by_density.csv";
    private static final String ACTIVE_USERS_BY_YEARS = "active_users_by_years.csv";

    private static final int MESSAGES_PER_YEAR_THRESHOLD = 300;

    @Autowired
    private ExportPathHolder exportPathHolder;

    @Autowired
    private MessagesService messagesService;

    @Autowired
    private OverallStatisticsJob overallStatisticsJob;

    @Override
    @SneakyThrows
    public void run() {
        File exportDir = exportPathHolder.getExportDir();
        File wordsByChatsFile = new File(exportDir + separator + WORDS_BY_USER);
        File wordsByChatsLastYearFile = new File(exportDir + separator + WORDS_BY_USER_LAST_YEAR);
        File usersByDensityFile = new File(exportDir + separator + USERS_BY_DENSITY);
        File activeUsersByYearsFile = new File(exportDir + separator + ACTIVE_USERS_BY_YEARS);

        logger.info("Writing words by users...");
        List<UserToWords> wordsByChats = messagesService.getWordsByUser();
        try (ClWriter writer = new ClWriter(wordsByChatsFile)) {
            wordsByChats.forEach(wbu -> writer.write(
                    wbu.getWords(), wbu.getFirstName() + " " + wbu.getLastName()));
        }

        logger.info("Writing words by users last year...");
        List<UserToWords> wordsByChatsLastYear = messagesService.getWordsByUserLastYear();
        try (ClWriter writer = new ClWriter(wordsByChatsLastYearFile)) {
            wordsByChatsLastYear.forEach(wbu -> writer.write(wbu.getWords(), wbu.getFirstName() + " "
                    + wbu.getLastName()));
        }

        double yearPredictionMultiplier = overallStatisticsJob.getYearPredictionMultiplier();

        logger.info("Writing active users words density...");
        List<Integer> activeLastYear = wordsByChatsLastYear.stream()
                .filter(user -> user.getMessages() * yearPredictionMultiplier > MESSAGES_PER_YEAR_THRESHOLD)
                .map(UserToWords::getId)
                .collect(Collectors.toList());

        wordsByChats.sort(Comparator.comparing(UserToWords::getDensity));
        try (ClWriter writer = new ClWriter(usersByDensityFile)) {
            wordsByChats.stream()
                    .filter(wbu -> activeLastYear.contains(wbu.getId()))
                    .forEach(wbu -> writer.write(FORMAT_DENSITY.format(wbu.getDensity()),
                            wbu.getFirstName() + " " + wbu.getLastName()));
        }

        logger.info("Calculating active users by years...");
        List<Map.Entry<Integer,List<UserToWords>>> yearToUserPair
                = new ArrayList<>(messagesService.getWordsByUserAndYears().entrySet());
        yearToUserPair.sort(Comparator.comparing(Map.Entry::getKey));

        try (ClWriter writer = new ClWriter(activeUsersByYearsFile)) {
            int count = 0;
            for (Map.Entry<Integer, List<UserToWords>> yearAndUsers : yearToUserPair) {
                Integer year = yearAndUsers.getKey();

                Stream<Long> usersStream = yearAndUsers.getValue().stream()
                        .map(UserToWords::getMessages);

                if (count == yearToUserPair.size() - 1) {
                    usersStream = usersStream.map(i -> Math.round(yearPredictionMultiplier * i));
                }

                usersStream = usersStream.filter(messagesCount -> messagesCount > MESSAGES_PER_YEAR_THRESHOLD);
                Long active = usersStream.count();

                writer.write(year, active);

                count++;
            }
        }

    }

    @Override
    public int getPriority() {
        return 3000;
    }

    @Override
    public String getDescription() {
        return "Calculate users statistics";
    }
}
