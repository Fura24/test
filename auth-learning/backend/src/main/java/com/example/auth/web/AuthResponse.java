package com.example.auth.web;

public record AuthResponse(String accessToken, String tokenType, long expiresInSeconds) {
}
