package com.berruhanedar.app.gym_springboot.dao;

import com.berruhanedar.app.gym_springboot.entity.Trainer;
import com.berruhanedar.app.gym_springboot.storage.TrainerStorage;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public class TrainerDao {
    private TrainerStorage storage;

    @Autowired
    public void setStorage(TrainerStorage storage) {
        this.storage = storage;
    }

    public Trainer save(Trainer trainer) {
        storage.getData().put(trainer.getId(), trainer);
        return trainer;
    }

    public Optional<Trainer> findById(Long id) {
        return Optional.ofNullable(storage.getData().get(id));
    }

    public List<Trainer> findAll() {
        return List.copyOf(storage.getData().values());
    }

    public List<String> findAllUsernames() {
        return findAll().stream()
                .map(Trainer::getUsername)
                .toList();
    }

    public boolean existsByUsername(String username) {
        return findAll().stream().anyMatch(t -> username.equals(t.getUsername()));
    }
}
