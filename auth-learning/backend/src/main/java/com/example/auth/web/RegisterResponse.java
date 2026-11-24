package com.example.auth.web;

import java.util.List;
import java.util.UUID;

/** What we send back after registering. Notice: no password, no hash. */
public record RegisterResponse(UUID id, String email, List<String> roles) {
}
