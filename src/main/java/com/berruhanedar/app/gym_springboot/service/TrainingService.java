package com.berruhanedar.app.gym_springboot.service;

import com.berruhanedar.app.gym_springboot.dao.TraineeDao;
import com.berruhanedar.app.gym_springboot.dao.TrainerDao;
import com.berruhanedar.app.gym_springboot.dao.TrainingDao;
import com.berruhanedar.app.gym_springboot.dao.TrainingTypeDao;
import com.berruhanedar.app.gym_springboot.dto.*;
import com.berruhanedar.app.gym_springboot.entity.Trainee;
import com.berruhanedar.app.gym_springboot.entity.Trainer;
import com.berruhanedar.app.gym_springboot.entity.Training;
import com.berruhanedar.app.gym_springboot.exception.EntityNotFoundException;
import com.berruhanedar.app.gym_springboot.mapper.TrainingMapper;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
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
    private AuthenticationService authenticationService;

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

    @Autowired
    public void setAuthenticationService(AuthenticationService authenticationService) {
        this.authenticationService = authenticationService;
    }

    @Transactional(readOnly = true)
    public List<TrainingResponseDTO> getTraineeTrainings(CredentialsDTO credentials, String username, TraineeTrainingsFilterDTO filter) {
        authenticationService.authenticate(credentials);
        return trainingDao.findByTraineeUsernameAndCriteria(
                        username,
                        filter.getPeriodFrom(),
                        filter.getPeriodTo(),
                        filter.getTrainerName(),
                        filter.getTrainingType())
                .stream()
                .map(trainingMapper::toDTO)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<TrainingResponseDTO> getTrainerTrainings(CredentialsDTO credentials, String username, TrainerTrainingsFilterDTO filter) {
        authenticationService.authenticate(credentials);
        return trainingDao.findByTrainerUsernameAndCriteria(
                        username,
                        filter.getPeriodFrom(),
                        filter.getPeriodTo(),
                        filter.getTraineeName())
                .stream()
                .map(trainingMapper::toDTO)
                .toList();
    }

    @Transactional
    public void createTraining(CredentialsDTO trainerCredentials, @Valid NewTrainingRequestDTO dto) {
        authenticationService.authenticate(trainerCredentials);
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
                .map(trainingType -> {
                    TrainingTypeResponseDTO dto = new TrainingTypeResponseDTO();
                    dto.setId(trainingType.getId());
                    dto.setTrainingTypeName(trainingType.getTrainingTypeName());
                    return dto;
                })
                .toList();
    }

    private Trainee findTraineeByUsername(String username) {
        return traineeDao.findByUsername(username).orElseThrow(() -> new EntityNotFoundException("Trainee not found: " + username));
    }

    private Trainer findTrainerByUsername(String username) {
        return trainerDao.findByUsername(username).orElseThrow(() -> new EntityNotFoundException("Trainer not found: " + username));
    }
}