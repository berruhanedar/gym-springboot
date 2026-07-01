package com.berruhanedar.app.gym_springboot.exception.handler;

import com.berruhanedar.app.gym_springboot.exception.BaseException;
import com.berruhanedar.app.gym_springboot.exception.IBaseException;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;


@RestControllerAdvice
public class BaseExceptionHandler {

    @ExceptionHandler(BaseException.class)
    public ResponseEntity<IBaseException> handleBaseException(BaseException ex) {
        return new ResponseEntity<>(ex, ex.getHttpStatus());
    }
}