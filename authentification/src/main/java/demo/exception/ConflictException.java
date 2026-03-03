package demo.exception;

import org.springframework.http.HttpStatus;

/** Thrown when a resource already exists (e.g. duplicate e-mail). → 409 */
public class ConflictException extends ApiException {
    public ConflictException(String message) {
        super(message, HttpStatus.CONFLICT);
    }
}
