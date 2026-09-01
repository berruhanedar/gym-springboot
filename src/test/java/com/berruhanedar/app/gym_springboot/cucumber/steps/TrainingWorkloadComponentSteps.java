package com.berruhanedar.app.gym_springboot.cucumber.steps;

import com.berruhanedar.app.gym_springboot.dao.TraineeDao;
import com.berruhanedar.app.gym_springboot.dao.TrainerDao;
import com.berruhanedar.app.gym_springboot.dao.TrainingDao;
import com.berruhanedar.app.gym_springboot.dao.TrainingTypeDao;
import com.berruhanedar.app.gym_springboot.dto.NewTrainingRequestDTO;
import com.berruhanedar.app.gym_springboot.dto.TrainerWorkloadRequestDTO;
import com.berruhanedar.app.gym_springboot.entity.Trainee;
import com.berruhanedar.app.gym_springboot.entity.Trainer;
import com.berruhanedar.app.gym_springboot.entity.Training;
import com.berruhanedar.app.gym_springboot.entity.TrainingType;
import com.berruhanedar.app.gym_springboot.mapper.TrainingMapper;
import com.berruhanedar.app.gym_springboot.mapper.TrainingTypeMapper;
import com.berruhanedar.app.gym_springboot.messaging.TrainerWorkloadProducer;
import com.berruhanedar.app.gym_springboot.service.TrainingService;
import io.cucumber.java.Before;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import org.mockito.Mockito;
import org.springframework.jms.core.JmsTemplate;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public class TrainingWorkloadComponentSteps {

    private JmsTemplate jmsTemplate;
    private TrainingService trainingService;

    private TrainingDao trainingDao;
    private TraineeDao traineeDao;
    private TrainerDao trainerDao;
    private TrainingMapper trainingMapper;

    private NewTrainingRequestDTO request;

    @Before("@component and @workload")
    public void setUp() {

        jmsTemplate = Mockito.mock(JmsTemplate.class);
        TrainerWorkloadProducer producer = new TrainerWorkloadProducer(jmsTemplate);

        org.springframework.test.util.ReflectionTestUtils.setField(producer, "trainerWorkloadQueue", "trainer.workload.queue");

        trainingDao = Mockito.mock(TrainingDao.class);
        traineeDao = Mockito.mock(TraineeDao.class);
        trainerDao = Mockito.mock(TrainerDao.class);
        TrainingTypeDao trainingTypeDao = Mockito.mock(TrainingTypeDao.class);
        trainingMapper = Mockito.mock(TrainingMapper.class);
        TrainingTypeMapper trainingTypeMapper = Mockito.mock(TrainingTypeMapper.class);

        trainingService = new TrainingService();
        trainingService.setTrainingDao(trainingDao);
        trainingService.setTraineeDao(traineeDao);
        trainingService.setTrainerDao(trainerDao);
        trainingService.setTrainingTypeDao(trainingTypeDao);
        trainingService.setTrainingMapper(trainingMapper);
        trainingService.setTrainingTypeMapper(trainingTypeMapper);
        trainingService.setTrainerWorkloadProducer(producer);
    }

    @Given("valid training data for workload publishing")
    public void validTrainingDataForWorkloadPublishing() {

        request = new NewTrainingRequestDTO();
        request.setTraineeUsername("john.smith");
        request.setTrainerUsername("david.brown");
        request.setTrainingName("Yoga Training");
        request.setTrainingDate(LocalDate.now().plusDays(1));
        request.setTrainingDuration(60);

        Trainee trainee = new Trainee();
        trainee.setUsername("john.smith");

        TrainingType yoga = new TrainingType();
        yoga.setTrainingTypeName("Yoga");

        Trainer trainer = new Trainer();
        trainer.setUsername("david.brown");
        trainer.setFirstName("David");
        trainer.setLastName("Brown");
        trainer.setIsActive(true);
        trainer.setSpecialization(yoga);

        Training training = new Training();
        training.setTrainingName("Yoga Training");
        training.setTrainingDate(request.getTrainingDate());
        training.setTrainingDuration(60);

        TrainerWorkloadRequestDTO workload = new TrainerWorkloadRequestDTO();

        Mockito.when(traineeDao.findByUsername("john.smith")).thenReturn(Optional.of(trainee));
        Mockito.when(trainerDao.findByUsername("david.brown")).thenReturn(Optional.of(trainer));
        Mockito.when(trainingMapper.toEntity(request)).thenReturn(training);
        Mockito.when(trainingDao.save(training)).thenReturn(training);
        Mockito.when(trainingMapper.toTrainerWorkloadRequestDTO(training)).thenReturn(workload);

        SecurityContextHolder.getContext().setAuthentication(new UsernamePasswordAuthenticationToken("david.brown", null, List.of()));
    }

    @When("a training is created")
    public void aTrainingIsCreated() {
        trainingService.createTraining(request);
    }

    @Then("a workload message should be sent to the mocked queue")
    public void workloadMessageShouldBeSentToMockedQueue() {
        Mockito.verify(jmsTemplate).convertAndSend(
                Mockito.eq("trainer.workload.queue"),
                Mockito.any(TrainerWorkloadRequestDTO.class),
                Mockito.any()
        );
    }
}