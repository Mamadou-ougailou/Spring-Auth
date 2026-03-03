package demo.exception;

import org.springframework.http.HttpStatus;

/** Thrown when input validation fails. → 400 */
public class BadRequestException extends ApiException {
    public BadRequestException(String message) {
        super(message, HttpStatus.BAD_REQUEST);
    }
}
