package com.berruhanedar.app.gym_springboot.storage;

import com.berruhanedar.app.gym_springboot.entity.TrainingType;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class TrainingTypeStorage {
    private final Map<Long, TrainingType> trainingTypes = new ConcurrentHashMap<>();

    public Map<Long, TrainingType> getData() {
        return trainingTypes;
    }
}