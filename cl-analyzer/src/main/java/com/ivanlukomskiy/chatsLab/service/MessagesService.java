package com.ivanlukomskiy.chatsLab.service;

import com.ivanlukomskiy.chatsLab.model.Message;
import com.ivanlukomskiy.chatsLab.model.User;
import com.ivanlukomskiy.chatsLab.model.dto.ChatNameToWords;
import com.ivanlukomskiy.chatsLab.model.dto.DateToWords;
import com.ivanlukomskiy.chatsLab.model.dto.UserToWords;
import com.ivanlukomskiy.chatsLab.repository.MessageRepository;
import org.apache.commons.lang.time.DateUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigInteger;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Created by ivanl <ilukomskiy@sbdagroup.com> on 09.10.2017.
 */
@Service
public class MessagesService {

    @Autowired
    private MessageRepository messageRepository;

    public void loadMessages(List<Message> messages) {
        messageRepository.save(messages);
    }

    public int selectMessagesCount() {
        return messageRepository.selectMessagesCount();
    }

    public int selectWordsCount() {
        return messageRepository.selectWordsCount();
    }

    public Date getMinDate() {
        return messageRepository.getMinDate();
    }

    public Date getMaxDate() {
        return messageRepository.getMaxDate();
    }

    public int selectMessagesCountInTimeRange(Date start, Date end) {
        return messageRepository.selectMessagesInTimeRange(start, end);
    }

    public int selectWordsCountInTimeRange(Date start, Date end) {
        return messageRepository.selectWordsInTimeRange(start, end);
    }

    public int selectMessagesCountInTimeRange(Date start, Date end, User user) {
        return messageRepository.selectMessagesInTimeRange(start, end, user);
    }

    public List<DateToWords> getWordsByYears() {
        return messageRepository.getWordsByYears().stream()
                .map(DateToWords::new)
                .collect(Collectors.toList());
    }

    public List<DateToWords> getWordsByMonths() {
        return messageRepository.getWordsByMonths().stream()
                .map(DateToWords::new)
                .collect(Collectors.toList());
    }

    public List<ChatNameToWords> getWordsByChats() {
        return messageRepository.getWordsByChats().stream().map(ChatNameToWords::new).collect(Collectors.toList());
    }

    public List<ChatNameToWords> getWordsByChatLastYear() {
        Date last = messageRepository.getMaxDate();
        Calendar cal = Calendar.getInstance();
        cal.setTime(last);
        cal.roll(Calendar.YEAR, -1);
        Date minusYear = cal.getTime();
        return messageRepository.getWordsByChats(minusYear, last).stream()
                .map(ChatNameToWords::new)
                .filter(cntw -> cntw.getWords() != 0).collect(Collectors.toList());
    }

    public List<UserToWords> getWordsByUser() {
        return messageRepository.getWordsBySender().stream().map(UserToWords::new).collect(Collectors.toList());
    }

    public List<UserToWords> getWordsByUserLastYear() {
        Date last = messageRepository.getMaxDate();
        Calendar cal = Calendar.getInstance();
        cal.setTime(last);
        cal.roll(Calendar.YEAR, -1);
        Date minusYear = cal.getTime();
        return messageRepository.getWordsBySender(minusYear, last).stream()
                .map(UserToWords::new).collect(Collectors.toList());
    }

    public Map<Integer, List<UserToWords>> getWordsByUserAndYears() {
        Date first = messageRepository.getMinDate();
        Date last = messageRepository.getMaxDate();

        Calendar pointer = Calendar.getInstance();
        pointer.setTime(DateUtils.ceiling(first, Calendar.YEAR));

        Map<Integer, List<UserToWords>> result = new HashMap<>();

        while(pointer.getTime().before(last)) {
            Date left = pointer.getTime();
            Integer year = pointer.get(Calendar.YEAR);
            pointer.add(Calendar.YEAR, 1);
            Date right = pointer.getTime();

            List<UserToWords> users = messageRepository.getWordsBySender(left, right)
                    .stream()
                    .map(UserToWords::new)
                    .collect(Collectors.toList());

            result.put(year, users);
        }
        return result;
    }
}
