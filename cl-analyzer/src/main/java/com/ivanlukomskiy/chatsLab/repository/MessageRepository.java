package com.ivanlukomskiy.chatsLab.repository;

import com.ivanlukomskiy.chatsLab.model.Message;
import com.ivanlukomskiy.chatsLab.model.User;
import com.ivanlukomskiy.chatsLab.model.dto.DateToWords;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Date;
import java.util.List;
import java.util.Set;

/**
 * Created by ivanl <ilukomskiy@sbdagroup.com> on 07.10.2017.
 */
public interface MessageRepository extends JpaRepository<Message, Integer> {

    @Query("select count(m) from Message m")
    int selectMessagesCount();

    @Query("select sum(m.wordsNumber) from Message m")
    int selectWordsCount();

    @Query("select min(m.time) from Message m")
    Date getMinDate();

    @Query("select max(m.time) from Message m")
    Date getMaxDate();

    @Query("select count(m) from Message m where m.time >= :start and m.time < :end")
    int selectMessagesInTimeRange(@Param("start") Date start, @Param("end") Date end);

    @Query("select sum(m.wordsNumber) from Message m where m.time >= :start and m.time < :end")
    int selectWordsInTimeRange(@Param("start") Date start, @Param("end") Date end);

    @Query(value = "SELECT to_char(mes.time, 'yyyy') as date_formatted, sum(mes.words_number) " +
            "FROM cl_messages mes GROUP BY date_formatted order by date_formatted", nativeQuery = true)
    List<Object[]> getWordsByYears();

    @Query(value = "SELECT to_char(mes.time, 'yyyy-mm-dd') as date_formatted, sum(mes.words_number) " +
            "FROM cl_messages mes WHERE mes.time >= :start AND mes.time < :end " +
            " GROUP BY date_formatted order by date_formatted", nativeQuery = true)
    List<Object[]> getWordsByDays(@Param("start") Date start, @Param("end") Date end);

    @Query(value = "SELECT extract(dow from mes.time) as date_formatted, sum(mes.words_number) " +
            "FROM cl_messages mes GROUP BY date_formatted order by date_formatted", nativeQuery = true)
    List<Object[]> getWordsByDaysOfWeek();

    @Query(value = "SELECT to_char(mes.time, 'yyyy-mm') as date_formatted, sum(mes.words_number) " +
            "FROM cl_messages mes GROUP BY date_formatted order by date_formatted", nativeQuery = true)
    List<Object[]> getWordsByMonths();

    @Query(value = "SELECT to_char(mes.time, 'yyyy-mm') as date_formatted, sum(mes.words_number) " +
            "FROM cl_messages mes WHERE mes.sender_id=:userId GROUP BY date_formatted " +
            "order by date_formatted", nativeQuery = true)
    List<Object[]> getWordsByMonthsAndUser(@Param("userId") Integer userId);

    @Query("select count(m) from Message m where m.time >= :start and m.time < :end " +
            "and m.sender = :user")
    int selectMessagesInTimeRange(@Param("start") Date start, @Param("end") Date end, @Param("user") User user);

    @Query(value = "SELECT" +
            "  chat.name," +
            "  groupped.cnt" +
            " FROM cl_chats chat" +
            "  JOIN (SELECT" +
            "          count(mes.words_number) AS cnt," +
            "          mes.chat_id             AS id" +
            "        FROM cl_messages mes" +
            "        GROUP BY mes.chat_id) AS groupped" +
            "    ON groupped.id = chat.id " +
            "ORDER BY groupped.cnt DESC", nativeQuery = true)
    List<Object[]> getWordsByChats();

    Page<Message> findAll(Pageable page);

    @Query(value = "SELECT" +
            "  chat.name," +
            "  groupped.cnt" +
            " FROM cl_chats chat" +
            "  JOIN (SELECT" +
            "          count(mes.words_number) AS cnt," +
            "          mes.chat_id             AS id" +
            "        FROM cl_messages mes " +
            "        WHERE mes.time > :start and mes.time < :end " +
            "        GROUP BY mes.chat_id " +
            " ) AS groupped" +
            "    ON groupped.id = chat.id " +
            "ORDER BY groupped.cnt DESC", nativeQuery = true)
    List<Object[]> getWordsByChats(@Param("start") Date start, @Param("end") Date end);

    @Query(value = "SELECT" +
            "  usr.id, usr.first_name, usr.last_name, usr.gender," +
            "  groupped.wcnt,groupped.mcnt" +
            " FROM cl_users usr" +
            "  JOIN (SELECT" +
            "          sum(mes.words_number) AS wcnt," +
            "          count(*) AS mcnt," +
            "          mes.sender_id             AS id" +
            "        FROM cl_messages mes" +
            "        GROUP BY mes.sender_id) AS groupped" +
            "    ON groupped.id = usr.id " +
            "ORDER BY groupped.wcnt DESC", nativeQuery = true)
    List<Object[]> getWordsBySender();

    @Query(value = "SELECT" +
            "  usr.id, usr.first_name, usr.last_name, usr.gender, " +
            "  groupped.wcnt,groupped.mcnt" +
            " FROM cl_users usr" +
            "  JOIN (SELECT" +
            "          sum(mes.words_number) AS wcnt," +
            "          count(*) AS mcnt," +
            "          mes.sender_id             AS id" +
            "        FROM cl_messages mes " +
            "        WHERE mes.time > :start and mes.time < :end " +
            "        GROUP BY mes.sender_id " +
            " ) AS groupped" +
            "    ON groupped.id = usr.id " +
            "ORDER BY groupped.wcnt DESC", nativeQuery = true)
    List<Object[]> getWordsBySender(@Param("start") Date start, @Param("end") Date end);

//    @Query(value = "SELECT to_char(mes.time, 'yyyy') as date_formatted, sum(mes.words_number) " +
//            "FROM cl_messages mes WHERE mes.sender_id = :userId" +
//            " GROUP BY date_formatted order by date_formatted", nativeQuery = true)
//    List<Object[]> getWordsByYearsAndUser(@Param("user_id") int userId);
}
