package demo.dto;

import jakarta.validation.constraints.NotBlank;

/** Request body for POST /admin/identities/{email}/credentials. */
public record AddRoleRequest(
        @NotBlank(message = "roleName is required")
        String roleName
) {}
