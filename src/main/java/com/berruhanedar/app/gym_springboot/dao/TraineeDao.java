package com.berruhanedar.app.gym_springboot.dao;

import com.berruhanedar.app.gym_springboot.entity.Trainee;
import com.berruhanedar.app.gym_springboot.storage.TraineeStorage;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public class TraineeDao {
    private TraineeStorage storage;

    @Autowired
    public void setStorage(TraineeStorage storage) {
        this.storage = storage;
    }

    public Trainee save(Trainee trainee) {
        storage.getData().put(trainee.getId(), trainee);
        return trainee;
    }

    public Optional<Trainee> findById(Long id) {
        return Optional.ofNullable(storage.getData().get(id));
    }

    public List<Trainee> findAll() {
        return List.copyOf(storage.getData().values());
    }

    public void delete(Long id) {
        storage.getData().remove(id);
    }

    public List<String> findAllUsernames() {
        return findAll().stream()
                .map(Trainee::getUsername)
                .toList();
    }

    public boolean existsByUsername(String username) {
        return findAll().stream()
                .anyMatch(t -> username.equals(t.getUsername()));
    }
}