package com.berruhanedar.app.gym_springboot.service;

import com.berruhanedar.app.gym_springboot.dao.*;
import com.berruhanedar.app.gym_springboot.dto.*;
import com.berruhanedar.app.gym_springboot.entity.Training;
import com.berruhanedar.app.gym_springboot.entity.TrainingType;
import com.berruhanedar.app.gym_springboot.exception.EntityNotFoundException;
import com.berruhanedar.app.gym_springboot.mapper.TrainingMapper;
import com.berruhanedar.app.gym_springboot.storage.IdGenerator;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class TrainingService {
    private TrainingDao trainingDao;
    private TraineeDao traineeDao;
    private TrainerDao trainerDao;
    private TrainingTypeDao trainingTypeDao;
    private TrainingMapper trainingMapper;
    private IdGenerator idGenerator;

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
    public void setIdGenerator(IdGenerator idGenerator) {
        this.idGenerator = idGenerator;
    }

    public TrainingResponseDTO createTraining(NewTrainingRequestDTO dto) {
        log.info("Creating training profile. traineeId={}, trainerId={}", dto.getTraineeId(), dto.getTrainerId());

        traineeDao.findById(dto.getTraineeId())
                .orElseThrow(() -> new EntityNotFoundException("Trainee not found. id=" + dto.getTraineeId()));

        trainerDao.findById(dto.getTrainerId())
                .orElseThrow(() -> new EntityNotFoundException("Trainer not found. id=" + dto.getTrainerId()));

        Training training = trainingMapper.toEntity(dto);
        training.setId(idGenerator.nextTrainingId());
        training.setTrainingType(getOrCreateTrainingType(dto.getTrainingTypeName()));

        Training saved = trainingDao.save(training);

        log.info("Training profile created successfully. id={}", saved.getId());
        return trainingMapper.toDTO(saved);
    }

    public TrainingResponseDTO getTraining(Long id) {
        log.debug("Selecting training profile. id={}", id);

        return trainingMapper.toDTO(trainingDao.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Training not found. id=" + id)));
    }

    private TrainingType getOrCreateTrainingType(String trainingTypeName) {
        return trainingTypeDao.findByName(trainingTypeName)
                .orElseGet(() -> trainingTypeDao.save(
                        new TrainingType(idGenerator.nextTrainingTypeId(), trainingTypeName)
                ));
    }
}