package com.example.auth.web;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record RegisterRequest(
        @NotBlank @Email @Size(max = 255) String email,

        // BCrypt only uses the first 72 bytes, so we cap the length there.
        @NotBlank @Size(min = 12, max = 72, message = "must be 12 to 72 characters") String password) {
}
