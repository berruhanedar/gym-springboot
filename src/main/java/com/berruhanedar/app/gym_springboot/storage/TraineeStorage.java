package com.berruhanedar.app.gym_springboot.storage;

import com.berruhanedar.app.gym_springboot.entity.Trainee;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class TraineeStorage {
    private final Map<Long, Trainee> trainees = new ConcurrentHashMap<>();

    public Map<Long, Trainee> getData() {
        return trainees;
    }
}