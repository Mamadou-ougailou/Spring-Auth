package demo.controller;

import demo.dto.ChangePasswordRequest;
import demo.dto.LoginRequest;
import demo.model.Identity;
import demo.model.Token;
import demo.service.AuthentificationService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/auth")
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
     * Returns 200 if the token is valid, 401 otherwise.
     */
    @GetMapping("/validate")
    public ResponseEntity<Void> validate(@RequestHeader(value = "Authorization", required = false) String token) {
        if (token == null || token.isBlank()) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        try {
            authService.getUserInfo(token);
            return ResponseEntity.ok().build();
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
    }
}