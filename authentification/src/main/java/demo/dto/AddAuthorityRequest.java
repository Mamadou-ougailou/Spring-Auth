package demo.dto;

import jakarta.validation.constraints.NotBlank;

/** Request body for POST /admin/identities/{email}/authorities. */
public record AddAuthorityRequest(
        @NotBlank(message = "provider is required")
        String provider,

        @NotBlank(message = "secret is required")
        String secret
) {}
