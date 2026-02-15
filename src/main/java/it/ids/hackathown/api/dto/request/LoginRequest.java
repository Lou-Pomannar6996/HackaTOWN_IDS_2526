package it.ids.hackathown.api.dto.request;

public record LoginRequest(
    String email,
    String password
) {
}
