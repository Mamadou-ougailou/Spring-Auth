package demo.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

/** Shared request body for POST /register and POST /auth/email/login. */
public record RegisterRequest(
        @NotBlank(message = "email is required")
        @Email(message = "email must be valid")
        String email,

        @NotBlank(message = "name is required")
        String name,

        @NotBlank(message = "password is required")
        String password
) {}
