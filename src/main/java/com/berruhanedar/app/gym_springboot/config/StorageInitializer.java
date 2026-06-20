package com.berruhanedar.app.gym_springboot.config;

import com.berruhanedar.app.gym_springboot.entity.*;
import com.berruhanedar.app.gym_springboot.storage.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.beans.factory.config.BeanPostProcessor;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Component;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;

@Slf4j
@Component
public class StorageInitializer implements BeanPostProcessor {
    private final Resource dataResource;
    private final TraineeStorage traineeStorage;
    private final TrainerStorage trainerStorage;
    private final TrainingStorage trainingStorage;
    private final TrainingTypeStorage trainingTypeStorage;
    private final IdGenerator idGenerator;
    private boolean initialized;

    public StorageInitializer(@Value("${gym.storage.initial-data-path}") Resource dataResource,
                              TraineeStorage traineeStorage,
                              TrainerStorage trainerStorage,
                              TrainingStorage trainingStorage,
                              TrainingTypeStorage trainingTypeStorage,
                              IdGenerator idGenerator) {
        this.dataResource = dataResource;
        this.traineeStorage = traineeStorage;
        this.trainerStorage = trainerStorage;
        this.trainingStorage = trainingStorage;
        this.trainingTypeStorage = trainingTypeStorage;
        this.idGenerator = idGenerator;
    }

    @Override
    public Object postProcessAfterInitialization(Object bean, String beanName) throws BeansException {
        if (!initialized && bean instanceof TrainingStorage) {
            initialize();
            initialized = true;
        }
        return bean;
    }

    private void initialize() {
        if (!dataResource.exists()) {
            log.warn("Initial storage data file not found. path={}", dataResource);
            return;
        }

        log.info("Initializing in-memory storage from file");

        long maxTrainee = 0;
        long maxTrainer = 0;
        long maxTraining = 0;
        long maxTrainingType = 0;

        try (BufferedReader reader = new BufferedReader(
                new InputStreamReader(dataResource.getInputStream(), StandardCharsets.UTF_8))) {

            String line;

            while ((line = reader.readLine()) != null) {
                if (line.isBlank() || line.startsWith("#")) {
                    continue;
                }

                String[] p = line.split(",", -1);

                switch (p[0]) {
                    case "TRAINEE" -> {
                        Trainee trainee = new Trainee();
                        trainee.setId(Long.valueOf(p[1]));
                        trainee.setFirstName(p[2]);
                        trainee.setLastName(p[3]);
                        trainee.setUsername(p[4]);
                        trainee.setPassword(p[5]);
                        trainee.setIsActive(Boolean.valueOf(p[6]));
                        trainee.setDateOfBirth(LocalDate.parse(p[7]));
                        trainee.setAddress(p[8]);

                        traineeStorage.getData().put(trainee.getId(), trainee);
                        maxTrainee = Math.max(maxTrainee, trainee.getId());
                    }

                    case "TRAINER" -> {
                        Trainer trainer = new Trainer();
                        trainer.setId(Long.valueOf(p[1]));
                        trainer.setFirstName(p[2]);
                        trainer.setLastName(p[3]);
                        trainer.setUsername(p[4]);
                        trainer.setPassword(p[5]);
                        trainer.setIsActive(Boolean.valueOf(p[6]));
                        trainer.setSpecialization(p[7]);

                        trainerStorage.getData().put(trainer.getId(), trainer);
                        maxTrainer = Math.max(maxTrainer, trainer.getId());
                    }

                    case "TRAINING" -> {
                        TrainingType type = trainingTypeStorage.getData().values().stream()
                                .filter(t -> t.getTrainingTypeName().equalsIgnoreCase(p[5]))
                                .findFirst()
                                .orElseGet(() -> {
                                    TrainingType newType = new TrainingType(
                                            trainingTypeStorage.getData().size() + 1L,
                                            p[5]
                                    );
                                    trainingTypeStorage.getData().put(newType.getId(), newType);
                                    return newType;
                                });

                        Training training = new Training(
                                Long.valueOf(p[1]),
                                Long.valueOf(p[2]),
                                Long.valueOf(p[3]),
                                p[4],
                                type,
                                LocalDate.parse(p[6]),
                                Integer.valueOf(p[7])
                        );

                        trainingStorage.getData().put(training.getId(), training);
                        maxTraining = Math.max(maxTraining, training.getId());
                        maxTrainingType = Math.max(maxTrainingType, type.getId());
                    }

                    default -> log.warn("Unknown initial data row type={}", p[0]);
                }
            }

            idGenerator.sync(maxTrainee, maxTrainer, maxTraining, maxTrainingType);

            log.info("Storage initialized. trainees={}, trainers={}, trainings={}, trainingTypes={}",
                    traineeStorage.getData().size(),
                    trainerStorage.getData().size(),
                    trainingStorage.getData().size(),
                    trainingTypeStorage.getData().size());

        } catch (Exception e) {
            log.error("Storage initialization failed", e);
            throw new IllegalStateException("Storage initialization failed", e);
        }
    }
}