package demo.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

/** Request body for POST /auth/change-password. */
public record ChangePasswordRequest(
        @NotBlank(message = "email is required")
        @Email(message = "email must be valid")
        String email,

        @NotBlank(message = "oldPassword is required")
        String oldPassword,

        @NotBlank(message = "newPassword is required")
        String newPassword
) {}
