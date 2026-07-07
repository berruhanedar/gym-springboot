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

    public GymFacade(TraineeService traineeService,
                     TrainerService trainerService,
                     TrainingService trainingService,
                     AuthenticationService authenticationService) {
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

    public String authenticate(CredentialsDTO credentials) {
        return authenticationService.login(credentials);
    }

    public void changePassword(ChangePasswordRequestDTO request) {
        authenticationService.changePassword(request);
    }

    public TraineeResponseDTO updateTrainee(UpdateTraineeRequestDTO dto) {
        return traineeService.updateTrainee(dto);
    }

    public TraineeResponseDTO getTraineeByUsername(String username) {
        return traineeService.getTraineeByUsername(username);
    }

    public void changeTraineeActivationStatus(UpdateActivationStatusDTO dto) {
        traineeService.changeTraineeActivationStatus(dto);
    }

    public void deleteTraineeByUsername(String username) {
        traineeService.deleteTraineeByUsername(username);
    }

    public List<TrainerSummaryDTO> updateTraineeTrainers(UpdateTraineeTrainersRequestDTO dto) {
        return traineeService.updateTraineeTrainers(dto);
    }

    public TrainerResponseDTO updateTrainer(UpdateTrainerRequestDTO dto) {
        return trainerService.updateTrainer(dto);
    }

    public TrainerResponseDTO getTrainerByUsername(String username) {
        return trainerService.getTrainerByUsername(username);
    }

    public void changeTrainerActivationStatus(UpdateActivationStatusDTO dto) {
        trainerService.changeActivationStatus(dto);
    }

    public List<TrainerSummaryDTO> getTrainersNotAssignedToTrainee(String traineeUsername) {
        return trainerService.getTrainersNotAssignedToTrainee(traineeUsername);
    }

    public void createTraining(NewTrainingRequestDTO dto) {
        trainingService.createTraining(dto);
    }

    public List<TraineeTrainingResponseDTO> getTraineeTrainings(String traineeUsername, TraineeTrainingsFilterDTO filter) {
        return trainingService.getTraineeTrainings(traineeUsername, filter);
    }

    public List<TrainerTrainingResponseDTO> getTrainerTrainings(String trainerUsername, TrainerTrainingsFilterDTO filter) {
        return trainingService.getTrainerTrainings(trainerUsername, filter);
    }

    public List<TrainingTypeResponseDTO> getTrainingTypes() {
        return trainingService.getTrainingTypes();
    }
}