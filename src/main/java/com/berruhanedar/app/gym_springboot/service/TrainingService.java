package com.berruhanedar.app.gym_springboot.service;

import com.berruhanedar.app.gym_springboot.dao.TraineeDao;
import com.berruhanedar.app.gym_springboot.dao.TrainerDao;
import com.berruhanedar.app.gym_springboot.dao.TrainingDao;
import com.berruhanedar.app.gym_springboot.dao.TrainingTypeDao;
import com.berruhanedar.app.gym_springboot.dto.*;
import com.berruhanedar.app.gym_springboot.entity.Trainee;
import com.berruhanedar.app.gym_springboot.entity.Trainer;
import com.berruhanedar.app.gym_springboot.entity.Training;
import com.berruhanedar.app.gym_springboot.entity.TrainingType;
import com.berruhanedar.app.gym_springboot.exception.EntityNotFoundException;
import com.berruhanedar.app.gym_springboot.mapper.TrainingMapper;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
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

    @Transactional
    public TrainingResponseDTO createTraining(
            CredentialsDTO trainerCredentials,
            @Valid NewTrainingRequestDTO dto
    ) {
        authenticationService.authenticate(trainerCredentials);

        log.info("Creating training. traineeId={}, trainerId={}",
                dto.getTraineeId(), dto.getTrainerId());

        Trainee trainee = findTraineeById(dto.getTraineeId());
        Trainer trainer = findTrainerById(dto.getTrainerId());
        TrainingType trainingType = findTrainingTypeByName(dto.getTrainingTypeName());

        Training training = trainingMapper.toEntity(dto);
        training.setTrainee(trainee);
        training.setTrainer(trainer);
        training.setTrainingType(trainingType);

        Training saved = trainingDao.save(training);

        log.info("Training created successfully. id={}", saved.getId());
        return trainingMapper.toDTO(saved);
    }

    @Transactional(readOnly = true)
    public TrainingResponseDTO getTraining(CredentialsDTO traineeCredentials, Long id) {
        authenticationService.authenticate(traineeCredentials);

        log.debug("Selecting training. id={}", id);

        Training training = findTrainingById(id);

        return trainingMapper.toDTO(training);
    }

    @Transactional(readOnly = true)
    public List<TrainingResponseDTO> getTraineeTrainings(
            CredentialsDTO credentials,
            String username,
            TraineeTrainingsFilterDTO filter) {
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

    private Training findTrainingById(Long id) {
        return trainingDao.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Training not found. id=" + id));
    }

    private Trainee findTraineeById(Long id) {
        return traineeDao.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Trainee not found. id=" + id));
    }

    private Trainer findTrainerById(Long id) {
        return trainerDao.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Trainer not found. id=" + id));
    }

    private TrainingType findTrainingTypeByName(String trainingTypeName) {
        return trainingTypeDao.findByName(trainingTypeName)
                .orElseThrow(() -> new EntityNotFoundException(
                        "Training type not found: " + trainingTypeName));
    }
}