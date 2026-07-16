package com.berruhanedar.app.gym_springboot.monitoring;

import com.berruhanedar.app.gym_springboot.dao.TraineeDao;
import com.berruhanedar.app.gym_springboot.dao.TrainerDao;
import com.berruhanedar.app.gym_springboot.dao.TrainingDao;
import io.micrometer.core.instrument.simple.SimpleMeterRegistry;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.support.DefaultListableBeanFactory;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class GymMetricsTest {

    @Test
    void shouldRegisterAndIncrementCustomMetrics() {
        SimpleMeterRegistry registry = new SimpleMeterRegistry();
        TraineeDao traineeDao = mock(TraineeDao.class);
        TrainerDao trainerDao = mock(TrainerDao.class);
        TrainingDao trainingDao = mock(TrainingDao.class);
        when(traineeDao.count()).thenReturn(4L);
        when(trainerDao.count()).thenReturn(2L);
        when(trainingDao.count()).thenReturn(7L);

        DefaultListableBeanFactory beanFactory = new DefaultListableBeanFactory();
        beanFactory.registerSingleton("meterRegistry", registry);
        GymMetrics metrics = new GymMetrics(beanFactory.getBeanProvider(io.micrometer.core.instrument.MeterRegistry.class),
                traineeDao, trainerDao, trainingDao);
        metrics.recordSuccessfulLogin();
        metrics.recordFailedLogin();
        metrics.recordTraineeRegistration();
        metrics.recordTrainerRegistration();
        metrics.recordTrainingCreated();

        assertThat(registry.get("gym.authentication.attempts").tag("result", "success").counter().count()).isEqualTo(1);
        assertThat(registry.get("gym.authentication.attempts").tag("result", "failure").counter().count()).isEqualTo(1);
        assertThat(registry.get("gym.profile.registrations").tag("profile.type", "trainee").counter().count()).isEqualTo(1);
        assertThat(registry.get("gym.profile.registrations").tag("profile.type", "trainer").counter().count()).isEqualTo(1);
        assertThat(registry.get("gym.trainings.created").counter().count()).isEqualTo(1);
        assertThat(registry.get("gym.trainees.total").gauge().value()).isEqualTo(4);
        assertThat(registry.get("gym.trainers.total").gauge().value()).isEqualTo(2);
        assertThat(registry.get("gym.trainings.total").gauge().value()).isEqualTo(7);
    }
}
