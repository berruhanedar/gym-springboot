package com.berruhanedar.app.gym_springboot.storage;

import com.berruhanedar.app.gym_springboot.entity.Training;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class TrainingStorage {
    private final Map<Long, Training> trainings = new ConcurrentHashMap<>();

    public Map<Long, Training> getData() {
        return trainings;
    }
}