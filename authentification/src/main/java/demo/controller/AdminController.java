package demo.controller;

import demo.dto.AddAuthorityRequest;
import demo.dto.AddCredentialRequest;
import demo.dto.AddRoleRequest;
import demo.model.*;
import demo.service.AdminService;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/admin")
@Tag(name = "Admin", description = "Administrative endpoints for users, roles, authorities and tokens")
public class AdminController {
    private final AdminService adminService;

    public AdminController(AdminService adminService) {
        this.adminService = adminService;
    }

    // ===== IDENTITY MANAGEMENT =====

    @GetMapping("/identities")
    public ResponseEntity<Object> getAllIdentities(@RequestHeader("Authorization") String token) {
        return new ResponseEntity<>(adminService.getAllUsers(token), HttpStatus.OK);
    }

    @GetMapping("/identities/connected")
    public ResponseEntity<Object> getConnectedIdentities(@RequestHeader("Authorization") String token) {
        return new ResponseEntity<>(adminService.getAllConnectedUsers(token), HttpStatus.OK);
    }

    @DeleteMapping("/identities/{email}")
    public ResponseEntity<Object> deleteIdentity(@RequestHeader("Authorization") String token,
            @PathVariable String email) {
        adminService.deleteUser(token, email);
        return new ResponseEntity<>("Identity deleted successfully", HttpStatus.OK);
    }

    // ===== CREDENTIAL (ROLE) MANAGEMENT =====

    @GetMapping("/credentials")
    public ResponseEntity<Object> getAllCredentials(@RequestHeader("Authorization") String token) {
        return new ResponseEntity<>(adminService.getAllCredentials(token), HttpStatus.OK);
    }

    @PostMapping("/credentials")
    public ResponseEntity<Object> addCredential(@RequestHeader("Authorization") String token,
            @Valid @RequestBody AddCredentialRequest request) {
        adminService.addCredential(token, request.name());
        return new ResponseEntity<>("Credential added successfully", HttpStatus.OK);
    }

    @PostMapping("/identities/{email}/credentials")
    public ResponseEntity<Object> addCredentialToIdentity(@RequestHeader("Authorization") String token,
            @PathVariable String email,
            @Valid @RequestBody AddRoleRequest request) {
        adminService.addRoleToUser(token, email, request.roleName());
        return new ResponseEntity<>("Role added successfully", HttpStatus.OK);
    }

    @DeleteMapping("/identities/{email}/credentials/{roleName}")
    public ResponseEntity<Object> removeCredentialFromIdentity(@RequestHeader("Authorization") String token,
            @PathVariable String email,
            @PathVariable String roleName) {
        adminService.removeRoleFromUser(token, email, roleName);
        return new ResponseEntity<>("Credential removed successfully", HttpStatus.OK);
    }

    // ===== AUTHORITY (LOGIN METHOD) MANAGEMENT =====

    @GetMapping("/authorities")
    public ResponseEntity<Object> getAllAuthorities(@RequestHeader("Authorization") String token) {
        return new ResponseEntity<>(adminService.getAllAuthorities(token), HttpStatus.OK);
    }

    @PostMapping("/identities/{email}/authorities")
    public ResponseEntity<Object> addAuthorityToIdentity(@RequestHeader("Authorization") String token,
            @PathVariable String email,
            @Valid @RequestBody AddAuthorityRequest request) {
        Authority.Provider provider = Authority.Provider.valueOf(request.provider().toUpperCase());
        adminService.addAuthorityToUser(token, email, provider, request.secret());
        return new ResponseEntity<>("Authority added successfully", HttpStatus.OK);
    }

    @DeleteMapping("/identities/{email}/authorities/{provider}")
    public ResponseEntity<Object> removeAuthorityFromIdentity(@RequestHeader("Authorization") String token,
            @PathVariable String email,
            @PathVariable String provider) {
        Authority.Provider providerEnum = Authority.Provider.valueOf(provider.toUpperCase());
        adminService.removeAuthorityFromUser(token, email, providerEnum);
        return new ResponseEntity<>("Authority removed successfully", HttpStatus.OK);
    }

    // ===== TOKEN MANAGEMENT =====

    @GetMapping("/tokens")
    public ResponseEntity<Object> getAllTokens(@RequestHeader("Authorization") String token) {
        return new ResponseEntity<>(adminService.getAllTokens(token), HttpStatus.OK);
    }

    @DeleteMapping("/tokens/{token}")
    public ResponseEntity<Object> deleteToken(@RequestHeader("Authorization") String token,
            @PathVariable("token") String tokenToDelete) {
        adminService.deleteToken(token, tokenToDelete);
        return new ResponseEntity<>("Token deleted successfully", HttpStatus.OK);
    }
}