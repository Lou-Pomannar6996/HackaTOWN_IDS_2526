package it.ids.hackathown.domain.exception;

import org.springframework.http.HttpStatus;

public class DomainValidationException extends DomainException {

    public DomainValidationException(String message) {
        super(HttpStatus.BAD_REQUEST, message);
    }
}
