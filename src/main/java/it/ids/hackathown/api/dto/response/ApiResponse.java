package it.ids.hackathown.api.dto.response;

public record ApiResponse<T>(String message, T data) {

    public static <T> ApiResponse<T> ok(T data) {
        return new ApiResponse<>("OK", data);
    }
}
