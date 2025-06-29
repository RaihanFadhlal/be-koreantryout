package com.enigma.tekor.exception;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;

import com.enigma.tekor.dto.response.CommonResponse;
import com.enigma.tekor.dto.response.ErrorResponse;

public class GlobalExceptionHandler {
    @ExceptionHandler(BadRequestException.class)
    public ResponseEntity<ErrorResponse> handleBadRequestException(BadRequestException ex) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(new ErrorResponse(ex.getMessage()));
    }

    @ExceptionHandler(UsernameAlreadyExistsException.class)
    public ResponseEntity<CommonResponse<?>> handleUsernameAlreadyExistsException(UsernameAlreadyExistsException e) {
        CommonResponse<?> response = CommonResponse.builder()
                .status(HttpStatus.CONFLICT.getReasonPhrase())
                .message(e.getMessage())
                .build();
        return new ResponseEntity<>(response, HttpStatus.CONFLICT);
    }

    @ExceptionHandler(UserNotFoundException.class)
    public ResponseEntity<CommonResponse<?>> handleUserNotFoundException(UserNotFoundException e) {
        CommonResponse<?> response = CommonResponse.builder()
                .status(HttpStatus.NOT_FOUND.getReasonPhrase())
                .message(e.getMessage())
                .build();
        return new ResponseEntity<>(response, HttpStatus.NOT_FOUND);
    }
}
