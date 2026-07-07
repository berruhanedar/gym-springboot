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
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.context.SecurityContextHolder;
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

    @Transactional
    public RegistrationResponseDTO createTrainee(NewTraineeRequestDTO dto) {
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
    public TraineeResponseDTO getTraineeByUsername(String username) {
        log.debug("Selecting trainee profile. username={}", username);
        Trainee trainee = findTraineeByUsername(username);
        validateTraineeOwnsProfile(trainee);
        return traineeMapper.toDTO(trainee);
    }

    @Transactional
    public TraineeResponseDTO updateTrainee(UpdateTraineeRequestDTO dto) {
        log.info("Updating trainee profile. username={}", dto.getUsername());
        Trainee trainee = findTraineeByUsername(dto.getUsername());
        validateTraineeOwnsProfile(trainee);
        traineeMapper.updateFromDTO(dto, trainee);
        Trainee updated = traineeDao.update(trainee);
        log.info("Trainee profile updated successfully. username={}", updated.getUsername());
        return traineeMapper.toDTO(updated);
    }

    @Transactional
    public List<TrainerSummaryDTO> updateTraineeTrainers(UpdateTraineeTrainersRequestDTO dto) {
        log.info("Updating trainee trainers list. username={}", dto.getTraineeUsername());
        Trainee trainee = findTraineeByUsername(dto.getTraineeUsername());
        validateTraineeOwnsProfile(trainee);
        List<Trainer> trainers = dto.getTrainers()
                .stream()
                .map(trainerDto -> trainerDao.findByUsername(trainerDto.getUsername())
                        .orElseThrow(() -> new EntityNotFoundException("Trainer not found: " + trainerDto.getUsername())))
                .toList();
        trainee.setTrainers(new HashSet<>(trainers));
        Trainee updated = traineeDao.update(trainee);
        return updated.getTrainers()
                .stream()
                .map(traineeMapper::toTrainerSummaryDTO)
                .toList();
    }

    @Transactional
    public void changeTraineeActivationStatus(UpdateActivationStatusDTO dto) {
        log.info("Updating trainee activation status. username={}, isActive={}", dto.getUsername(), dto.getIsActive());
        Trainee trainee = findTraineeByUsername(dto.getUsername());
        validateTraineeOwnsProfile(trainee);
        trainee.setIsActive(dto.getIsActive());
        traineeDao.update(trainee);
        log.info("Trainee activation status updated successfully. username={}, isActive={}", dto.getUsername(), dto.getIsActive());
    }

    @Transactional
    public void deleteTraineeByUsername(String username) {
        log.info("Deleting trainee profile. username={}", username);
        Trainee trainee = findTraineeByUsername(username);
        validateTraineeOwnsProfile(trainee);
        traineeDao.delete(trainee);
        log.info("Trainee profile deleted successfully. username={}", username);
    }

    private Trainee findTraineeByUsername(String username) {
        return traineeDao.findByUsername(username)
                .orElseThrow(() -> new EntityNotFoundException("Trainee not found: " + username));
    }

    private void validateTraineeOwnsProfile(Trainee trainee) {
        String authenticatedUsername = SecurityContextHolder.getContext().getAuthentication().getName();
        if (!trainee.getUsername().equals(authenticatedUsername)) {
            throw new AuthenticationException("Trainee is not authorized to access this profile.");
        }
    }
}