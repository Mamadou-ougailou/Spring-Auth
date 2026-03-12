package demo.dto;

import jakarta.validation.constraints.NotBlank;

/** Request body for POST /admin/credentials. */
public record AddCredentialRequest(
        @NotBlank(message = "name is required")
        String name
) {}
