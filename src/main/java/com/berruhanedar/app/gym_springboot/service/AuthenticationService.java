package com.berruhanedar.app.gym_springboot.service;

import com.berruhanedar.app.gym_springboot.dao.TraineeDao;
import com.berruhanedar.app.gym_springboot.dao.TrainerDao;
import com.berruhanedar.app.gym_springboot.dto.CredentialsDTO;
import com.berruhanedar.app.gym_springboot.exception.AuthenticationException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AuthenticationService {

    private TraineeDao traineeDao;
    private TrainerDao trainerDao;

    @Autowired
    public void setTraineeDao(TraineeDao traineeDao) {
        this.traineeDao = traineeDao;
    }

    @Autowired
    public void setTrainerDao(TrainerDao trainerDao) {
        this.trainerDao = trainerDao;
    }

    @Transactional(readOnly = true)
    public void authenticateTrainee(CredentialsDTO credentials) {
        traineeDao.findByUsername(credentials.getUsername())
                .filter(trainee -> trainee.getPassword().equals(credentials.getPassword()))
                .orElseThrow(() ->
                        new AuthenticationException("Invalid trainee username or password."));
    }

    @Transactional(readOnly = true)
    public void authenticateTrainer(CredentialsDTO credentials) {
        trainerDao.findByUsername(credentials.getUsername())
                .filter(trainer -> trainer.getPassword().equals(credentials.getPassword()))
                .orElseThrow(() ->
                        new AuthenticationException("Invalid trainer username or password."));
    }
}