package com.ivanlukomskiy.chatsLab.model;

/**
 * Created by ivanl <ilukomskiy@sbdagroup.com> on 27.10.2018.
 */

import lombok.Data;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.Table;

@Data
@Entity
@Table(name = "cl_forced_merge_tasks")
public class ForcedMergeTask {

    @Id
    @GeneratedValue
    private int id;

    private String nameFrom;
    private String nameTo;
}
