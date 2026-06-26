package com.berruhanedar.app.gym_springboot.service;

import com.berruhanedar.app.gym_springboot.dao.TrainerDao;
import com.berruhanedar.app.gym_springboot.dto.*;
import com.berruhanedar.app.gym_springboot.entity.Trainer;
import com.berruhanedar.app.gym_springboot.exception.EntityNotFoundException;
import com.berruhanedar.app.gym_springboot.mapper.TrainerMapper;
import com.berruhanedar.app.gym_springboot.storage.IdGenerator;
import com.berruhanedar.app.gym_springboot.util.CredentialGenerator;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
public class TrainerService {
    private TrainerDao trainerDao;
    private TrainerMapper trainerMapper;
    private CredentialGenerator credentialGenerator;

    @Autowired
    public void setTrainerDao(TrainerDao trainerDao) {
        this.trainerDao = trainerDao;
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
    public TrainerResponseDTO createTrainer(NewTrainerRequestDTO dto) {
        log.info("Creating trainer profile for {} {}", dto.getFirstName(), dto.getLastName());
        Trainer trainer = trainerMapper.toEntity(dto);
        trainer.setUsername(credentialGenerator.generateUsername(dto.getFirstName(), dto.getLastName()));
        trainer.setPassword(credentialGenerator.generatePassword());
        trainer.setIsActive(true);
        Trainer saved = trainerDao.save(trainer);
        log.info("Trainer profile created successfully. id={}", saved.getId());
        return trainerMapper.toDTO(saved);
    }

    public TrainerResponseDTO updateTrainer(UpdateTrainerRequestDTO dto) {
        log.info("Updating trainer profile. id={}", dto.getId());
        Trainer trainer = trainerDao.findById(dto.getId())
                .orElseThrow(() -> new EntityNotFoundException("Trainer not found. id=" + dto.getId()));
        String existingUsername = trainer.getUsername();
        String existingPassword = trainer.getPassword();
        trainerMapper.updateFromDTO(dto, trainer);
        trainer.setUsername(existingUsername);
        trainer.setPassword(existingPassword);
        Trainer updated = trainerDao.save(trainer);
        log.info("Trainer profile updated successfully. id={}", updated.getId());
        return trainerMapper.toDTO(updated);
    }

    public TrainerResponseDTO getTrainer(Long id) {
        log.debug("Selecting trainer profile. id={}", id);
        return trainerMapper.toDTO(trainerDao.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Trainer not found. id=" + id)));
    }
}
