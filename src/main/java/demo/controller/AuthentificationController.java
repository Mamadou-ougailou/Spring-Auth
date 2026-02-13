package demo.controller;

import demo.model.Identity;
import demo.model.Token;
import demo.service.AuthentificationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/auth")
public class AuthentificationController {

    @Autowired
    private AuthentificationService authService;

    @PostMapping("/email/login")
    public ResponseEntity<Object> emailLogin(@RequestBody Map<String, String> body) {
        String email = body.get("email");
        String password = body.get("password");

        if (email == null || password == null) {
            return new ResponseEntity<>("Email and password are required", HttpStatus.BAD_REQUEST);
        }

        Token token = authService.emailLogin(email, password);

        Map<String, Object> response = new HashMap<>();
        response.put("message", "Login successful");
        response.put("token", token.getToken());
        response.put("expirationTime", token.getExpirationTime());

        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    @PostMapping("/logout")
    public ResponseEntity<Object> logout(@RequestHeader("Authorization") String token) {
        authService.logout(token);
        return new ResponseEntity<>("Logged out successfully", HttpStatus.OK);
    }

    @PostMapping("/change-password")
    public ResponseEntity<Object> changePassword(@RequestHeader("Authorization") String token,
            @RequestBody Map<String, String> body) {
        String email = body.get("email");
        String oldPassword = body.get("oldPassword");
        String newPassword = body.get("newPassword");

        if (email == null || oldPassword == null || newPassword == null) {
            return new ResponseEntity<>("Email, old password, and new password are required", HttpStatus.BAD_REQUEST);
        }

        authService.changePassword(email, oldPassword, newPassword);

        return new ResponseEntity<>("Password changed successfully", HttpStatus.OK);
    }

    @GetMapping("/me")
    public ResponseEntity<Object> getUserInfo(@RequestHeader("Authorization") String token) {
        Identity identity = authService.getUserInfo(token);

        Map<String, Object> response = new HashMap<>();
        response.put("email", identity.getEmail());
        response.put("name", identity.getName());

        return new ResponseEntity<>(response, HttpStatus.OK);
    }
}