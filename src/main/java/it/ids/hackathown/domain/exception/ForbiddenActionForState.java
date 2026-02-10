package it.ids.hackathown.domain.exception;

import org.springframework.http.HttpStatus;

public class ForbiddenActionForState extends DomainException {

    public ForbiddenActionForState(String message) {
        super(HttpStatus.FORBIDDEN, message);
    }
}
