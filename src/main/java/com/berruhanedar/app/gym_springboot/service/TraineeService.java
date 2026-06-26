package com.berruhanedar.app.gym_springboot.service;

import com.berruhanedar.app.gym_springboot.dao.TraineeDao;
import com.berruhanedar.app.gym_springboot.dao.TrainerDao;
import com.berruhanedar.app.gym_springboot.dto.*;
import com.berruhanedar.app.gym_springboot.entity.Trainee;
import com.berruhanedar.app.gym_springboot.entity.Trainer;
import com.berruhanedar.app.gym_springboot.exception.EntityNotFoundException;
import com.berruhanedar.app.gym_springboot.mapper.TraineeMapper;
import com.berruhanedar.app.gym_springboot.util.CredentialGenerator;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashSet;
import java.util.List;

@Slf4j
@Service
public class TraineeService {

    private TraineeDao traineeDao;
    private TrainerDao trainerDao;
    private TraineeMapper traineeMapper;
    private CredentialGenerator credentialGenerator;
    private AuthenticationService authenticationService;

    @Autowired
    public void setTraineeDao(TraineeDao traineeDao) {
        this.traineeDao = traineeDao;
    }

    @Autowired
    public void setTrainerDao(TrainerDao trainerDao) {
        this.trainerDao = trainerDao;
    }

    @Autowired
    public void setTraineeMapper(TraineeMapper traineeMapper) {
        this.traineeMapper = traineeMapper;
    }

    @Autowired
    public void setCredentialGenerator(CredentialGenerator credentialGenerator) {
        this.credentialGenerator = credentialGenerator;
    }

    @Autowired
    public void setAuthenticationService(AuthenticationService authenticationService) {
        this.authenticationService = authenticationService;
    }

    @Transactional
    public TraineeResponseDTO createTrainee(@Valid NewTraineeRequestDTO dto) {
        log.info("Creating trainee profile for {} {}", dto.getFirstName(), dto.getLastName());
        Trainee trainee = traineeMapper.toEntity(dto);
        trainee.setUsername(credentialGenerator.generateUsername(dto.getFirstName(), dto.getLastName()));
        trainee.setPassword(credentialGenerator.generatePassword());
        trainee.setIsActive(true);
        Trainee saved = traineeDao.save(trainee);
        log.info("Trainee profile created successfully. id={}", saved.getId());
        return traineeMapper.toDTO(saved);
    }

    @Transactional
    public TraineeResponseDTO updateTrainee(CredentialsDTO credentials, @Valid UpdateTraineeRequestDTO dto) {
        authenticationService.authenticateTrainee(credentials);
        log.info("Updating trainee profile. id={}", dto.getId());
        Trainee trainee = traineeDao.findById(dto.getId())
                .orElseThrow(() -> new EntityNotFoundException("Trainee not found. id=" + dto.getId()));
        String existingUsername = trainee.getUsername();
        String existingPassword = trainee.getPassword();
        traineeMapper.updateFromDTO(dto, trainee);
        trainee.setUsername(existingUsername);
        trainee.setPassword(existingPassword);
        Trainee updated = traineeDao.update(trainee);
        log.info("Trainee profile updated successfully. id={}", updated.getId());
        return traineeMapper.toDTO(updated);
    }

    @Transactional
    public void deleteTrainee(CredentialsDTO credentials, Long id) {
        authenticationService.authenticateTrainee(credentials);
        log.info("Deleting trainee profile. id={}", id);
        Trainee trainee = traineeDao.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Trainee not found. id=" + id));
        traineeDao.delete(trainee);
        log.info("Trainee profile deleted successfully. id={}", id);
    }

    @Transactional(readOnly = true)
    public TraineeResponseDTO getTrainee(CredentialsDTO credentials, Long id) {
        authenticationService.authenticateTrainee(credentials);
        log.debug("Selecting trainee profile. id={}", id);
        Trainee trainee = traineeDao.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Trainee not found. id=" + id));
        return traineeMapper.toDTO(trainee);
    }

    @Transactional(readOnly = true)
    public TraineeResponseDTO getTraineeByUsername(CredentialsDTO credentials, String username) {
        authenticationService.authenticateTrainee(credentials);
        Trainee trainee = traineeDao.findByUsername(username)
                .orElseThrow(() -> new EntityNotFoundException("Trainee not found: " + username));
        return traineeMapper.toDTO(trainee);
    }

    @Transactional
    public void changePassword(CredentialsDTO credentials, String newPassword) {
        authenticationService.authenticateTrainee(credentials);
        Trainee trainee = traineeDao.findByUsername(credentials.getUsername())
                .orElseThrow(() -> new EntityNotFoundException("Trainee not found."));
        trainee.setPassword(newPassword);
        traineeDao.update(trainee);
    }

    @Transactional
    public TraineeResponseDTO changeActivationStatus(CredentialsDTO credentials) {
        authenticationService.authenticateTrainee(credentials);
        log.info("Changing trainee activation status. username={}", credentials.getUsername());
        Trainee trainee = traineeDao.findByUsername(credentials.getUsername())
                .orElseThrow(() -> new EntityNotFoundException("Trainee not found: " + credentials.getUsername()));
        trainee.setIsActive(!trainee.getIsActive());
        Trainee updated = traineeDao.update(trainee);
        log.info("Trainee activation status changed. username={}, isActive={}",
                credentials.getUsername(), updated.getIsActive());
        return traineeMapper.toDTO(updated);
    }

    @Transactional
    public void deleteTraineeByUsername(CredentialsDTO credentials, String username) {
        authenticationService.authenticateTrainee(credentials);
        log.info("Deleting trainee profile. username={}", username);
        Trainee trainee = traineeDao.findByUsername(username)
                .orElseThrow(() -> new EntityNotFoundException("Trainee not found: " + username));
        traineeDao.delete(trainee);
        log.info("Trainee profile deleted successfully. username={}", username);
    }

    @Transactional
    public TraineeResponseDTO updateTraineeTrainers(CredentialsDTO credentials, UpdateTraineeTrainersRequestDTO dto) {
        authenticationService.authenticateTrainee(credentials);
        log.info("Updating trainee trainers list. username={}", dto.getTraineeUsername());
        Trainee trainee = traineeDao.findByUsername(dto.getTraineeUsername())
                .orElseThrow(() -> new EntityNotFoundException("Trainee not found: " + dto.getTraineeUsername()));
        List<Trainer> trainers = trainerDao.findAllByIds(dto.getTrainerIds());
        if (trainers.size() != dto.getTrainerIds().size()) {
            throw new EntityNotFoundException("One or more trainers not found.");
        }
        trainee.setTrainers(new HashSet<>(trainers));
        Trainee updated = traineeDao.update(trainee);
        log.info("Trainee trainers list updated successfully. username={}", dto.getTraineeUsername());
        return traineeMapper.toDTO(updated);
    }
}