package com.ivanlukomskiy.chatsLab.service;

import com.ivanlukomskiy.chatsLab.model.Gender;
import com.ivanlukomskiy.chatsLab.model.User;
import com.ivanlukomskiy.chatsLab.model.dto.DateToWords;
import com.ivanlukomskiy.chatsLab.model.dto.UserToWords;
import com.ivanlukomskiy.chatsLab.model.json.PointOnTime;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.SneakyThrows;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.codehaus.jackson.map.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.io.File;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.*;
import java.util.concurrent.atomic.AtomicLong;
import java.util.stream.Collectors;

import static com.google.common.collect.Sets.difference;
import static com.google.common.collect.Sets.intersection;
import static com.ivanlukomskiy.chatsLab.service.OverallStatisticsJob.DATE_FORMAT;
import static com.ivanlukomskiy.chatsLab.service.OverallStatisticsJob.toDate;
import static java.io.File.separator;
import static java.util.Comparator.comparing;
import static java.util.Comparator.reverseOrder;

/**
 * Created by ivanl <ilukomskiy@sbdagroup.com> on 20.10.2017.
 */
@Component
public class UserStatisticsJob implements Job {
    private static final Logger logger = LogManager.getLogger(UserStatisticsJob.class);
    public static final DecimalFormat FORMAT_DENSITY = new DecimalFormat("#.#",
            new DecimalFormatSymbols(Locale.US));
    public static final DecimalFormat FORMAT_GENDER_PART = new DecimalFormat("#.###",
            new DecimalFormatSymbols(Locale.US));

    private static final String WORDS_BY_USER = "words_by_user.txt";
    private static final String WORDS_BY_USER_LAST_YEAR = "words_by_user_last_year.csv";
    private static final String USERS_BY_DENSITY = "users_by_density.csv";
    private static final String ACTIVE_USERS_BY_YEARS = "active_users_by_years.csv";
    private static final String GENDER_BALANCE_BY_YEARS = "gender_balance_by_years.csv";
    private static final String ACTIVE_USERS_BALANCE_BY_YEARS = "active_users_balance_by_years.csv";
    private static final String TOP_ACTIVE_USERS_BY_YEARS = "top_active_users_by_years.csv";
    private static final String NEW_USERS_RATING = "newUsersRating.csv";
    private static final String PARTICIPANT = "participant%user.csv";
    private static final String PARTICIPANT_JSON = "participant%user.json";
    private static final String ACTIVE_USERS_STANDING = "standing.csv";
    private static final String WORDS_DENSITY_THIS_YEAR = "words_density_this_year.csv";

    @Value("${chats-lab.active-threshold-year}")
    private int activeThresholdYear = 300;

    @Value("${chats-lab.participant-ids}")
    private String participantIdsString;

    @Autowired
    private ExportPathHolder exportPathHolder;

    @Autowired
    private MessagesService messagesService;

    @Autowired
    private OverallStatisticsJob overallStatisticsJob;

    @Autowired
    private UserService userService;

    @Override
    @SneakyThrows
    public void run() {
        File exportDir = exportPathHolder.getExportDir();
        File wordsByChatsFile = new File(exportDir + separator + WORDS_BY_USER);
        File wordsByChatsLastYearFile = new File(exportDir + separator + WORDS_BY_USER_LAST_YEAR);
        File usersByDensityFile = new File(exportDir + separator + USERS_BY_DENSITY);
        File activeUsersByYearsFile = new File(exportDir + separator + ACTIVE_USERS_BY_YEARS);
        File genderBalanceByYearsFile = new File(exportDir + separator + GENDER_BALANCE_BY_YEARS);
        File activeUsersBalanceByYearsFile = new File(exportDir + separator + ACTIVE_USERS_BALANCE_BY_YEARS);
        File topActiveUsersByYears = new File(exportDir + separator + TOP_ACTIVE_USERS_BY_YEARS);
        File newUsersRatingFile = new File(exportDir + separator + NEW_USERS_RATING);
        File activeUsersStandingFile = new File(exportDir + separator + ACTIVE_USERS_STANDING);
        File wordsDensityThisYear = new File(exportDir + separator + WORDS_DENSITY_THIS_YEAR);


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

        if (!participantIdsString.isEmpty()) {
            String[] participantIds = participantIdsString.split(",");
            for (String participantId : participantIds) {
                Integer id = Integer.valueOf(participantId);
                List<DateToWords> userActivityByMonths = messagesService.getUserActivityByMonths(id);
                userActivityByMonths.sort(comparing(DateToWords::getFormattedDate, reverseOrder()));
                userActivityByMonths = userActivityByMonths.subList(0, 12);
                userActivityByMonths.sort(Comparator.comparing(DateToWords::getFormattedDate));
                User user = userService.getById(id);
                try (ClWriter writer = new ClWriter(new File(exportDir + separator
                        + PARTICIPANT.replace("%user", user.getFirstName() + "_" + user.getLastName())))) {
                    int count = 0;
                    for (DateToWords dateToWords : userActivityByMonths) {
                        writer.write(
                                FORMAT_GENDER_PART.format(Long.valueOf(dateToWords.getWords()).doubleValue() / 1000));
                        count++;
                        if (count == 12) break;
                    }
                }
            }


            for (String participantId : participantIds) {
                List<PointOnTime> points = new ArrayList<>();
                Integer id = Integer.valueOf(participantId);
                List<DateToWords> userActivityByMonths = messagesService.getUserActivityByMonths(id);
                userActivityByMonths.sort(Comparator.comparing(DateToWords::getFormattedDate));
                User user = userService.getById(id);
                for (DateToWords dateToWords : userActivityByMonths) {
                    points.add(new PointOnTime(toDate(dateToWords.getFormattedDate()), dateToWords.getWords()));
                }

                File participantsJson = new File(exportDir + separator + PARTICIPANT_JSON.replace("%user", user.getLastName()));

                ObjectMapper objectMapper = new ObjectMapper();
                objectMapper.setDateFormat(DATE_FORMAT);
                objectMapper.writeValue(participantsJson, points);
            }
        }

        double yearPredictionMultiplier = overallStatisticsJob.getYearPredictionMultiplier();

        logger.info("Writing active users words density...");
        List<Integer> activeLastYear = wordsByChatsLastYear.stream()
                .filter(user -> user.getMessages() * yearPredictionMultiplier > activeThresholdYear)
                .map(UserToWords::getId)
                .collect(Collectors.toList());

        wordsByChats.sort(comparing(UserToWords::getDensity));
        try (ClWriter writer = new ClWriter(usersByDensityFile)) {
            wordsByChats.stream()
                    .filter(wbu -> activeLastYear.contains(wbu.getId()))
                    .forEach(wbu -> writer.write(FORMAT_DENSITY.format(wbu.getDensity()),
                            wbu.getFirstName() + " " + wbu.getLastName()));
        }

        logger.info("Calculating active users by years...");
        List<Map.Entry<Integer, List<UserToWords>>> yearToUserPair
                = new ArrayList<>(messagesService.getWordsByUserAndYears().entrySet());
        yearToUserPair.sort(comparing(Map.Entry::getKey));

        try (ClWriter writerGender = new ClWriter(genderBalanceByYearsFile);
             ClWriter writerActive = new ClWriter(activeUsersByYearsFile);
             ClWriter writerUsersBalance = new ClWriter(activeUsersBalanceByYearsFile);
             ClWriter writerTopActiveUsers = new ClWriter(topActiveUsersByYears);
             ClWriter newUsersRating = new ClWriter(newUsersRatingFile)) {

            Set<Integer> lastYearActiveUserIds = new HashSet<>();

            // Calculate best debute
            Integer currentYear = yearToUserPair.get(yearToUserPair.size() - 1).getKey();
            Map<String, Long> userToCountYearsBeforeNow = new HashMap<>();
            Map<String, Long> userToCountThisYear = new HashMap<>();
            for (Map.Entry<Integer, List<UserToWords>> yearAndUsers : yearToUserPair) {
                if (yearAndUsers.getKey().equals(currentYear)) {
                    for (UserToWords userToWords : yearAndUsers.getValue()) {
                        userToCountThisYear.put(userToWords.getNameAndSurname(), userToWords.getWords());
                    }
                } else {
                    List<UserToWords> userToWords = yearAndUsers.getValue();
                    for (UserToWords userToWord : userToWords) {
                        Long wordsCount = userToCountYearsBeforeNow.getOrDefault(userToWord.getId(), 0L);
                        wordsCount += userToWord.getWords();
                        userToCountYearsBeforeNow.put(userToWord.getNameAndSurname(), wordsCount);
                    }
                }

            }
            List<WordsBeforeThisYearAndCurrent> newUsersDetails = new ArrayList<>();
            userToCountThisYear.forEach((nameAndSurname, wordsThisYear) -> {
                Long wordsBeforeThisYear = userToCountYearsBeforeNow.get(nameAndSurname);
                if (wordsBeforeThisYear == null) {
                    wordsBeforeThisYear = 0L;
                }
                if (wordsBeforeThisYear.doubleValue() < wordsThisYear / 5) {
                    newUsersDetails.add(new WordsBeforeThisYearAndCurrent(
                            nameAndSurname, wordsBeforeThisYear, wordsThisYear));
                }
            });
            newUsersDetails.sort(comparing(WordsBeforeThisYearAndCurrent::getCurrent, reverseOrder()));
            for (WordsBeforeThisYearAndCurrent newUsersDetail : newUsersDetails) {
                newUsersRating.write(newUsersDetail.nameAndSurname,
                        newUsersDetail.before,
                        newUsersDetail.current);
            }

            Set<Integer> currentYearActiveUsersIds = new HashSet<>();

            for (Map.Entry<Integer, List<UserToWords>> yearAndUsers : yearToUserPair) {
                Integer year = yearAndUsers.getKey();

                // Create set with active users for the considered year
                Set<UserToWords> activeUsers = new HashSet<>();
                Set<Integer> thisYearActiveUserIds = new HashSet<>();
                int userCount = 0;
                for (UserToWords userToWords : yearAndUsers.getValue()) {
                    // Increase last year user activity to expected value
                    if (userCount == yearAndUsers.getValue().size() - 1) {
                        userToWords.setMessages(Math.round(yearPredictionMultiplier * userToWords.getMessages()));
                    }
                    if (userToWords.getMessages() < activeThresholdYear) {
                        continue;
                    }
                    activeUsers.add(userToWords);
                    thisYearActiveUserIds.add(userToWords.getId());
                    userCount++;
                }

                if (Objects.equals(yearAndUsers.getKey(), currentYear)) {
                    currentYearActiveUsersIds.addAll(thisYearActiveUserIds);
                }

                // Write gender statistics
                AtomicLong males = new AtomicLong(0L);
                AtomicLong females = new AtomicLong(0L);
                AtomicLong total = new AtomicLong(0L);
                activeUsers.forEach(user -> {
                    total.incrementAndGet();
                    if (user.getGender() == Gender.MALE) {
                        males.incrementAndGet();
                    } else if (user.getGender() == Gender.FEMALE) {
                        females.incrementAndGet();
                    }
                });
                Double malesPercent = males.doubleValue() / (females.longValue() + males.longValue());
                writerGender.write(year, FORMAT_GENDER_PART.format(malesPercent),
                        FORMAT_GENDER_PART.format(1 - malesPercent));

                // Write active users by years with income/outcome
                int stayedActive = intersection(lastYearActiveUserIds, thisYearActiveUserIds).size();
                int newUsers = difference(thisYearActiveUserIds, lastYearActiveUserIds).size();
                int leavesActive = difference(lastYearActiveUserIds, thisYearActiveUserIds).size();
                writerUsersBalance.write(year, stayedActive, newUsers, leavesActive, (stayedActive + newUsers));
                lastYearActiveUserIds = thisYearActiveUserIds;

                // Write top active users by years
                List<UserToWords> topActive = new ArrayList<>(activeUsers);
                topActive.sort(comparing(UserToWords::getWords, reverseOrder()));
                writerTopActiveUsers.write(year);
                Long max = null;
                for (int i = 0; i < 10 && i < topActive.size(); i++) {
                    UserToWords user = topActive.get(i);
                    if (max == null) {
                        max = user.getWords();
                    }
                    writerTopActiveUsers.write(user.getId(),
                            FORMAT_DENSITY.format((double) user.getWords() * 100 / max),
                            user.getFirstName() + " " + user.getLastName());
                }

                // Write active users by year
                writerActive.write(year, total);
            }

            logger.info("Writing active users analysis");
            try (ClWriter writer = new ClWriter(activeUsersStandingFile)) {
                Map<Integer, Long> yearToUsersNumber = currentYearActiveUsersIds.stream()
                        .map(messagesService::getFirstMessageDate)
                        .map(date -> {
                            Calendar cal = Calendar.getInstance();
                            cal.setTime(date);
                            return cal.get(Calendar.YEAR);
                        }).collect(Collectors.groupingBy(i -> i, Collectors.counting()));
                yearToUsersNumber.forEach((year, user) -> {
                    writer.write(year, user);
                });
            }

            logger.info("Writing words density this year");
            try (ClWriter writer = new ClWriter(wordsDensityThisYear)) {
                List<UserToDensity> usersAndDensities = new ArrayList<>();
                for (Integer userId : currentYearActiveUsersIds) {
                    User user = userService.getById(userId);
                    double density = messagesService.getWordsDensityThisYear(userId);
                    usersAndDensities.add(new UserToDensity(user, density));
                }
                usersAndDensities.sort(Comparator.comparing(UserToDensity::getDensity));
                usersAndDensities.forEach(uad -> writer.write(uad.getUser().toString(), uad.getDensity()));
            }
        }
    }

    @Data
    @AllArgsConstructor
    class UserToDensity {
        User user;
        double density;
    }

    @Data
    @AllArgsConstructor
    private class WordsBeforeThisYearAndCurrent {
        String nameAndSurname;
        long before;
        long current;
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
