package demo.controller;

import demo.model.Identity;
import demo.service.RegistrationService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
public class RegistrationController {

    private final RegistrationService registrationService;

    public RegistrationController(RegistrationService registrationService) {
        this.registrationService = registrationService;
    }

    /**
     * POST /register
     * Body: { "email": "...", "password": "..." }
     */
    @PostMapping("/register")
    public ResponseEntity<Object> register(@RequestBody Map<String, String> body) {
        String email = body.get("email");
        String password = body.get("password");

        if (email == null || password == null) {
            return new ResponseEntity<>(
                    Map.of("error", "email and password are required"),
                    HttpStatus.BAD_REQUEST);
        }

        Identity identity = registrationService.register(email, password);

        return new ResponseEntity<>(
                Map.of("message", "User registered. Check your e-mail to verify your account.",
                        "userId", identity.getId()),
                HttpStatus.CREATED);
    }

    /**
     * GET /verify?tokenId=...&t=...
     */
    @GetMapping("/verify")
    public ResponseEntity<Object> verify(@RequestParam String tokenId,
                                         @RequestParam String t) {
        registrationService.verify(tokenId, t);
        return new ResponseEntity<>(
                Map.of("message", "E-mail verified successfully!"),
                HttpStatus.OK);
    }
}
