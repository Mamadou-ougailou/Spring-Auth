package demo.exception;

import org.springframework.http.HttpStatus;

/** Thrown when a requested resource does not exist. → 404 */
public class NotFoundException extends ApiException {
    public NotFoundException(String message) {
        super(message, HttpStatus.NOT_FOUND);
    }
}
