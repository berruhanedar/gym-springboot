package com.berruhanedar.app.gym_springboot.service;

import com.berruhanedar.app.gym_springboot.dao.TraineeDao;
import com.berruhanedar.app.gym_springboot.dao.TrainerDao;
import com.berruhanedar.app.gym_springboot.dto.ChangePasswordRequestDTO;
import com.berruhanedar.app.gym_springboot.dto.CredentialsDTO;
import com.berruhanedar.app.gym_springboot.exception.AuthenticationException;
import com.berruhanedar.app.gym_springboot.monitoring.GymMetrics;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;

import java.util.function.Consumer;

@Service
public class AuthenticationService {
    private TraineeDao traineeDao;
    private TrainerDao trainerDao;
    private JwtService jwtService;
    private PasswordEncoder passwordEncoder;
    private AuthenticationManager authenticationManager;

    @Autowired(required = false)
    private GymMetrics gymMetrics;

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

    @Autowired
    public void setPasswordEncoder(PasswordEncoder passwordEncoder) {
        this.passwordEncoder = passwordEncoder;
    }

    @Autowired
    public void setAuthenticationManager(AuthenticationManager authenticationManager) {
        this.authenticationManager = authenticationManager;
    }

    @Transactional(readOnly = true)
    public String login(CredentialsDTO credentials) {
        try {
            Authentication authentication =
                    authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(credentials.getUsername(), credentials.getPassword()));
            if (gymMetrics != null) {
                gymMetrics.recordSuccessfulLogin();
            }
            return jwtService.generateToken(authentication.getName());
        } catch (org.springframework.security.core.AuthenticationException exception) {
            if (gymMetrics != null) {
                gymMetrics.recordFailedLogin();
            }
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

    private void updatePassword(String currentEncodedPassword, String oldRawPassword, String newRawPassword, Consumer<String> passwordSetter, Runnable saveAction) {
        if (!passwordEncoder.matches(oldRawPassword, currentEncodedPassword)) {
            throw new AuthenticationException("Invalid username or password.");
        }

        String newEncodedPassword = passwordEncoder.encode(newRawPassword);
        passwordSetter.accept(newEncodedPassword);
        saveAction.run();
    }

    public boolean isUserActive(String username) {
        return traineeDao.findByUsername(username)
                .map(trainee -> Boolean.TRUE.equals(trainee.getIsActive()))
                .or(() -> trainerDao.findByUsername(username)
                        .map(trainer -> Boolean.TRUE.equals(trainer.getIsActive())))
                .orElse(false);
    }

    @Transactional(readOnly = true)
    public boolean userExists(String username) {
        return traineeDao.findByUsername(username).isPresent()
                || trainerDao.findByUsername(username).isPresent();
    }
}