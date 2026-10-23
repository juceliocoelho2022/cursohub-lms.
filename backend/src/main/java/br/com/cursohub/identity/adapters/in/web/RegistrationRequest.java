package br.com.cursohub.identity.adapters.in.web;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record RegistrationRequest(
        @NotBlank @Size(min = 3, max = 150) String name,
        @NotBlank @Size(max = 254) String email,
        @NotBlank @Size(min = 12, max = 72) String password) {}
