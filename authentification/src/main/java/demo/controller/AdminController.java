package demo.controller;

import demo.dto.AddAuthorityRequest;
import demo.dto.AddCredentialRequest;
import demo.dto.AddRoleRequest;
import demo.model.*;
import demo.service.AdminService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/admin")
public class AdminController {
    private final AdminService adminService;

    public AdminController(AdminService adminService) {
        this.adminService = adminService;
    }

    // ===== IDENTITY MANAGEMENT =====

    @GetMapping("/identities")
    public ResponseEntity<Object> getAllIdentities() {
        return new ResponseEntity<>(adminService.getAllUsers(), HttpStatus.OK);
    }

    @GetMapping("/identities/connected")
    public ResponseEntity<Object> getConnectedIdentities() {
        return new ResponseEntity<>(adminService.getAllConnectedUsers(), HttpStatus.OK);
    }

    @DeleteMapping("/identities/{email}")
    public ResponseEntity<Object> deleteIdentity(@PathVariable String email) {
        adminService.deleteUser(email);
        return new ResponseEntity<>("Identity deleted successfully", HttpStatus.OK);
    }

    // ===== CREDENTIAL (ROLE) MANAGEMENT =====

    @GetMapping("/credentials")
    public ResponseEntity<Object> getAllCredentials() {
        return new ResponseEntity<>(adminService.getAllCredentials(), HttpStatus.OK);
    }

    @PostMapping("/credentials")
    public ResponseEntity<Object> addCredential(@Valid @RequestBody AddCredentialRequest request) {
        adminService.addCredential(request.name());
        return new ResponseEntity<>("Credential added successfully", HttpStatus.OK);
    }

    @PostMapping("/identities/{email}/credentials")
    public ResponseEntity<Object> addCredentialToIdentity(@PathVariable String email,
            @Valid @RequestBody AddRoleRequest request) {
        adminService.addRoleToUser(email, request.roleName());
        return new ResponseEntity<>("Role added successfully", HttpStatus.OK);
    }

    @DeleteMapping("/identities/{email}/credentials/{roleName}")
    public ResponseEntity<Object> removeCredentialFromIdentity(@PathVariable String email,
            @PathVariable String roleName) {
        adminService.removeRoleFromUser(email, roleName);
        return new ResponseEntity<>("Credential removed successfully", HttpStatus.OK);
    }

    // ===== AUTHORITY (LOGIN METHOD) MANAGEMENT =====

    @GetMapping("/authorities")
    public ResponseEntity<Object> getAllAuthorities() {
        return new ResponseEntity<>(adminService.getAllAuthorities(), HttpStatus.OK);
    }

    @PostMapping("/identities/{email}/authorities")
    public ResponseEntity<Object> addAuthorityToIdentity(@PathVariable String email,
            @Valid @RequestBody AddAuthorityRequest request) {
        Authority.Provider provider = Authority.Provider.valueOf(request.provider().toUpperCase());
        adminService.addAuthorityToUser(email, provider, request.secret());
        return new ResponseEntity<>("Authority added successfully", HttpStatus.OK);
    }

    @DeleteMapping("/identities/{email}/authorities/{provider}")
    public ResponseEntity<Object> removeAuthorityFromIdentity(@PathVariable String email,
            @PathVariable String provider) {
        Authority.Provider providerEnum = Authority.Provider.valueOf(provider.toUpperCase());
        adminService.removeAuthorityFromUser(email, providerEnum);
        return new ResponseEntity<>("Authority removed successfully", HttpStatus.OK);
    }

    // ===== TOKEN MANAGEMENT =====

    @GetMapping("/tokens")
    public ResponseEntity<Object> getAllTokens() {
        return new ResponseEntity<>(adminService.getAllTokens(), HttpStatus.OK);
    }

    @DeleteMapping("/tokens/{token}")
    public ResponseEntity<Object> deleteToken(@PathVariable String token) {
        adminService.deleteToken(token);
        return new ResponseEntity<>("Token deleted successfully", HttpStatus.OK);
    }
}