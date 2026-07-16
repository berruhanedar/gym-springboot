package com.berruhanedar.app.gym_springboot.monitoring;

import com.berruhanedar.app.gym_springboot.dao.TraineeDao;
import com.berruhanedar.app.gym_springboot.dao.TrainerDao;
import com.berruhanedar.app.gym_springboot.dao.TrainingDao;
import com.berruhanedar.app.gym_springboot.dao.TrainingTypeDao;
import org.junit.jupiter.api.Test;
import org.springframework.boot.actuate.health.Status;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class GymDataHealthIndicatorTest {

    @Test
    void shouldBeUpWhenTrainingTypesExist() {
        TraineeDao traineeDao = mock(TraineeDao.class);
        TrainerDao trainerDao = mock(TrainerDao.class);
        TrainingDao trainingDao = mock(TrainingDao.class);
        TrainingTypeDao trainingTypeDao = mock(TrainingTypeDao.class);
        when(traineeDao.count()).thenReturn(3L);
        when(trainerDao.count()).thenReturn(2L);
        when(trainingDao.count()).thenReturn(5L);
        when(trainingTypeDao.count()).thenReturn(4L);

        var health = new GymDataHealthIndicator(traineeDao, trainerDao, trainingDao, trainingTypeDao).health();

        assertThat(health.getStatus()).isEqualTo(Status.UP);
        assertThat(health.getDetails()).containsEntry("trainingTypes", 4L);
    }

    @Test
    void shouldBeDownWhenNoTrainingTypeExists() {
        TraineeDao traineeDao = mock(TraineeDao.class);
        TrainerDao trainerDao = mock(TrainerDao.class);
        TrainingDao trainingDao = mock(TrainingDao.class);
        TrainingTypeDao trainingTypeDao = mock(TrainingTypeDao.class);
        when(trainingTypeDao.count()).thenReturn(0L);

        var health = new GymDataHealthIndicator(traineeDao, trainerDao, trainingDao, trainingTypeDao).health();

        assertThat(health.getStatus()).isEqualTo(Status.DOWN);
        assertThat(health.getDetails()).containsEntry("reason", "No training types are configured");
    }
}
