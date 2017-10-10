package com.ivanlukomskiy.chatsLab.repository;

import com.ivanlukomskiy.chatsLab.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

/**
 * Created by ivanl <ilukomskiy@sbdagroup.com> on 07.10.2017.
 */
public interface UserRepository extends JpaRepository<User, Integer> {
    @Query("select user from User user where user.id in :ids")
    List<User> findExistUsers(@Param("ids") List<Integer> idsToCheck);
}
