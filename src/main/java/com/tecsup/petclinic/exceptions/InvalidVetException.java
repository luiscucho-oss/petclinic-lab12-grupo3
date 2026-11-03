package com.tecsup.petclinic.exceptions;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.BAD_REQUEST)
public class InvalidVetException extends RuntimeException {
    public InvalidVetException(String message) {
        super(message);
    }
}
