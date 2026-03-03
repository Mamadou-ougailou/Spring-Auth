package demo.exception;

import org.springframework.http.HttpStatus;

/** Thrown when authentication fails (bad credentials, expired token, etc.). → 401 */
public class UnauthorizedException extends ApiException {
    public UnauthorizedException(String message) {
        super(message, HttpStatus.UNAUTHORIZED);
    }
}
