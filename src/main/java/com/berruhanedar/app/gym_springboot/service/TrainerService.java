package com.berruhanedar.app.gym_springboot.service;

import com.berruhanedar.app.gym_springboot.dao.TrainerDao;
import com.berruhanedar.app.gym_springboot.dao.TrainingTypeDao;
import com.berruhanedar.app.gym_springboot.dto.*;
import com.berruhanedar.app.gym_springboot.entity.Trainer;
import com.berruhanedar.app.gym_springboot.entity.TrainingType;
import com.berruhanedar.app.gym_springboot.exception.AuthenticationException;
import com.berruhanedar.app.gym_springboot.exception.EntityNotFoundException;
import com.berruhanedar.app.gym_springboot.mapper.TrainerMapper;
import com.berruhanedar.app.gym_springboot.util.CredentialGenerator;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Slf4j
@Service
public class TrainerService {

    private TrainerDao trainerDao;
    private TrainingTypeDao trainingTypeDao;
    private TrainerMapper trainerMapper;
    private CredentialGenerator credentialGenerator;

    @Autowired
    public void setTrainerDao(TrainerDao trainerDao) {
        this.trainerDao = trainerDao;
    }

    @Autowired
    public void setTrainingTypeDao(TrainingTypeDao trainingTypeDao) {
        this.trainingTypeDao = trainingTypeDao;
    }

    @Autowired
    public void setTrainerMapper(TrainerMapper trainerMapper) {
        this.trainerMapper = trainerMapper;
    }

    @Autowired
    public void setCredentialGenerator(CredentialGenerator credentialGenerator) {
        this.credentialGenerator = credentialGenerator;
    }

    @Transactional
    public RegistrationResponseDTO createTrainer(NewTrainerRequestDTO dto) {
        log.info("Creating trainer profile for {} {}", dto.getFirstName(), dto.getLastName());
        TrainingType specialization = findTrainingTypeByName(dto.getSpecializationName());
        Trainer trainer = trainerMapper.toEntity(dto);
        trainer.setSpecialization(specialization);
        trainer.setUsername(credentialGenerator.generateUsername(dto.getFirstName(), dto.getLastName()));
        trainer.setPassword(credentialGenerator.generatePassword());
        trainer.setIsActive(true);
        Trainer saved = trainerDao.save(trainer);
        log.info("Trainer profile created successfully. id={}", saved.getId());
        return trainerMapper.toRegistrationResponseDTO(saved);
    }

    @Transactional(readOnly = true)
    public TrainerResponseDTO getTrainerByUsername(String username) {
        log.debug("Selecting trainer profile. username={}", username);
        Trainer trainer = findTrainerByUsername(username);
        validateTrainerOwnsProfile(trainer);
        return trainerMapper.toDTO(trainer);
    }

    @Transactional
    public TrainerResponseDTO updateTrainer(UpdateTrainerRequestDTO dto) {
        log.info("Updating trainer profile. username={}", dto.getUsername());
        Trainer trainer = findTrainerByUsername(dto.getUsername());
        validateTrainerOwnsProfile(trainer);
        TrainingType specialization = findTrainingTypeByName(dto.getSpecializationName());
        trainerMapper.updateFromDTO(dto, trainer);
        trainer.setSpecialization(specialization);
        Trainer updated = trainerDao.update(trainer);
        log.info("Trainer profile updated successfully. username={}", updated.getUsername());
        return trainerMapper.toDTO(updated);
    }

    @Transactional
    public void changeActivationStatus(UpdateActivationStatusDTO dto) {
        log.info("Updating trainer activation status. username={}, isActive={}", dto.getUsername(), dto.getIsActive());
        Trainer trainer = findTrainerByUsername(dto.getUsername());
        validateTrainerOwnsProfile(trainer);
        trainer.setIsActive(dto.getIsActive());
        trainerDao.update(trainer);
        log.info("Trainer activation status updated successfully. username={}, isActive={}", dto.getUsername(), dto.getIsActive());
    }

    @Transactional(readOnly = true)
    public List<TrainerSummaryDTO> getTrainersNotAssignedToTrainee(String traineeUsername) {
        log.info("Getting active trainers not assigned to trainee. traineeUsername={}", traineeUsername);

        return trainerDao.findTrainersNotAssignedToTrainee(traineeUsername)
                .stream()
                .filter(Trainer::getIsActive)
                .map(trainerMapper::toTrainerSummaryDTO)
                .toList();
    }

    private Trainer findTrainerByUsername(String username) {
        return trainerDao.findByUsername(username).orElseThrow(() -> new EntityNotFoundException("Trainer not found: " + username));
    }

    private void validateTrainerOwnsProfile(Trainer trainer) {
        String authenticatedUsername = SecurityContextHolder.getContext().getAuthentication().getName();
        if (!trainer.getUsername().equals(authenticatedUsername)) {
            throw new AuthenticationException("Trainer is not authorized to access this profile.");
        }
    }

    private TrainingType findTrainingTypeByName(String specializationName) {
        return trainingTypeDao.findByName(specializationName).orElseThrow(() -> new EntityNotFoundException("Training type not found: " + specializationName));
    }
}