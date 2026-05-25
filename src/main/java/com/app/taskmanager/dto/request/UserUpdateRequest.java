package com.app.taskmanager.dto.request;

import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record UserUpdateRequest(
        @Size(min = 3, max = 20, message = "size must be between 3 and 20")
        @Pattern(
                regexp = "^[a-zA-Z0-9_]+$",
                message = "must only contain letters, numbers, and underscores"
        )
        String username,

        @Size(min = 8, max = 100, message = "size must be between 8 and 100")
        @Pattern(
                regexp = "^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d).*$",
                message = "must contain at least one uppercase letter, one lowercase letter, and one number"
        )
        String password,

        @Email
        @Size(max = 100)
        String email
)
{
    @AssertTrue(message = "At least one field must be provided")
    public boolean isAtLeastOneFieldPresent() {
        return username != null || password != null || email != null;
    }
}
