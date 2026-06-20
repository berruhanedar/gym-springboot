package com.berruhanedar.app.gym_springboot.storage;

import com.berruhanedar.app.gym_springboot.entity.Trainer;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class TrainerStorage {
    private final Map<Long, Trainer> trainers = new ConcurrentHashMap<>();

    public Map<Long, Trainer> getData() {
        return trainers;
    }
}