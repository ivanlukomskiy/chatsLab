package com.ivanlukomskiy.chatsLab.repository;

import com.ivanlukomskiy.chatsLab.model.ForcedMergeTask;
import com.ivanlukomskiy.chatsLab.service.ForcedMergeJob;
import org.springframework.data.jpa.repository.JpaRepository;

/**
 * Created by ivanl <ilukomskiy@sbdagroup.com> on 27.10.2018.
 */
public interface ForcedMergeJobRepository extends JpaRepository<ForcedMergeTask, Integer> {
}
