package com.berruhanedar.app.gym_springboot.monitoring;

import com.berruhanedar.app.gym_springboot.dao.TraineeDao;
import com.berruhanedar.app.gym_springboot.dao.TrainerDao;
import com.berruhanedar.app.gym_springboot.dao.TrainingDao;
import com.berruhanedar.app.gym_springboot.dao.TrainingTypeDao;
import org.springframework.boot.actuate.health.Health;
import org.springframework.boot.actuate.health.HealthIndicator;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component("gymData")
public class GymDataHealthIndicator implements HealthIndicator {

    private final TraineeDao traineeDao;
    private final TrainerDao trainerDao;
    private final TrainingDao trainingDao;
    private final TrainingTypeDao trainingTypeDao;

    public GymDataHealthIndicator(TraineeDao traineeDao,
                                  TrainerDao trainerDao,
                                  TrainingDao trainingDao,
                                  TrainingTypeDao trainingTypeDao) {
        this.traineeDao = traineeDao;
        this.trainerDao = trainerDao;
        this.trainingDao = trainingDao;
        this.trainingTypeDao = trainingTypeDao;
    }

    @Override
    @Transactional(readOnly = true)
    public Health health() {
        try {
            long trainees = traineeDao.count();
            long trainers = trainerDao.count();
            long trainings = trainingDao.count();
            long trainingTypes = trainingTypeDao.count();

            Health.Builder builder = trainingTypes > 0
                    ? Health.up()
                    : Health.down().withDetail("reason", "No training types are configured");

            return builder
                    .withDetail("trainees", trainees)
                    .withDetail("trainers", trainers)
                    .withDetail("trainings", trainings)
                    .withDetail("trainingTypes", trainingTypes)
                    .build();
        } catch (Exception exception) {
            return Health.down(exception)
                    .withDetail("reason", "Gym data could not be queried")
                    .build();
        }
    }
}
