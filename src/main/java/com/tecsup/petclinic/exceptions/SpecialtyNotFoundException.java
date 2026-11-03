package com.tecsup.petclinic.exceptions;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.NOT_FOUND)
public class SpecialtyNotFoundException extends RuntimeException {
    public SpecialtyNotFoundException(String message) {
        super(message);
    }
}
