package com.penta.template.common.handler;

import com.penta.template.common.exception.DuplicateUserIdException;
import com.penta.template.common.response.ApiErrorResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;


@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    // 순수 API 예외처리만 여기에 작성...
    // 시큐리티는 X

    @ExceptionHandler(DuplicateUserIdException.class)
    public ResponseEntity<ApiErrorResponse<String>> duplicateUserIdExceptionHandler(DuplicateUserIdException ex) {
        log.info("duplicateUserIdExceptionHandler = {}", ex.getMessage());
        ApiErrorResponse<String> apiErrorResponse = new ApiErrorResponse<>(ex.getMessage());
        return new ResponseEntity<>(apiErrorResponse, HttpStatus.BAD_REQUEST);
    }


    @ExceptionHandler(RuntimeException.class)
    public ResponseEntity<ApiErrorResponse<String>> runTimeExceptionHandler(RuntimeException ex) {
        log.info("runTimeExceptionHandler = {}", ex.getMessage());
        ApiErrorResponse<String> apiErrorResponse = new ApiErrorResponse<>(ex.getMessage());
        return new ResponseEntity<>(apiErrorResponse, HttpStatus.BAD_REQUEST);
    }


    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiErrorResponse<String>> exceptionHandler(Exception ex) {
        log.info("exceptionHandler = {}", ex.getMessage());
        ApiErrorResponse<String> apiErrorResponse = new ApiErrorResponse<>(ex.getMessage());
        return new ResponseEntity<>(apiErrorResponse, HttpStatus.INTERNAL_SERVER_ERROR);
    }


}
