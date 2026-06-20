package com.berruhanedar.app.gym_springboot.dao;

import com.berruhanedar.app.gym_springboot.entity.TrainingType;
import com.berruhanedar.app.gym_springboot.storage.TrainingTypeStorage;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public class TrainingTypeDao {
    private TrainingTypeStorage storage;

    @Autowired
    public void setStorage(TrainingTypeStorage storage) {
        this.storage = storage;
    }

    public TrainingType save(TrainingType type) {
        storage.getData().put(type.getId(), type);
        return type;
    }

    public Optional<TrainingType> findByName(String name) {
        return storage.getData().values().stream().filter(t -> t.getTrainingTypeName().equalsIgnoreCase(name)).findFirst();
    }

    public List<TrainingType> findAll() {
        return List.copyOf(storage.getData().values());
    }
}
