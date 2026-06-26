package com.berruhanedar.app.gym_springboot.service;

import com.berruhanedar.app.gym_springboot.dao.TraineeDao;
import com.berruhanedar.app.gym_springboot.dao.TrainerDao;
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
    public void authenticateTrainee(String username, String password) {
        traineeDao.findByUsername(username)
                .filter(trainee -> trainee.getPassword().equals(password))
                .orElseThrow(() -> new AuthenticationException("Invalid trainee username or password."));
    }

    @Transactional(readOnly = true)
    public void authenticateTrainer(String username, String password) {
        trainerDao.findByUsername(username)
                .filter(trainer -> trainer.getPassword().equals(password))
                .orElseThrow(() -> new AuthenticationException("Invalid trainer username or password."));
    }
}