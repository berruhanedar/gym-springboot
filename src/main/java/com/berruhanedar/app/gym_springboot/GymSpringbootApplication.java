package com.berruhanedar.app.gym_springboot;

import com.berruhanedar.app.gym_springboot.config.AppConfig;
import com.berruhanedar.app.gym_springboot.facade.GymFacade;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;

@Slf4j
public class GymSpringbootApplication {

    public static void main(String[] args) {
        try (AnnotationConfigApplicationContext context =
                     new AnnotationConfigApplicationContext(AppConfig.class)) {

            GymFacade gymFacade = context.getBean(GymFacade.class);

        }
    }
}