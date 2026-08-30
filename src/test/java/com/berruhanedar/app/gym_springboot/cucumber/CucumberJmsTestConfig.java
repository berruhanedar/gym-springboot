package com.berruhanedar.app.gym_springboot.cucumber;

import jakarta.jms.ConnectionFactory;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.jms.core.JmsTemplate;
import org.springframework.jms.support.converter.MessageConverter;

@TestConfiguration
public class CucumberJmsTestConfig {

    @Bean
    public JmsTemplate cucumberJmsTemplate(ConnectionFactory connectionFactory, MessageConverter messageConverter) {

        JmsTemplate jmsTemplate = new JmsTemplate(connectionFactory);

        jmsTemplate.setMessageConverter(messageConverter);
        jmsTemplate.setReceiveTimeout(5000);

        return jmsTemplate;
    }
}