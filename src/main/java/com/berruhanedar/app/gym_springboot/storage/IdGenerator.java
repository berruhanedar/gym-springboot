package com.berruhanedar.app.gym_springboot.storage;

import org.springframework.stereotype.Component;

import java.util.concurrent.atomic.AtomicLong;

@Component
public class IdGenerator {
    private final AtomicLong traineeId = new AtomicLong(1);
    private final AtomicLong trainerId = new AtomicLong(1);
    private final AtomicLong trainingId = new AtomicLong(1);
    private final AtomicLong trainingTypeId = new AtomicLong(1);

    public Long nextTraineeId() {
        return traineeId.getAndIncrement();
    }

    public Long nextTrainerId() {
        return trainerId.getAndIncrement();
    }

    public Long nextTrainingId() {
        return trainingId.getAndIncrement();
    }

    public Long nextTrainingTypeId() {
        return trainingTypeId.getAndIncrement();
    }

    public void sync(long maxTrainee, long maxTrainer, long maxTraining, long maxTrainingType) {
        traineeId.set(Math.max(traineeId.get(), maxTrainee + 1));
        trainerId.set(Math.max(trainerId.get(), maxTrainer + 1));
        trainingId.set(Math.max(trainingId.get(), maxTraining + 1));
        trainingTypeId.set(Math.max(trainingTypeId.get(), maxTrainingType + 1));
    }
}
