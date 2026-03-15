package demo.dto;

import java.util.List;

public record AdminIdentityResponse(
        Long id,
        String email,
        String name,
        boolean verified,
        List<String> roles
) {
}
