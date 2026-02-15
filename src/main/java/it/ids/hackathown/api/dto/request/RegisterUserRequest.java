package it.ids.hackathown.api.dto.request;

public record RegisterUserRequest(
    String email,
    String password,
    String nome,
    String cognome
) {
}
