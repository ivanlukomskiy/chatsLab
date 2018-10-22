package com.ivanlukomskiy.chatsLab.service;

import com.ivanlukomskiy.chatsLab.model.dto.DateToWords;
import com.ivanlukomskiy.chatsLab.model.json.PointOnTime;
import lombok.Getter;
import lombok.SneakyThrows;
import org.apache.commons.lang.time.DateUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.codehaus.jackson.map.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.File;
import java.text.DateFormatSymbols;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.text.SimpleDateFormat;
import java.util.*;

import static java.io.File.separator;
import static java.lang.Math.log;
import static java.lang.Math.round;
import static java.util.Calendar.DAY_OF_WEEK;
import static java.util.Calendar.MONTH;
import static java.util.Calendar.YEAR;
import static java.util.Locale.ENGLISH;

/**
 * Created by ivanl <ilukomskiy@sbdagroup.com> on 19.10.2017.
 */
@Component
public class OverallStatisticsJob implements Job {
    private static final Logger logger = LogManager.getLogger(GatheringJob.class);

    private static final String OVERALL_STATS = "overall.txt";
    private static final String BY_YEARS = "words_by_years.csv";
    private static final String MONTHS_ACTIVITY = "months_activity.csv";
    private static final String BY_MONTHS = "words_by_months.csv";
    private static final String BY_MONTHS_JSON = "words_by_months.json";
    private static final String BY_DAYS = "words_by_days_last_year.csv";
    private static final String DAY_OF_WEEK_ACTIVITY = "day_of_week_activity.csv";

    private static final DecimalFormat FORMAT_PROPORTIONS = new DecimalFormat("#.##",
            new DecimalFormatSymbols(Locale.US));
    private static final DecimalFormat FORMAT_WORDS = new DecimalFormat("#.####",
            new DecimalFormatSymbols(Locale.US));
    public static final SimpleDateFormat DATE_FORMAT = new SimpleDateFormat("yyyy-MM-dd");

    @Autowired
    private ExportPathHolder exportPathHolder;

    @Autowired
    private MessagesService messagesService;

    @Getter
    private double yearPredictionMultiplier;

    @Override
    @SneakyThrows
    public void run() {
        File exportDir = exportPathHolder.getExportDir();
        File overallStatsFile = new File(exportDir + separator + OVERALL_STATS);
        File byYearsFile = new File(exportDir + separator + BY_YEARS);
        File monthsActivityFile = new File(exportDir + separator + MONTHS_ACTIVITY);
        File byMonthsFile = new File(exportDir + separator + BY_MONTHS);
        File byMonthsJsonFile = new File(exportDir + separator + BY_MONTHS_JSON);
        File byDaysLastYear = new File(exportDir + separator + BY_DAYS);
        File dayOfWeekActivityFile = new File(exportDir + separator + DAY_OF_WEEK_ACTIVITY);

        logger.info("Writing words by days activity last year...");
        List<DateToWords> wordsByDaysLastYear = messagesService.getWordsByDatsLastYear();
        try (ClWriter writer = new ClWriter(byDaysLastYear)) {
            wordsByDaysLastYear.forEach(date -> writer.write(date.getFormattedDate(), date.getWords()));
        }

        logger.info("Writing days of week activity");
        Map<Integer, Long> wordsByDayOfWeek = messagesService.getWordsByDayOfWeek();
        try(ClWriter writer = new ClWriter(dayOfWeekActivityFile)) {
            Long sum = wordsByDayOfWeek.values().stream().mapToLong(i -> i).sum();
            List<Map.Entry<Integer, Long>> dowToWords = new ArrayList<>(wordsByDayOfWeek.entrySet());
            dowToWords.sort(Comparator.comparing(Map.Entry::getKey));
            for (Map.Entry<Integer, Long> entry : dowToWords) {
                writer.write(entry.getKey(), FORMAT_WORDS.format(entry.getValue().doubleValue() / sum));
            }
        }

        List<DateToWords> wordsByMonths = messagesService.getWordsByMonths();

        logger.info("Writing months activity...");
        String[] monthNames = new DateFormatSymbols(ENGLISH).getMonths();
        Map<Integer, Long> monthsActivity = new HashMap<>();
        Map<Integer, Double> monthsActivityProportion = new HashMap<>();
        try (ClWriter writer = new ClWriter(monthsActivityFile)) {
            int pointer = wordsByMonths.size() - 2; // Don't count the last month
            while (pointer >= 12) {
                int iterationCounter = 0;
                while (iterationCounter < 12) {
                    iterationCounter++;
                    pointer--;

                    DateToWords dateToWords = wordsByMonths.get(pointer);
                    int monthId = extractMonthId(dateToWords.getFormattedDate());
                    Long wordsCount = monthsActivity.getOrDefault(monthId, 0L);
                    wordsCount += dateToWords.getWords();
                    monthsActivity.put(monthId, wordsCount);
                }
            }

            // Calculate months activity proportion
            long sum = monthsActivity.values().stream().mapToLong(Long::longValue).sum();
            monthsActivity.entrySet().forEach(entry -> {
                monthsActivityProportion.put(entry.getKey(), ((double) entry.getValue()) / sum);
            });

            List<Map.Entry<Integer, Double>> pairs = new ArrayList<>(monthsActivityProportion.entrySet());
            pairs.sort(Comparator.comparing(Map.Entry::getKey));
            pairs.forEach(entry -> writer.write(monthNames[entry.getKey()].substring(0, 3),
                    FORMAT_PROPORTIONS.format(entry.getValue() * 100) + "%"));
        }

        logger.info("Writing words by months...");
        List<PointOnTime> actual = new ArrayList<>();
        List<PointOnTime> prediction = new ArrayList<>();
        int wordsLastSixMonths = 0;
        long expectedWordsIncreaseThisYear;
        long lastYearWordsEquality;
        try (ClWriter writer = new ClWriter(byMonthsFile)) {
            wordsByMonths.forEach(date -> {
                writer.write(date.getFormattedDate(), date.getWords(), "accurate");
                actual.add(new PointOnTime(toDate(date.getFormattedDate()), date.getWords()));
            });
            String lastDateString = wordsByMonths.get(wordsByMonths.size() - 1).getFormattedDate();
            int lastMonthId = extractMonthId(lastDateString);
            int lastYear = Integer.valueOf(lastDateString.substring(0, 4));

            Calendar cal = Calendar.getInstance();
            cal.set(YEAR, lastYear);
            cal.set(MONTH, lastMonthId);
            cal = DateUtils.truncate(cal, MONTH);

            double weight = 0;
            for (int i = wordsByMonths.size() - 2; i > wordsByMonths.size() - 8; i--) {
                wordsLastSixMonths += wordsByMonths.get(i).getWords();
                int monthId = extractMonthId(wordsByMonths.get(i).getFormattedDate());
                weight += monthsActivityProportion.get(monthId);
            }

            // Calculate weight left
            double weightLeft = 0;
            for(int i = lastMonthId + 1; i < 12; i++) {
                weightLeft += monthsActivityProportion.get(i);
            }
            expectedWordsIncreaseThisYear = Double.valueOf(weightLeft * wordsLastSixMonths / weight).longValue();

            lastYearWordsEquality = (int) round(wordsLastSixMonths / weight);
            yearPredictionMultiplier = 1 / (1 - weightLeft);

            for (int i = 0; i < 6; i++) {
                int monthId = cal.get(MONTH);
                int year = cal.get(YEAR);
                Double expected = monthsActivityProportion.get(cal.get(MONTH)) * lastYearWordsEquality;
                writer.write(year + "-" + (monthId + 1), expected.intValue(), "prediction");
                prediction.add(new PointOnTime(cal.getTime(), expected.intValue()));
                cal.add(MONTH, 1);
            }
        }

        logger.info("Writing words by years...");
        List<DateToWords> wordsByYears = messagesService.getWordsByYears();
        try (ClWriter writer = new ClWriter(byYearsFile)) {
            long lastYearWords = 0;
            for (int i = 0 ; i < wordsByYears.size(); i++) {
                Long words = wordsByYears.get(i).getWords();
                double delta = 0D;
                Long expectedIncrease = 0L;
                if(i == wordsByYears.size() - 1) {
                    expectedIncrease = expectedWordsIncreaseThisYear;
                } else if (lastYearWords != 0){
                    delta = (new Long(words - lastYearWords).doubleValue())/lastYearWords;
                }
                writer.write(wordsByYears.get(i).getFormattedDate(),
                        FORMAT_PROPORTIONS.format(words.doubleValue()/1000000),
                        FORMAT_PROPORTIONS.format(expectedIncrease.doubleValue()/1000000),
                        FORMAT_PROPORTIONS.format(delta * 100));
                lastYearWords = words;
            }
        }

        long messagesCount = messagesService.selectMessagesCount();
        long wordsCount1 = messagesService.selectWordsCount();

        logger.info("Writing overall statistic");
        try (ClWriter writer = new ClWriter(overallStatsFile)) {
            writer.write("messages_total", messagesCount);
            writer.write("words_total", wordsCount1);
            writer.write("average_density", UserStatisticsJob.FORMAT_DENSITY.format(Long.valueOf(wordsCount1).doubleValue()
                    / messagesCount));
            writer.write("expected_words_sum_current_year", lastYearWordsEquality);
        }

        logger.info("Writing graph data...");
        List graphData = Arrays.asList(actual, prediction);
        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.setDateFormat(DATE_FORMAT);
        objectMapper.writeValue(byMonthsJsonFile, graphData);
    }

    public static Date toDate(String dateString) {
        Calendar cal = Calendar.getInstance();
        cal = DateUtils.truncate(cal, MONTH);
        cal.set(YEAR, Integer.valueOf(dateString.substring(0, 4)));
        cal.set(MONTH, extractMonthId(dateString));
        return cal.getTime();
    }

    private static int extractMonthId(String dateString) {
        return Integer.valueOf(dateString.substring(dateString.length() - 2, dateString.length())) - 1;
    }

    @Override
    public int getPriority() {
        return 1000;
    }

    @Override
    public String getDescription() {
        return "Calculating overall statistics";
    }
}
