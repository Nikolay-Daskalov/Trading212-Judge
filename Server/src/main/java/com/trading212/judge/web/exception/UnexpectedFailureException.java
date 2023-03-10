package com.trading212.judge.web.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(code = HttpStatus.INTERNAL_SERVER_ERROR)
public class UnexpectedFailureException extends RuntimeException {

    private final static String MESSAGE = "Oops! Something went wrong.";

    public UnexpectedFailureException() {
        super(MESSAGE);
    }
}
