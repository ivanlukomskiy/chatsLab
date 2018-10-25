package com.ivanlukomskiy.chatsLab.repository;

import com.ivanlukomskiy.chatsLab.model.ChatPositionFingerprint;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

/**
 * Created by ivanl <ilukomskiy@sbdagroup.com> on 24.10.2018.
 */
public interface ChatPositionFpRepository extends JpaRepository<ChatPositionFingerprint, Integer> {

    @Modifying
    @Query(value = "update cl_chat_position_fingerprints set suspect = :to where suspect = :from", nativeQuery = true)
    void changeSuspect(@Param("from") Integer from, @Param("to") Integer to);
}
