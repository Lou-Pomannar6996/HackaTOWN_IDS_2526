package it.ids.hackathown.api.dto.response;

public record TeamResponse(
    Integer id,
    String name,
    Integer maxSize
) {
}
