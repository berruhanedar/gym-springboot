package com.berruhanedar.app.gym_springboot.exception;

import org.springframework.http.HttpStatus;

public class WorkloadServiceUnavailableException extends BaseException {

    public WorkloadServiceUnavailableException(String message) {
        super(message);
    }

    @Override
    public HttpStatus getHttpStatus() {
        return HttpStatus.SERVICE_UNAVAILABLE;
    }
}