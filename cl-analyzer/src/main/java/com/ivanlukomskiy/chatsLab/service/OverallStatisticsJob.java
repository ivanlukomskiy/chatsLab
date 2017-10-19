package com.ivanlukomskiy.chatsLab.service;

import com.ivanlukomskiy.chatsLab.model.dto.DateToWords;
import lombok.Getter;
import lombok.SneakyThrows;
import org.apache.commons.lang.time.DateUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.File;
import java.io.IOException;
import java.text.DateFormatSymbols;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.*;

import static java.io.File.separator;
import static java.lang.Math.round;
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

    private static final DecimalFormat FORMAT_PROPORTIONS = new DecimalFormat("#.##");

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

        logger.info("Writing words by years...");
        List<DateToWords> wordsByYears = messagesService.getWordsByYears();
        try (ClWriter writer = new ClWriter(byYearsFile)) {
            wordsByYears.forEach(date -> writer.write(date.getFormattedDate(), date.getWords()));
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
                    FORMAT_PROPORTIONS.format(entry.getValue() * 100)+"%"));
        }

        logger.info("Writing words by months...");
        int wordsLastSixMonths = 0;
        long lastYearWordsEquality = 0;
        try (ClWriter writer = new ClWriter(byMonthsFile)) {
            wordsByMonths.forEach(date -> writer.write(date.getFormattedDate(), date.getWords(), "accurate"));
            String lastDateString = wordsByMonths.get(wordsByMonths.size() - 1).getFormattedDate();
            int lastMonthId = extractMonthId(lastDateString);
            int lastYear = Integer.valueOf(lastDateString.substring(0, 4));

            Calendar cal = Calendar.getInstance();
            cal.set(YEAR, lastYear);
            cal.set(MONTH, lastMonthId);
            DateUtils.ceiling(cal, MONTH);

            double weight = 0;
            for (int i = wordsByMonths.size() - 2; i > wordsByMonths.size() - 8; i--) {
                wordsLastSixMonths += wordsByMonths.get(i).getWords();
                int monthId = extractMonthId(wordsByMonths.get(i).getFormattedDate());
                weight += monthsActivityProportion.get(monthId);
            }
            lastYearWordsEquality = (int) round(wordsLastSixMonths / weight);
            yearPredictionMultiplier = 1 / weight;

            for (int i = 0; i < 6; i++) {
                int monthId = cal.get(MONTH);
                int year = cal.get(YEAR);
                Double expected = monthsActivityProportion.get(cal.get(MONTH)) * lastYearWordsEquality;
                writer.write(year + "-" + (monthId + 1), expected.intValue(), "prediction");
                cal.add(MONTH, 1);
            }
        }

        logger.info("Writing overall statistic");
        try (ClWriter writer = new ClWriter(overallStatsFile)) {
            writer.write("messages_total", messagesService.selectMessagesCount());
            writer.write("words_total", messagesService.selectWordsCount());
            writer.write("expected_current_year", lastYearWordsEquality);
        }
    }

    private int extractMonthId(String dateString) {
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
