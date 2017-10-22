package com.ivanlukomskiy.chatsLab.model.json;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.Date;

/**
 * Created by ivanl <ilukomskiy@sbdagroup.com> on 20.10.2017.
 */
@Data
@AllArgsConstructor
public class PointOnTime {
    public Date date;
    public long value;
}
