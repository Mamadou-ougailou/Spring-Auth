package demo.controller;

import demo.model.Credential;
import demo.model.Autority;
import demo.model.Identity;
import demo.service.AuthService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/auth")
public class IdentifyController {

    @Autowired
    private AuthService authService;

    // Endpoint : Enregistrer un nouvel utilisateur
    @PostMapping("/create")
    public ResponseEntity<Object> register(@RequestBody Map<String, String> body) {
        String email = body.get("email");
        String name = body.get("name");
        String password = body.get("password");

        if (email == null || email.trim().isEmpty()) {
            return new ResponseEntity<>("Email is required", HttpStatus.BAD_REQUEST);
        }

        if (name == null || name.trim().isEmpty()) {
            return new ResponseEntity<>("Name is required", HttpStatus.BAD_REQUEST);
        }

        if (password == null || password.length() < 6) {
            return new ResponseEntity<>("Password must be at least 6 characters", HttpStatus.BAD_REQUEST);
        }

        if (authService.userExists(email)) {
            return new ResponseEntity<>("User already exists", HttpStatus.CONFLICT);
        }

        authService.registerUser(email, name, password);

        Map<String, Object> response = new HashMap<>();
        response.put("message", "User registered successfully");
        response.put("email", email);
        response.put("name", name);

        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }

    // Endpoint : Se connecter
    @PostMapping("/login")
    public ResponseEntity<Object> login(@RequestBody Map<String, String> body) {
        String email = body.get("email");
        String password = body.get("password");

        if (email == null || password == null) {
            return new ResponseEntity<>("Email and password are required", HttpStatus.BAD_REQUEST);
        }

        if (!authService.userExists(email)) {
            return new ResponseEntity<>("User not found", HttpStatus.NOT_FOUND);
        }

        // Vérifier le mot de passe
        if (!authService.verifyPassword(email, password)) {
            return new ResponseEntity<>("Invalid credentials", HttpStatus.UNAUTHORIZED);
        }

        Autority token = authService.generateToken(email);

        Map<String, Object> response = new HashMap<>();
        response.put("message", "Login successful");
        response.put("token", token.getToken());
        response.put("expirationTime", token.getExpirationTime());

        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    // Endpoint : Vérifier si un token est valide
    @GetMapping("/validate")
    public ResponseEntity<Object> validateToken(@RequestHeader("Authorization") String token) {
        if (authService.isTokenValid(token)) {
            return new ResponseEntity<>("Token is valid", HttpStatus.OK);
        } else {
            return new ResponseEntity<>("Token is invalid or expired", HttpStatus.UNAUTHORIZED);
        }
    }

    // Endpoint : Consulter les informations du token actuel
    @GetMapping("/me")
    public ResponseEntity<Object> getCurrentUser(@RequestHeader("Authorization") String token) {
        Autority credentialToken = authService.getTokenInfo(token);

        if (credentialToken == null) {
            return new ResponseEntity<>("Token not found", HttpStatus.NOT_FOUND);
        }

        if (credentialToken.isExpired()) {
            return new ResponseEntity<>("Token expired", HttpStatus.UNAUTHORIZED);
        }

        // Récupérer les informations de l'utilisateur
        Identity user = authService.getAllUsers().stream()
                .filter(u -> u.getEmail().equals(credentialToken.getEmail()))
                .findFirst()
                .orElse(null);

        if (user == null) {
            return new ResponseEntity<>("User not found", HttpStatus.NOT_FOUND);
        }

        Map<String, Object> response = new HashMap<>();
        response.put("email", user.getEmail());
        response.put("name", user.getName());
        response.put("token", credentialToken.getToken());
        response.put("expirationTime", credentialToken.getExpirationTime());
        response.put("isExpired", credentialToken.isExpired());

        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    // Endpoint : Se déconnecter (révoquer le token)
    @PostMapping("/logout")
    public ResponseEntity<Object> logout(@RequestHeader("Authorization") String token) {
        authService.revokeToken(token);
        return new ResponseEntity<>("Logged out successfully", HttpStatus.OK);
    }

    // Endpoint : Afficher tous les utilisateurs
    @GetMapping("/users")
    public ResponseEntity<Object> getAllUsers() {
        java.util.Collection<Identity> users = authService.getAllUsers();

        Map<String, Object> response = new HashMap<>();
        response.put("count", users.size());
        response.put("users", users);

        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    // Endpoint : Afficher tous les credentials
    @GetMapping("/credentials")
    public ResponseEntity<Object> getAllCredentials() {
        java.util.Collection<Credential> credentials = authService.getAllCredentials();

        Map<String, Object> response = new HashMap<>();
        response.put("count", credentials.size());
        response.put("credentials", credentials);

        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    // Endpoint : Supprimer un utilisateur
    @DeleteMapping("/delete/{email}")
    public ResponseEntity<Object> deleteUser(@PathVariable String email) {
        if (email == null || email.trim().isEmpty()) {
            return new ResponseEntity<>("Email is required", HttpStatus.BAD_REQUEST);
        }

        if (!authService.userExists(email)) {
            return new ResponseEntity<>("User not found", HttpStatus.NOT_FOUND);
        }

        boolean deleted = authService.deleteUser(email);

        if (deleted) {
            Map<String, Object> response = new HashMap<>();
            response.put("message", "User deleted successfully");
            response.put("email", email);

            return new ResponseEntity<>(response, HttpStatus.OK);
        } else {
            return new ResponseEntity<>("Failed to delete user", HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
}