package com.catapi.exceptionhandler;

import jakarta.persistence.EntityNotFoundException;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class ApiErrorHandler {

    @ExceptionHandler(value = {EntityNotFoundException.class})
    public ResponseEntity<Void> handleEntityNotFound(EntityNotFoundException ex){
        return ResponseEntity.notFound().build();
    }
}