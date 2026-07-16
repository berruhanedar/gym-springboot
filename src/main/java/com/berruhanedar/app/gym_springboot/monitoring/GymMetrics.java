package com.berruhanedar.app.gym_springboot.monitoring;

import com.berruhanedar.app.gym_springboot.dao.TraineeDao;
import com.berruhanedar.app.gym_springboot.dao.TrainerDao;
import com.berruhanedar.app.gym_springboot.dao.TrainingDao;
import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.Gauge;
import io.micrometer.core.instrument.MeterRegistry;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.stereotype.Component;

@Component
public class GymMetrics {

    private final MeterRegistry meterRegistry;
    private final Counter successfulLogins;
    private final Counter failedLogins;
    private final Counter traineeRegistrations;
    private final Counter trainerRegistrations;
    private final Counter trainingsCreated;

    public GymMetrics(ObjectProvider<MeterRegistry> meterRegistryProvider,
                      TraineeDao traineeDao,
                      TrainerDao trainerDao,
                      TrainingDao trainingDao) {
        this.meterRegistry = meterRegistryProvider.getIfAvailable();
        if (this.meterRegistry == null) {
            successfulLogins = null;
            failedLogins = null;
            traineeRegistrations = null;
            trainerRegistrations = null;
            trainingsCreated = null;
            return;
        }

        successfulLogins = Counter.builder("gym.authentication.attempts")
                .description("Number of authentication attempts")
                .tag("result", "success")
                .register(this.meterRegistry);

        failedLogins = Counter.builder("gym.authentication.attempts")
                .description("Number of authentication attempts")
                .tag("result", "failure")
                .register(this.meterRegistry);

        traineeRegistrations = Counter.builder("gym.profile.registrations")
                .description("Number of created gym profiles")
                .tag("profile.type", "trainee")
                .register(this.meterRegistry);

        trainerRegistrations = Counter.builder("gym.profile.registrations")
                .description("Number of created gym profiles")
                .tag("profile.type", "trainer")
                .register(this.meterRegistry);

        trainingsCreated = Counter.builder("gym.trainings.created")
                .description("Number of created trainings")
                .register(this.meterRegistry);

        Gauge.builder("gym.trainees.total", traineeDao, TraineeDao::count)
                .description("Current number of trainees")
                .register(this.meterRegistry);

        Gauge.builder("gym.trainers.total", trainerDao, TrainerDao::count)
                .description("Current number of trainers")
                .register(this.meterRegistry);

        Gauge.builder("gym.trainings.total", trainingDao, TrainingDao::count)
                .description("Current number of trainings")
                .register(this.meterRegistry);
    }

    public void recordSuccessfulLogin() {
        if (successfulLogins != null) successfulLogins.increment();
    }

    public void recordFailedLogin() {
        if (failedLogins != null) failedLogins.increment();
    }

    public void recordTraineeRegistration() {
        if (traineeRegistrations != null) traineeRegistrations.increment();
    }

    public void recordTrainerRegistration() {
        if (trainerRegistrations != null) trainerRegistrations.increment();
    }

    public void recordTrainingCreated() {
        if (trainingsCreated != null) trainingsCreated.increment();
    }

    MeterRegistry getMeterRegistry() {
        return meterRegistry;
    }
}
