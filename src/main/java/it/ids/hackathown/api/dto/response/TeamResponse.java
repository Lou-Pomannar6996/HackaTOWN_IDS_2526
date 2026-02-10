package it.ids.hackathown.api.dto.response;

public record TeamResponse(
    Long id,
    String name,
    Integer maxSize
) {
}
