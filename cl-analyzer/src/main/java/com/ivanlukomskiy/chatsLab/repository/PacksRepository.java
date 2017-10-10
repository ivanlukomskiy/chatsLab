package com.ivanlukomskiy.chatsLab.repository;

import com.ivanlukomskiy.chatsLab.model.Pack;
import org.springframework.data.jpa.repository.JpaRepository;

/**
 * Created by ivanl <ilukomskiy@sbdagroup.com> on 07.10.2017.
 */
public interface PacksRepository extends JpaRepository<Pack, String> {
}
