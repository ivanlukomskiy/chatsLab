package com.ivanlukomskiy.chatsLab.repository;

import com.ivanlukomskiy.chatsLab.model.MergeTask;
import org.springframework.data.jpa.repository.JpaRepository;

/**
 * Created by ivanl <ilukomskiy@sbdagroup.com> on 25.10.2018.
 */
public interface MergeTaskRepository extends JpaRepository<MergeTask, Integer> {
}
