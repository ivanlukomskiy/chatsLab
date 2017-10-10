package com.ivanlukomskiy.chatsLab.repository;

import com.ivanlukomskiy.chatsLab.model.Chat;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

/**
 * Created by ivanl <ilukomskiy@sbdagroup.com> on 07.10.2017.
 */
public interface ChatRepository extends JpaRepository<Chat, Integer> {

    @Query("select c.name, count(c) from Chat c group by c.name")
    List<Object[]> getDuplicateChatNames();

    List<Chat> findByName(String name);

    @Modifying
    @Query(value = "UPDATE cl_messages " +
            "SET chat_id = :target_id " +
            "WHERE id IN (" +
            "  SELECT m1.id" +
            "  FROM cl_messages m1" +
            "    JOIN cl_messages m2 ON m1.time = m2.time" +
            "  WHERE m1.chat_id = :source_id AND m2.chat_id = :target_id" +
            "        AND m1.sender_id = m2.sender_id)", nativeQuery = true)
    void mergeChats(@Param("source_id") int sourceId, @Param("target_id") int targetId);

    @Modifying
    @Query(value = "DELETE FROM cl_messages " +
            "WHERE chat_id = :chat_id", nativeQuery = true)
    void clearChatMessages(@Param("chat_id") int chatId);

    @Modifying
    @Query(value = "update cl_chats cc set messages_number = " +
            "COALESCE((select count(*) from cl_messages cm where cm.chat_id = :chat_id),0)" +
            " where cc.id = :chat_id", nativeQuery = true)
    void updateChatMessagesNumber(@Param("chat_id") int chatId);

    @Modifying
    @Query(value = "update cl_chats cc set words_number =" +
            "COALESCE((select sum(cm.words_number) from cl_messages cm where cm.chat_id = :chat_id),0)" +
            " where cc.id = :chat_id",
            nativeQuery = true)
    void updateChatWordsNumber(@Param("chat_id") int chatId);
}
