package com.huifu.starchain.common.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ApiResponse<T> {

    private int code;
    private String message;
    private T data;
    private String traceId;

    @Builder.Default
    private long timestamp = Instant.now().toEpochMilli();

    // ---- Success ----
    public static <T> ApiResponse<T> ok(T data) {
        return ApiResponse.<T>builder()
                .code(200)
                .message("OK")
                .data(data)
                .build();
    }

    public static <T> ApiResponse<T> ok(String message, T data) {
        return ApiResponse.<T>builder()
                .code(200)
                .message(message)
                .data(data)
                .build();
    }

    public static ApiResponse<Void> ok() {
        return ApiResponse.<Void>builder()
                .code(200)
                .message("OK")
                .build();
    }

    // ---- Error ----
    public static <T> ApiResponse<T> error(int code, String message) {
        return ApiResponse.<T>builder()
                .code(code)
                .message(message)
                .build();
    }

    // ---- Paged ----
    public static <T> ApiResponse<PageResult<T>> page(PageResult<T> pageResult) {
        return ApiResponse.<PageResult<T>>builder()
                .code(200)
                .message("OK")
                .data(pageResult)
                .build();
    }
}
