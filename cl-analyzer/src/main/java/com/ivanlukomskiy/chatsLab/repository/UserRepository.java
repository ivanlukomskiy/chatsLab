package com.ivanlukomskiy.chatsLab.repository;

import com.ivanlukomskiy.chatsLab.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Set;

/**
 * Created by ivanl <ilukomskiy@sbdagroup.com> on 07.10.2017.
 */
public interface UserRepository extends JpaRepository<User, Integer> {
    @Query("select user from User user where user.id in :ids")
    List<User> findExistUsers(@Param("ids") List<Integer> idsToCheck);

    @Query("select user.id from User user where user.gender is null")
    Set<Integer> findUsersWithNotResolvedGender();

    @Modifying
    @Query("update User set gender=com.ivanlukomskiy.chatsLab.model.Gender.MALE where id in (:ids)")
    int updateSetMale(@Param("ids") Set<Integer> ids);

    @Modifying
    @Query("update User set gender=com.ivanlukomskiy.chatsLab.model.Gender.FEMALE where id in (:ids)")
    int updateSetFemale(@Param("ids") Set<Integer> ids);

    @Modifying
    @Query("update User set gender=com.ivanlukomskiy.chatsLab.model.Gender.UNKNOWN where id in (:ids)")
    int updateSetUnknown(@Param("ids") Set<Integer> ids);

    @Query("select u from User u where u.firstName = :name and u.lastName = '' " +
            "and u.providerId=:providerId")
    List<User> getTelegramUser(@Param("name") String name,
                               @Param("providerId") Integer providerId);
}
