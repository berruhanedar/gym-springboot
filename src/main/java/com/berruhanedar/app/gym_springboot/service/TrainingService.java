package com.berruhanedar.app.gym_springboot.service;

import com.berruhanedar.app.gym_springboot.dao.*;
import com.berruhanedar.app.gym_springboot.dto.*;
import com.berruhanedar.app.gym_springboot.entity.*;
import com.berruhanedar.app.gym_springboot.exception.AuthenticationException;
import com.berruhanedar.app.gym_springboot.exception.EntityNotFoundException;
import com.berruhanedar.app.gym_springboot.mapper.TrainingMapper;
import com.berruhanedar.app.gym_springboot.mapper.TrainingTypeMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Slf4j
@Service
public class TrainingService {

    private TrainingDao trainingDao;
    private TraineeDao traineeDao;
    private TrainerDao trainerDao;
    private TrainingTypeDao trainingTypeDao;
    private TrainingMapper trainingMapper;
    private TrainingTypeMapper trainingTypeMapper;

    @Autowired
    public void setTrainingTypeMapper(TrainingTypeMapper trainingTypeMapper) {
        this.trainingTypeMapper = trainingTypeMapper;
    }

    @Autowired
    public void setTrainingDao(TrainingDao trainingDao) {
        this.trainingDao = trainingDao;
    }

    @Autowired
    public void setTraineeDao(TraineeDao traineeDao) {
        this.traineeDao = traineeDao;
    }

    @Autowired
    public void setTrainerDao(TrainerDao trainerDao) {
        this.trainerDao = trainerDao;
    }

    @Autowired
    public void setTrainingTypeDao(TrainingTypeDao trainingTypeDao) {
        this.trainingTypeDao = trainingTypeDao;
    }

    @Autowired
    public void setTrainingMapper(TrainingMapper trainingMapper) {
        this.trainingMapper = trainingMapper;
    }

    @Transactional(readOnly = true)
    public List<TraineeTrainingResponseDTO> getTraineeTrainings(String username, TraineeTrainingsFilterDTO filter) {
        validateCurrentUser(username, "Trainee is not authorized to access these trainings.");
        return trainingDao.findByTraineeUsernameAndCriteria(
                        username,
                        filter.getPeriodFrom(),
                        filter.getPeriodTo(),
                        filter.getTrainerName(),
                        filter.getTrainingType())
                .stream()
                .map(trainingMapper::toTraineeTrainingResponseDTO)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<TrainerTrainingResponseDTO> getTrainerTrainings(String username, TrainerTrainingsFilterDTO filter) {
        validateCurrentUser(username, "Trainer is not authorized to access these trainings.");
        return trainingDao.findByTrainerUsernameAndCriteria(
                        username,
                        filter.getPeriodFrom(),
                        filter.getPeriodTo(),
                        filter.getTraineeName())
                .stream()
                .map(trainingMapper::toTrainerTrainingResponseDTO)
                .toList();
    }

    @Transactional
    public void createTraining(NewTrainingRequestDTO dto) {
        validateCurrentUser(dto.getTrainerUsername(), "Trainer is not authorized to create this training.");
        log.info("Creating training. traineeUsername={}, trainerUsername={}", dto.getTraineeUsername(), dto.getTrainerUsername());
        Trainee trainee = findTraineeByUsername(dto.getTraineeUsername());
        Trainer trainer = findTrainerByUsername(dto.getTrainerUsername());
        Training training = trainingMapper.toEntity(dto);
        training.setTrainee(trainee);
        training.setTrainer(trainer);
        training.setTrainingType(trainer.getSpecialization());
        Training saved = trainingDao.save(training);
        log.info("Training created successfully. id={}", saved.getId());
    }

    @Transactional(readOnly = true)
    public List<TrainingTypeResponseDTO> getTrainingTypes() {
        return trainingTypeDao.findAll()
                .stream()
                .map(trainingTypeMapper::toDTO)
                .toList();
    }

    private Trainee findTraineeByUsername(String username) {
        return traineeDao.findByUsername(username).orElseThrow(() -> new EntityNotFoundException("Trainee not found: " + username));
    }

    private Trainer findTrainerByUsername(String username) {
        return trainerDao.findByUsername(username).orElseThrow(() -> new EntityNotFoundException("Trainer not found: " + username));
    }

    private void validateCurrentUser(String username, String message) {
        String authenticatedUsername = SecurityContextHolder.getContext().getAuthentication().getName();
        if (!authenticatedUsername.equals(username)) {
            throw new AuthenticationException(message);
        }
    }
}