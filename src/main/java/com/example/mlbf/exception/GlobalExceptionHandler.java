package com.example.mlbf.exception;

import com.example.mlbf.domain.ApiResponseDto;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;

public class GlobalExceptionHandler {

    /**
     * 비즈니스 로직 오류 처리 (400 Bad Request)
     * ex) 중복 아이디, 비밀번호 불일치, 존재하지 않는 데이터 등
     */
    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ApiResponseDto<Void>> handleBadRequest(IllegalArgumentException e) {
        return ResponseEntity.badRequest().body(ApiResponseDto.fail(e.getMessage()));
    }

    /*
     * 권한 부족 예외 (403 Forbidden)
     * 예: 남의 쪽지 읽기 시도, 관리자 페이지 접근 등
     */
        @ExceptionHandler(SecurityException.class)
        public ResponseEntity<ApiResponseDto<Void>> handleForbidden(SecurityException e) {
            return ResponseEntity.status(403).body(ApiResponseDto.fail(e.getMessage()));
        }

        /**
         * @Valid 유효성 검사 실패 처리 (400 Bad Request)
         * ex) 빈 제목, 짧은 비밀번호 등 DTO 검증 실패
         * getFieldErrors() 에서 첫 번째 오류 메시지만 반환
         */
        @ExceptionHandler(MethodArgumentNotValidException.class)
        public ResponseEntity<ApiResponseDto<Void>> handleValidation(MethodArgumentNotValidException e) {
            String message = e.getBindingResult().getFieldErrors().stream()
                    .map(fe -> fe.getField() + ": " + fe.getDefaultMessage())
                    .findFirst()
                    .orElse("입력값 오류");
            return ResponseEntity.badRequest().body(ApiResponseDto.fail(message));
        }

        /**
         * 서버 오류 처리 (500 Internal Server Error)
         * ex) 이메일 발송 실패, 예상치 못한 오류
         */
        @ExceptionHandler(RuntimeException.class)
        public ResponseEntity<ApiResponseDto<Void>> handleServerError(RuntimeException e) {
            return ResponseEntity.status(500).body(ApiResponseDto.fail(
                    e.getMessage() != null ? e.getMessage() : "서버 오류가 발생했습니다."));
        }
}
