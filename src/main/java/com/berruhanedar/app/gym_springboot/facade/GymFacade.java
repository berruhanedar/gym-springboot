package com.berruhanedar.app.gym_springboot.facade;

import com.berruhanedar.app.gym_springboot.dto.*;
import com.berruhanedar.app.gym_springboot.service.*;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.util.List;

@Component
public class GymFacade {

    private final TraineeService traineeService;
    private final TrainerService trainerService;
    private final TrainingService trainingService;

    public GymFacade(TraineeService traineeService,
                     TrainerService trainerService,
                     TrainingService trainingService) {
        this.traineeService = traineeService;
        this.trainerService = trainerService;
        this.trainingService = trainingService;
    }

    public TraineeResponseDTO createTrainee(NewTraineeRequestDTO dto) {
        return traineeService.createTrainee(dto);
    }

    public TraineeResponseDTO updateTrainee(CredentialsDTO credentials, UpdateTraineeRequestDTO dto) {
        return traineeService.updateTrainee(credentials, dto);
    }

    public void deleteTrainee(CredentialsDTO credentials, Long id) {
        traineeService.deleteTrainee(credentials, id);
    }

    public TraineeResponseDTO getTrainee(CredentialsDTO credentials, Long id) {
        return traineeService.getTrainee(credentials, id);
    }

    public TraineeResponseDTO getTraineeByUsername(CredentialsDTO credentials, String username) {
        return traineeService.getTraineeByUsername(credentials, username);
    }

    public void changeTraineePassword(CredentialsDTO credentials, String newPassword) {
        traineeService.changePassword(credentials, newPassword);
    }

    public TraineeResponseDTO changeTraineeActivationStatus(CredentialsDTO credentials) {
        return traineeService.changeActivationStatus(credentials);
    }

    public void deleteTraineeByUsername(CredentialsDTO credentials, String username) {
        traineeService.deleteTraineeByUsername(credentials, username);
    }

    public TraineeResponseDTO updateTraineeTrainers(CredentialsDTO credentials,
                                                    UpdateTraineeTrainersRequestDTO dto) {
        return traineeService.updateTraineeTrainers(credentials, dto);
    }

    public TrainerResponseDTO createTrainer(NewTrainerRequestDTO dto) {
        return trainerService.createTrainer(dto);
    }

    public TrainerResponseDTO updateTrainer(CredentialsDTO credentials, UpdateTrainerRequestDTO dto) {
        return trainerService.updateTrainer(credentials, dto);
    }

    public TrainerResponseDTO getTrainer(CredentialsDTO credentials, Long id) {
        return trainerService.getTrainer(credentials, id);
    }

    public TrainerResponseDTO getTrainerByUsername(CredentialsDTO credentials, String username) {
        return trainerService.getTrainerByUsername(credentials, username);
    }

    public void changeTrainerPassword(CredentialsDTO credentials, String newPassword) {
        trainerService.changePassword(credentials, newPassword);
    }

    public TrainerResponseDTO changeTrainerActivationStatus(CredentialsDTO credentials) {
        return trainerService.changeActivationStatus(credentials);
    }

    public List<TrainerResponseDTO> getTrainersNotAssignedToTrainee(CredentialsDTO credentials,
                                                                    String traineeUsername) {
        return trainerService.getTrainersNotAssignedToTrainee(credentials, traineeUsername);
    }

    public TrainingResponseDTO createTraining(CredentialsDTO trainerCredentials,
                                              NewTrainingRequestDTO dto) {
        return trainingService.createTraining(trainerCredentials, dto);
    }

    public TrainingResponseDTO getTraining(CredentialsDTO credentials, Long id) {
        return trainingService.getTraining(credentials, id);
    }

    public List<TrainingResponseDTO> getTraineeTrainings(
            CredentialsDTO traineeCredentials,
            String traineeUsername,
            LocalDate fromDate,
            LocalDate toDate,
            String trainerName,
            String trainingType) {

        return trainingService.getTraineeTrainings(
                traineeCredentials,
                traineeUsername,
                fromDate,
                toDate,
                trainerName,
                trainingType);
    }

    public List<TrainingResponseDTO> getTrainerTrainings(
            CredentialsDTO trainerCredentials,
            String trainerUsername,
            LocalDate fromDate,
            LocalDate toDate,
            String traineeName) {

        return trainingService.getTrainerTrainings(
                trainerCredentials,
                trainerUsername,
                fromDate,
                toDate,
                traineeName);
    }
}