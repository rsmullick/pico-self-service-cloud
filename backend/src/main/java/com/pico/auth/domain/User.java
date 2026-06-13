package com.pico.auth.domain;

public record User(String id, String email, String displayName, UserRole role) {}
