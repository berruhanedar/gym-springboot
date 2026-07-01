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
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
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
    private AuthenticationService authenticationService;

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

    @Autowired
    public void setAuthenticationService(AuthenticationService authenticationService) {
        this.authenticationService = authenticationService;
    }

    @Transactional
    public RegistrationResponseDTO createTrainer(@Valid NewTrainerRequestDTO dto) {
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
    public TrainerResponseDTO getTrainerByUsername(CredentialsDTO credentials, String username) {
        authenticationService.authenticate(credentials);
        log.debug("Selecting trainer profile. username={}", username);
        Trainer trainer = findTrainerByUsername(username);
        validateTrainerOwnsProfile(credentials, trainer);
        return trainerMapper.toDTO(trainer);
    }

    @Transactional
    public TrainerResponseDTO updateTrainer(CredentialsDTO credentials, @Valid UpdateTrainerRequestDTO dto) {
        authenticationService.authenticate(credentials);
        log.info("Updating trainer profile. username={}", dto.getUsername());
        Trainer trainer = findTrainerByUsername(dto.getUsername());
        validateTrainerOwnsProfile(credentials, trainer);
        trainerMapper.updateFromDTO(dto, trainer);
        Trainer updated = trainerDao.update(trainer);
        log.info("Trainer profile updated successfully. username={}", updated.getUsername());
        return trainerMapper.toDTO(updated);
    }

    @Transactional(readOnly = true)
    public TrainerResponseDTO getTrainer(CredentialsDTO credentials, Long id) {
        authenticationService.authenticate(credentials);
        log.debug("Selecting trainer profile. id={}", id);
        Trainer trainer = findTrainerById(id);
        return trainerMapper.toDTO(trainer);
    }

    @Transactional
    public TrainerResponseDTO changeActivationStatus(CredentialsDTO credentials) {
        authenticationService.authenticate(credentials);
        log.info("Changing trainer activation status. username={}", credentials.getUsername());
        Trainer trainer = findTrainerByUsername(credentials.getUsername());
        trainer.setIsActive(!trainer.getIsActive());
        Trainer updated = trainerDao.update(trainer);
        log.info("Trainer activation status changed. username={}, isActive={}", credentials.getUsername(), updated.getIsActive());
        return trainerMapper.toDTO(updated);
    }

    @Transactional(readOnly = true)
    public List<TrainerResponseDTO> getTrainersNotAssignedToTrainee(CredentialsDTO credentials, String traineeUsername) {
        authenticationService.authenticate(credentials);
        log.info("Getting trainers not assigned to trainee. traineeUsername={}", traineeUsername);
        return trainerDao.findTrainersNotAssignedToTrainee(traineeUsername)
                .stream()
                .map(trainerMapper::toDTO)
                .toList();
    }

    private Trainer findTrainerById(Long id) {
        return trainerDao.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Trainer not found. id=" + id));
    }

    private Trainer findTrainerByUsername(String username) {
        return trainerDao.findByUsername(username)
                .orElseThrow(() -> new EntityNotFoundException("Trainer not found: " + username));
    }

    private TrainingType findTrainingTypeByName(String specializationName) {
        return trainingTypeDao.findByName(specializationName)
                .orElseThrow(() -> new EntityNotFoundException(
                        "Training type not found: " + specializationName));
    }

    private void validateTrainerOwnsProfile(CredentialsDTO credentials, Trainer trainer) {
        if (!trainer.getUsername().equals(credentials.getUsername())) {
            throw new AuthenticationException("Trainer is not authorized to access this profile.");
        }
    }

}