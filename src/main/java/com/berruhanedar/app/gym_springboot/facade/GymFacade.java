package com.berruhanedar.app.gym_springboot.facade;

import com.berruhanedar.app.gym_springboot.dto.*;
import com.berruhanedar.app.gym_springboot.service.*;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class GymFacade {

    private final TraineeService traineeService;
    private final TrainerService trainerService;
    private final TrainingService trainingService;
    private final AuthenticationService authenticationService;

    public GymFacade(TraineeService traineeService, TrainerService trainerService, TrainingService trainingService, AuthenticationService authenticationService) {
        this.traineeService = traineeService;
        this.trainerService = trainerService;
        this.trainingService = trainingService;
        this.authenticationService = authenticationService;
    }

    public RegistrationResponseDTO createTrainee(NewTraineeRequestDTO dto) {
        return traineeService.createTrainee(dto);
    }

    public RegistrationResponseDTO createTrainer(NewTrainerRequestDTO dto) {
        return trainerService.createTrainer(dto);
    }

    public void authenticate(CredentialsDTO credentials) {
        authenticationService.authenticate(credentials);
    }

    public void changePassword(ChangePasswordRequestDTO request) {
        authenticationService.changePassword(request);
    }

    public TraineeResponseDTO updateTrainee(CredentialsDTO credentials, UpdateTraineeRequestDTO dto) {
        return traineeService.updateTrainee(credentials, dto);
    }

    public TraineeResponseDTO getTraineeByUsername(CredentialsDTO credentials, String username) {
        return traineeService.getTraineeByUsername(credentials, username);
    }

    public void changeTraineeActivationStatus(CredentialsDTO credentials, UpdateActivationStatusDTO dto) {
        traineeService.changeTraineeActivationStatus(credentials, dto);
    }

    public void deleteTraineeByUsername(CredentialsDTO credentials, String username) {
        traineeService.deleteTraineeByUsername(credentials, username);
    }

    public List<TrainerSummaryDTO> updateTraineeTrainers(CredentialsDTO credentials, UpdateTraineeTrainersRequestDTO dto) {
        return traineeService.updateTraineeTrainers(credentials, dto);
    }

    public TrainerResponseDTO updateTrainer(CredentialsDTO credentials, UpdateTrainerRequestDTO dto) {
        return trainerService.updateTrainer(credentials, dto);
    }

    public TrainerResponseDTO getTrainerByUsername(CredentialsDTO credentials, String username) {
        return trainerService.getTrainerByUsername(credentials, username);
    }

    public void changeTrainerActivationStatus(CredentialsDTO credentials, UpdateActivationStatusDTO dto) {
        trainerService.changeActivationStatus(credentials, dto);
    }

    public List<TrainerResponseDTO> getTrainersNotAssignedToTrainee(CredentialsDTO credentials, String traineeUsername) {
        return trainerService.getTrainersNotAssignedToTrainee(credentials, traineeUsername);
    }

    public void createTraining(CredentialsDTO trainerCredentials, NewTrainingRequestDTO dto) {
        trainingService.createTraining(trainerCredentials, dto);
    }

    public List<TrainingResponseDTO> getTraineeTrainings(CredentialsDTO traineeCredentials, String traineeUsername, TraineeTrainingsFilterDTO filter) {
        return trainingService.getTraineeTrainings(traineeCredentials, traineeUsername, filter);
    }

    public List<TrainingResponseDTO> getTrainerTrainings(CredentialsDTO trainerCredentials, String trainerUsername, TrainerTrainingsFilterDTO filter) {
        return trainingService.getTrainerTrainings(trainerCredentials, trainerUsername, filter);
    }

    public List<TrainingTypeResponseDTO> getTrainingTypes() {
        return trainingService.getTrainingTypes();
    }
}