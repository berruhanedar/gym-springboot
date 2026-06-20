package com.berruhanedar.app.gym_springboot.util;

import com.berruhanedar.app.gym_springboot.dao.TraineeDao;
import com.berruhanedar.app.gym_springboot.dao.TrainerDao;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.security.SecureRandom;

@Component
public class CredentialGenerator {

    private static final String PASSWORD_CHARACTERS =
            "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789";

    private static final String USERNAME_SEPARATOR = ".";

    private static final int PASSWORD_LENGTH = 10;

    private final SecureRandom secureRandom = new SecureRandom();

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

    public String generateUsername(String firstName, String lastName) {

        String baseUsername =
                firstName.trim() + USERNAME_SEPARATOR + lastName.trim();

        String username = baseUsername;
        int suffix = 1;

        while (traineeDao.existsByUsername(username)
                || trainerDao.existsByUsername(username)) {

            username = baseUsername + suffix;
            suffix++;
        }

        return username;
    }

    public String generatePassword() {

        StringBuilder password = new StringBuilder(PASSWORD_LENGTH);

        for (int i = 0; i < PASSWORD_LENGTH; i++) {
            password.append(
                    PASSWORD_CHARACTERS.charAt(
                            secureRandom.nextInt(PASSWORD_CHARACTERS.length())
                    )
            );
        }

        return password.toString();
    }
}