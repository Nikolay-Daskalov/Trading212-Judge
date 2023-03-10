package com.trading212.judge.web.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(code = HttpStatus.BAD_REQUEST)
public class AuthorizationHeaderNotValidException extends RuntimeException {

    private static final String MESSAGE = "Authorization header is not valid.";

    public AuthorizationHeaderNotValidException() {
        super(MESSAGE);
    }
}
