package com.berruhanedar.app.gym_springboot;

import com.berruhanedar.app.gym_springboot.config.AppConfig;
import com.berruhanedar.app.gym_springboot.dto.NewTraineeRequestDTO;
import com.berruhanedar.app.gym_springboot.dto.TraineeResponseDTO;
import com.berruhanedar.app.gym_springboot.facade.GymFacade;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;

import java.time.LocalDate;

@Slf4j
public class GymSpringbootApplication {

    public static void main(String[] args) {
        try (AnnotationConfigApplicationContext context =
                     new AnnotationConfigApplicationContext(AppConfig.class)) {

            GymFacade gymFacade = context.getBean(GymFacade.class);

            NewTraineeRequestDTO request = new NewTraineeRequestDTO();
            request.setFirstName("Demo");
            request.setLastName("User");
            request.setDateOfBirth(LocalDate.of(2000, 1, 1));
            request.setAddress("Istanbul");

            TraineeResponseDTO response = gymFacade.createTrainee(request);

            log.info("Demo trainee created. id={}, username={}, active={}",
                    response.getId(),
                    response.getUsername(),
                    response.getIsActive());
        }
    }
}