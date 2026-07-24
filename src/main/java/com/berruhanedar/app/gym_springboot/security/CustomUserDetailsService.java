package com.berruhanedar.app.gym_springboot.security;

import com.berruhanedar.app.gym_springboot.dao.TraineeDao;
import com.berruhanedar.app.gym_springboot.dao.TrainerDao;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Service
public class CustomUserDetailsService implements UserDetailsService {

    private final TraineeDao traineeDao;
    private final TrainerDao trainerDao;

    public CustomUserDetailsService(TraineeDao traineeDao, TrainerDao trainerDao) {
        this.traineeDao = traineeDao;
        this.trainerDao = trainerDao;
    }

    @Override
    public UserDetails loadUserByUsername(String username) {
        return traineeDao.findByUsername(username)
                .map(trainee -> User.builder()
                        .username(trainee.getUsername())
                        .password(trainee.getPassword())
                        .disabled(!Boolean.TRUE.equals(trainee.getIsActive()))
                        .authorities("ROLE_TRAINEE")
                        .build()
                )
                .orElseGet(() -> trainerDao.findByUsername(username)
                        .map(trainer -> User.builder()
                                .username(trainer.getUsername())
                                .password(trainer.getPassword())
                                .disabled(!Boolean.TRUE.equals(trainer.getIsActive()))
                                .authorities("ROLE_TRAINER")
                                .build()
                        )
                        .orElseThrow(() ->
                                new UsernameNotFoundException(
                                        "User not found: " + username
                                )
                        )
                );
    }
}