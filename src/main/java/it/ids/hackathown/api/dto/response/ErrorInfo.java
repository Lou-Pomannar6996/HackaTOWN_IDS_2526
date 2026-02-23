package it.ids.hackathown.api.dto.response;

public record ErrorInfo(
    int status,
    String error,
    String detail,
    String path,
    Object errors
) {
}
