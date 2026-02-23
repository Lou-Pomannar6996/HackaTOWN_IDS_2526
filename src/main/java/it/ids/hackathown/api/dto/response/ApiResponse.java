package it.ids.hackathown.api.dto.response;

public record ApiResponse(String message, ErrorInfo error) {

    public static ApiResponse ok() {
        return new ApiResponse("OK", null);
    }
}
