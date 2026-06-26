package com.berruhanedar.app.gym_springboot.service;

import com.berruhanedar.app.gym_springboot.dao.*;
import com.berruhanedar.app.gym_springboot.dto.*;
import com.berruhanedar.app.gym_springboot.entity.Trainee;
import com.berruhanedar.app.gym_springboot.entity.Trainer;
import com.berruhanedar.app.gym_springboot.entity.Training;
import com.berruhanedar.app.gym_springboot.entity.TrainingType;
import com.berruhanedar.app.gym_springboot.exception.EntityNotFoundException;
import com.berruhanedar.app.gym_springboot.mapper.TrainingMapper;
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

    @Transactional
    public TrainingResponseDTO createTraining(NewTrainingRequestDTO dto) {
        log.info("Creating training profile. traineeId={}, trainerId={}",
                dto.getTraineeId(), dto.getTrainerId());
        Trainee trainee = traineeDao.findById(dto.getTraineeId())
                .orElseThrow(() ->
                        new EntityNotFoundException("Trainee not found. id=" + dto.getTraineeId()));
        Trainer trainer = trainerDao.findById(dto.getTrainerId())
                .orElseThrow(() ->
                        new EntityNotFoundException("Trainer not found. id=" + dto.getTrainerId()));
        Training training = trainingMapper.toEntity(dto);
        training.setTrainee(trainee);
        training.setTrainer(trainer);
        training.setTrainingType(getTrainingType(dto.getTrainingTypeName()));
        Training saved = trainingDao.save(training);
        log.info("Training profile created successfully. id={}", saved.getId());
        return trainingMapper.toDTO(saved);
    }

    @Transactional(readOnly = true)
    public TrainingResponseDTO getTraining(Long id) {
        log.debug("Selecting training profile. id={}", id);
        return trainingMapper.toDTO(trainingDao.findById(id)
                .orElseThrow(() ->
                        new EntityNotFoundException("Training not found. id=" + id)));
    }

    @Transactional(readOnly = true)
    public List<TrainingResponseDTO> getTraineeTrainings(
            String traineeUsername,
            LocalDate fromDate,
            LocalDate toDate,
            String trainerName,
            String trainingType
    ) {
        return trainingDao.findByTraineeUsernameAndCriteria(
                        traineeUsername,
                        fromDate,
                        toDate,
                        trainerName,
                        trainingType
                )
                .stream()
                .map(trainingMapper::toDTO)
                .toList();
    }

    private TrainingType getTrainingType(String trainingTypeName) {
        return trainingTypeDao.findByName(trainingTypeName)
                .orElseThrow(() ->
                        new EntityNotFoundException("Training type not found: " + trainingTypeName));
    }
}