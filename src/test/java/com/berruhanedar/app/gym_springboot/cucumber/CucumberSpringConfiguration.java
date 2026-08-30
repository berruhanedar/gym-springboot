package com.berruhanedar.app.gym_springboot.cucumber;

import io.cucumber.spring.CucumberContextConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;

@CucumberContextConfiguration
@SpringBootTest
@Import(CucumberJmsTestConfig.class)
public class CucumberSpringConfiguration {
}