package demo.exception;

import org.springframework.http.HttpStatus;

/** Thrown when an authenticated user is not authorized for a requested resource (→ 403) */
public class ForbiddenException extends ApiException {
    public ForbiddenException(String message) {
        super(message, HttpStatus.FORBIDDEN);
    }
}
