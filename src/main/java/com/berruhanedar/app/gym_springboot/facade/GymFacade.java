package com.berruhanedar.app.gym_springboot.facade;

import com.berruhanedar.app.gym_springboot.dto.*;
import com.berruhanedar.app.gym_springboot.service.*;
import org.springframework.stereotype.Component;

@Component
public class GymFacade {
    private final TraineeService traineeService;
    private final TrainerService trainerService;
    private final TrainingService trainingService;

    public GymFacade(TraineeService traineeService, TrainerService trainerService, TrainingService trainingService) {
        this.traineeService = traineeService;
        this.trainerService = trainerService;
        this.trainingService = trainingService;
    }

    public TraineeResponseDTO createTrainee(NewTraineeRequestDTO dto) {
        return traineeService.createTrainee(dto);
    }

    public TraineeResponseDTO updateTrainee(UpdateTraineeRequestDTO dto) {
        return traineeService.updateTrainee(dto);
    }

    public void deleteTrainee(Long id) {
        traineeService.deleteTrainee(id);
    }

    public TraineeResponseDTO getTrainee(Long id) {
        return traineeService.getTrainee(id);
    }

    public TrainerResponseDTO createTrainer(NewTrainerRequestDTO dto) {
        return trainerService.createTrainer(dto);
    }

    public TrainerResponseDTO updateTrainer(UpdateTrainerRequestDTO dto) {
        return trainerService.updateTrainer(dto);
    }

    public TrainerResponseDTO getTrainer(Long id) {
        return trainerService.getTrainer(id);
    }

    public TrainingResponseDTO createTraining(NewTrainingRequestDTO dto) {
        return trainingService.createTraining(dto);
    }

    public TrainingResponseDTO getTraining(Long id) {
        return trainingService.getTraining(id);
    }
}