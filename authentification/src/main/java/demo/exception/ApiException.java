package demo.exception;

import org.springframework.http.HttpStatus;

/**
 * Base exception for all business-rule violations.
 * Carries the HTTP status that the global handler should return.
 */
public abstract class ApiException extends RuntimeException {

    private final HttpStatus status;

    protected ApiException(String message, HttpStatus status) {
        super(message);
        this.status = status;
    }

    public HttpStatus getStatus() {
        return status;
    }
}
