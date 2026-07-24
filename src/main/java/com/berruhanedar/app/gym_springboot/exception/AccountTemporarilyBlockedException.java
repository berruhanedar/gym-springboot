package com.berruhanedar.app.gym_springboot.exception;

import org.springframework.http.HttpStatus;

public class AccountTemporarilyBlockedException extends BaseException {

    public AccountTemporarilyBlockedException(String message) {
        super(message);
    }

    @Override
    public HttpStatus getHttpStatus() {
        return HttpStatus.TOO_MANY_REQUESTS;
    }
}