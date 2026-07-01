package com.berruhanedar.app.gym_springboot.service;

import com.berruhanedar.app.gym_springboot.dao.TraineeDao;
import com.berruhanedar.app.gym_springboot.dao.TrainerDao;
import com.berruhanedar.app.gym_springboot.dto.*;
import com.berruhanedar.app.gym_springboot.entity.Trainee;
import com.berruhanedar.app.gym_springboot.entity.Trainer;
import com.berruhanedar.app.gym_springboot.exception.AuthenticationException;
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
    public RegistrationResponseDTO createTrainee(@Valid NewTraineeRequestDTO dto) {
        log.info("Creating trainee profile for {} {}", dto.getFirstName(), dto.getLastName());
        Trainee trainee = traineeMapper.toEntity(dto);
        trainee.setUsername(credentialGenerator.generateUsername(dto.getFirstName(), dto.getLastName()));
        trainee.setPassword(credentialGenerator.generatePassword());
        trainee.setIsActive(true);
        Trainee saved = traineeDao.save(trainee);
        log.info("Trainee profile created successfully. id={}", saved.getId());
        return traineeMapper.toRegistrationResponseDTO(saved);
    }

    @Transactional(readOnly = true)
    public TraineeResponseDTO getTraineeByUsername(CredentialsDTO credentials, String username) {
        authenticationService.authenticate(credentials);
        log.debug("Selecting trainee profile. username={}", username);
        Trainee trainee = findTraineeByUsername(username);
        validateTraineeOwnsProfile(credentials, trainee);
        return traineeMapper.toDTO(trainee);
    }

    @Transactional
    public TraineeResponseDTO updateTrainee(CredentialsDTO credentials, @Valid UpdateTraineeRequestDTO dto) {
        authenticationService.authenticate(credentials);
        log.info("Updating trainee profile. username={}", dto.getUsername());
        Trainee trainee = findTraineeByUsername(dto.getUsername());
        validateTraineeOwnsProfile(credentials, trainee);
        traineeMapper.updateFromDTO(dto, trainee);
        Trainee updated = traineeDao.update(trainee);
        log.info("Trainee profile updated successfully. username={}", updated.getUsername());
        return traineeMapper.toDTO(updated);
    }

    @Transactional
    public void deleteTrainee(CredentialsDTO credentials, Long id) {
        authenticationService.authenticate(credentials);
        log.info("Deleting trainee profile. id={}", id);
        Trainee trainee = findTraineeById(id);
        validateTraineeOwnsProfile(credentials, trainee);
        traineeDao.delete(trainee);
        log.info("Trainee profile deleted successfully. id={}", id);
    }

    @Transactional
    public List<TrainerSummaryDTO> updateTraineeTrainers(CredentialsDTO credentials, @Valid UpdateTraineeTrainersRequestDTO dto) {
        authenticationService.authenticate(credentials);
        log.info("Updating trainee trainers list. username={}", dto.getTraineeUsername());
        Trainee trainee = findTraineeByUsername(dto.getTraineeUsername());
        validateTraineeOwnsProfile(credentials, trainee);
        List<Trainer> trainers = dto.getTrainers()
                .stream()
                .map(trainerDto -> trainerDao.findByUsername(trainerDto.getUsername()).orElseThrow(() -> new EntityNotFoundException("Trainer not found: " + trainerDto.getUsername())))
                .toList();
        trainee.setTrainers(new HashSet<>(trainers));
        Trainee updated = traineeDao.update(trainee);
        return updated.getTrainers()
                .stream()
                .map(traineeMapper::toTrainerSummaryDTO)
                .toList();
    }

    @Transactional(readOnly = true)
    public TraineeResponseDTO getTrainee(CredentialsDTO credentials, Long id) {
        authenticationService.authenticate(credentials);
        log.debug("Selecting trainee profile. id={}", id);
        Trainee trainee = findTraineeById(id);
        validateTraineeOwnsProfile(credentials, trainee);
        return traineeMapper.toDTO(trainee);
    }

    @Transactional
    public TraineeResponseDTO changeActivationStatus(CredentialsDTO credentials) {
        authenticationService.authenticate(credentials);
        log.info("Changing trainee activation status. username={}", credentials.getUsername());
        Trainee trainee = findTraineeByUsername(credentials.getUsername());
        trainee.setIsActive(!trainee.getIsActive());
        Trainee updated = traineeDao.update(trainee);
        log.info("Trainee activation status changed. username={}, isActive={}", credentials.getUsername(), updated.getIsActive());
        return traineeMapper.toDTO(updated);
    }

    @Transactional
    public void deleteTraineeByUsername(CredentialsDTO credentials, String username) {
        authenticationService.authenticate(credentials);
        log.info("Deleting trainee profile. username={}", username);
        Trainee trainee = findTraineeByUsername(username);
        validateTraineeOwnsProfile(credentials, trainee);
        traineeDao.delete(trainee);
        log.info("Trainee profile deleted successfully. username={}", username);
    }

    private Trainee findTraineeById(Long id) {
        return traineeDao.findById(id).orElseThrow(() -> new EntityNotFoundException("Trainee not found. id=" + id));
    }

    private Trainee findTraineeByUsername(String username) {
        return traineeDao.findByUsername(username).orElseThrow(() -> new EntityNotFoundException("Trainee not found: " + username));
    }

    private void validateTraineeOwnsProfile(CredentialsDTO credentials, Trainee trainee) {
        if (!trainee.getUsername().equals(credentials.getUsername())) {
            throw new AuthenticationException("Trainee is not authorized to access this profile.");
        }
    }

}