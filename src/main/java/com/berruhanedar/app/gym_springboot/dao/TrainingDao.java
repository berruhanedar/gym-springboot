package com.berruhanedar.app.gym_springboot.dao;

import com.berruhanedar.app.gym_springboot.entity.Training;
import com.berruhanedar.app.gym_springboot.storage.TrainingStorage;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public class TrainingDao {
    private TrainingStorage storage;

    @Autowired
    public void setStorage(TrainingStorage storage) {
        this.storage = storage;
    }

    public Training save(Training training) {
        storage.getData().put(training.getId(), training);
        return training;
    }

    public Optional<Training> findById(Long id) {
        return Optional.ofNullable(storage.getData().get(id));
    }

    public List<Training> findAll() {
        return List.copyOf(storage.getData().values());
    }
}
