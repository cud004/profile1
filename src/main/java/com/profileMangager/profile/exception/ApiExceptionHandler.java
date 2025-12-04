package com.profileMangager.profile.exception;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.Instant;

/** Global handler: map các lỗi phổ biến sang mã HTTP chuẩn. */
@RestControllerAdvice
public class ApiExceptionHandler {
    private static final Logger log = LoggerFactory.getLogger(ApiExceptionHandler.class);

    // Trả 429 khi threadpool/queue từ chối tác vụ async (backpressure)
    @ExceptionHandler(java.util.concurrent.RejectedExecutionException.class)
    public ResponseEntity<ErrorResponse> handleRejected(java.util.concurrent.RejectedExecutionException ex) {
        log.warn("[429] Too busy: {}", ex.toString());
        return ResponseEntity.status(429).body(
                new ErrorResponse(429, "Server busy, please retry", Instant.now().toEpochMilli())
        );
    }

    // Ví dụ map 404 nếu bạn ném RuntimeException("Not found")
    @ExceptionHandler(RuntimeException.class)
    public ResponseEntity<ErrorResponse> handleRuntime(RuntimeException ex) {
        if ("Not found".equals(ex.getMessage())) {
            return ResponseEntity.status(404).body(
                    new ErrorResponse(404, "Not found", Instant.now().toEpochMilli())
            );
        }
        log.error("[500] {}", ex.toString(), ex);
        return ResponseEntity.status(500).body(
                new ErrorResponse(500, "Server error", Instant.now().toEpochMilli())
        );
    }

    // DTO trả về
    public static class ErrorResponse {
        public final int status;
        public final String message;
        public final long timestamp;
        public ErrorResponse(int status, String message, long timestamp) {
            this.status = status; this.message = message; this.timestamp = timestamp;
        }
    }
}
