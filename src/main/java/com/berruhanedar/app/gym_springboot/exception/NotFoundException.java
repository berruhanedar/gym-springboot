package com.berruhanedar.app.gym_springboot.exception;

public class NotFoundException extends BadRequestException {

    public NotFoundException(String message) {
        super(message);
    }
}