package demo.controller;

import demo.dto.ChangePasswordRequest;
import demo.dto.LoginRequest;
import demo.model.Identity;
import demo.model.Token;
import demo.service.AuthentificationService;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/auth")
@Tag(name = "Authentication", description = "Login, token validation and account session endpoints")
public class AuthentificationController {
    private final AuthentificationService authService;

    public AuthentificationController(AuthentificationService authService) {
        this.authService = authService;
    }

    @PostMapping("/email/login")
    public ResponseEntity<Object> emailLogin(@Valid @RequestBody LoginRequest request) {
        Token token = authService.emailLogin(request.email(), request.password());

        return new ResponseEntity<>(
                Map.of("message", "Login successful",
                        "token", token.getToken(),
                        "expirationTime", token.getExpirationTime()),
                HttpStatus.OK);
    }

    @PostMapping("/logout")
    public ResponseEntity<Object> logout(@RequestHeader("Authorization") String token) {
        authService.logout(token);
        return new ResponseEntity<>("Logged out successfully", HttpStatus.OK);
    }

    @PostMapping("/change-password")
    public ResponseEntity<Object> changePassword(@RequestHeader("Authorization") String token,
            @Valid @RequestBody ChangePasswordRequest request) {
        authService.changePassword(request.email(), request.oldPassword(), request.newPassword());
        return new ResponseEntity<>("Password changed successfully", HttpStatus.OK);
    }

    @GetMapping("/me")
    public ResponseEntity<Object> getUserInfo(@RequestHeader("Authorization") String token) {
        Identity identity = authService.getUserInfo(token);

        return new ResponseEntity<>(
                Map.of("email", identity.getEmail(),
                        "name", identity.getName()),
                HttpStatus.OK);
    }

    /**
     * GET /auth/validate — called by Nginx auth_request subrequest.
     * Returns 200 if the token is valid and the user is authorized for the
     * requested target service (header `X-Target-Service`), 401 if token invalid,
     * 403 if authenticated but not authorized for the target service.
     */
    @GetMapping("/validate")
    public ResponseEntity<Void> validate(
            @RequestHeader(value = "Authorization", required = false) String token,
            @RequestHeader(value = "X-Target-Service", required = false) String targetService) {
        // Delegate validation + authorization to the service. GlobalExceptionHandler
        // will translate thrown ApiException subclasses into proper HTTP responses.
        authService.validateTokenForTarget(token, targetService);
        return ResponseEntity.ok().build();
    }
}