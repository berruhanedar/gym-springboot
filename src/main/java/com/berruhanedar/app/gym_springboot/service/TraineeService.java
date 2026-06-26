package com.berruhanedar.app.gym_springboot.service;

import com.berruhanedar.app.gym_springboot.dao.TraineeDao;
import com.berruhanedar.app.gym_springboot.dto.*;
import com.berruhanedar.app.gym_springboot.entity.Trainee;
import com.berruhanedar.app.gym_springboot.exception.EntityNotFoundException;
import com.berruhanedar.app.gym_springboot.mapper.TraineeMapper;
import com.berruhanedar.app.gym_springboot.util.CredentialGenerator;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
public class TraineeService {
    private TraineeDao traineeDao;
    private TraineeMapper traineeMapper;
    private CredentialGenerator credentialGenerator;

    @Autowired
    public void setTraineeDao(TraineeDao traineeDao) {
        this.traineeDao = traineeDao;
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
    public TraineeResponseDTO createTrainee(NewTraineeRequestDTO dto) {
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
    public TraineeResponseDTO updateTrainee(UpdateTraineeRequestDTO dto) {
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
    public void deleteTrainee(Long id) {
        log.info("Deleting trainee profile. id={}", id);
        Trainee trainee = traineeDao.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Trainee not found. id=" + id));
        traineeDao.delete(trainee);
        log.info("Trainee profile deleted successfully. id={}", id);
    }

    @Transactional(readOnly = true)
    public TraineeResponseDTO getTrainee(Long id) {
        log.debug("Selecting trainee profile. id={}", id);
        return traineeMapper.toDTO(traineeDao.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Trainee not found. id=" + id)));
    }

    @Transactional(readOnly = true)
    public TraineeResponseDTO getTraineeByUsername(String username) {

        Trainee trainee = traineeDao.findByUsername(username)
                .orElseThrow(() ->
                        new EntityNotFoundException("Trainee not found: " + username));

        return traineeMapper.toDTO(trainee);
    }

    @Transactional
    public void changePassword(String username, String newPassword) {

        Trainee trainee = traineeDao.findByUsername(username)
                .orElseThrow(() ->
                        new EntityNotFoundException("Trainee not found."));

        trainee.setPassword(newPassword);

        traineeDao.update(trainee);
    }
}
