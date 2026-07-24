package com.berruhanedar.app.gym_springboot.security;

import com.berruhanedar.app.gym_springboot.dao.TraineeDao;
import com.berruhanedar.app.gym_springboot.dao.TrainerDao;
import com.berruhanedar.app.gym_springboot.entity.Trainee;
import com.berruhanedar.app.gym_springboot.entity.Trainer;
import org.junit.jupiter.api.Test;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

class CustomUserDetailsServiceTest {

    private final TraineeDao traineeDao = mock(TraineeDao.class);
    private final TrainerDao trainerDao = mock(TrainerDao.class);
    private final CustomUserDetailsService service =
            new CustomUserDetailsService(traineeDao, trainerDao);

    @Test
    void shouldLoadActiveTraineeWithTraineeRole() {
        Trainee trainee = new Trainee();
        trainee.setUsername("trainee.user");
        trainee.setPassword("encoded");
        trainee.setIsActive(true);
        when(traineeDao.findByUsername("trainee.user"))
                .thenReturn(Optional.of(trainee));

        var details = service.loadUserByUsername("trainee.user");

        assertThat(details.getUsername()).isEqualTo("trainee.user");
        assertThat(details.getPassword()).isEqualTo("encoded");
        assertThat(details.isEnabled()).isTrue();
        assertThat(details.getAuthorities())
                .extracting(Object::toString)
                .containsExactly("ROLE_TRAINEE");
        verifyNoInteractions(trainerDao);
    }

    @Test
    void shouldLoadDisabledTrainerWithTrainerRole() {
        Trainer trainer = new Trainer();
        trainer.setUsername("trainer.user");
        trainer.setPassword("encoded");
        trainer.setIsActive(false);
        when(traineeDao.findByUsername("trainer.user"))
                .thenReturn(Optional.empty());
        when(trainerDao.findByUsername("trainer.user"))
                .thenReturn(Optional.of(trainer));

        var details = service.loadUserByUsername("trainer.user");

        assertThat(details.isEnabled()).isFalse();
        assertThat(details.getAuthorities())
                .extracting(Object::toString)
                .containsExactly("ROLE_TRAINER");
    }

    @Test
    void shouldThrowWhenUserDoesNotExist() {
        when(traineeDao.findByUsername("missing")).thenReturn(Optional.empty());
        when(trainerDao.findByUsername("missing")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.loadUserByUsername("missing"))
                .isInstanceOf(UsernameNotFoundException.class)
                .hasMessageContaining("missing");
    }
}
