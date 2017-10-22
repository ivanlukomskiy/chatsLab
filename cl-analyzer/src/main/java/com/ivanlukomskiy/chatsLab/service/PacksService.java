package com.ivanlukomskiy.chatsLab.service;

import com.ivanlukomskiy.chatsLab.model.Pack;
import com.ivanlukomskiy.chatsLab.repository.PacksRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * Created by ivanl <ilukomskiy@sbdagroup.com> on 09.10.2017.
 */
@Service
public class PacksService {

    @Autowired
    private PacksRepository packsRepository;

    public Pack save(Pack pack) {
        return packsRepository.save(pack);
    }

    public Pack findById(String id) {
        return packsRepository.findOne(id);
    }

    public boolean loaded(String uuid) {
        return packsRepository.findOne(uuid) != null;
    }
}
