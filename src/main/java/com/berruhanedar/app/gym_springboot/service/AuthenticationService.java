package com.berruhanedar.app.gym_springboot.service;

import com.berruhanedar.app.gym_springboot.dao.TraineeDao;
import com.berruhanedar.app.gym_springboot.dao.TrainerDao;
import com.berruhanedar.app.gym_springboot.dto.ChangePasswordRequestDTO;
import com.berruhanedar.app.gym_springboot.dto.CredentialsDTO;
import com.berruhanedar.app.gym_springboot.exception.AuthenticationException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.function.Consumer;

@Service
public class AuthenticationService {

    private TraineeDao traineeDao;
    private TrainerDao trainerDao;
    private JwtService jwtService;

    @Autowired
    public void setTraineeDao(TraineeDao traineeDao) {
        this.traineeDao = traineeDao;
    }

    @Autowired
    public void setTrainerDao(TrainerDao trainerDao) {
        this.trainerDao = trainerDao;
    }

    @Autowired
    public void setJwtService(JwtService jwtService) {
        this.jwtService = jwtService;
    }

    @Transactional(readOnly = true)
    public String login(CredentialsDTO credentials) {
        authenticate(credentials);
        return jwtService.generateToken(credentials.getUsername());
    }

    @Transactional(readOnly = true)
    public void authenticate(CredentialsDTO credentials) {
        boolean authenticated =
                traineeDao.findByUsername(credentials.getUsername())
                        .filter(trainee -> trainee.getPassword().equals(credentials.getPassword()))
                        .isPresent()
                        ||
                        trainerDao.findByUsername(credentials.getUsername())
                                .filter(trainer -> trainer.getPassword().equals(credentials.getPassword()))
                                .isPresent();

        if (!authenticated) {
            throw new AuthenticationException("Invalid username or password.");
        }
    }

    @Transactional
    public void changePassword(ChangePasswordRequestDTO request) {
        traineeDao.findByUsername(request.getUsername())
                .ifPresentOrElse(
                        trainee -> updatePassword(
                                trainee.getPassword(),
                                request.getOldPassword(),
                                request.getNewPassword(),
                                trainee::setPassword,
                                () -> traineeDao.update(trainee)
                        ),
                        () -> trainerDao.findByUsername(request.getUsername())
                                .ifPresentOrElse(
                                        trainer -> updatePassword(
                                                trainer.getPassword(),
                                                request.getOldPassword(),
                                                request.getNewPassword(),
                                                trainer::setPassword,
                                                () -> trainerDao.update(trainer)
                                        ),
                                        () -> {
                                            throw new AuthenticationException("Invalid username or password.");
                                        }
                                )
                );
    }

    private void updatePassword(String currentPassword, String oldPassword, String newPassword, Consumer<String> passwordSetter, Runnable saveAction) {
        if (!currentPassword.equals(oldPassword)) {
            throw new AuthenticationException("Invalid username or password.");
        }

        passwordSetter.accept(newPassword);
        saveAction.run();
    }
}