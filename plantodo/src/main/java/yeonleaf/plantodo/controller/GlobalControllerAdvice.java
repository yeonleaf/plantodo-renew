package yeonleaf.plantodo.controller;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.jsonwebtoken.security.InvalidKeyException;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import yeonleaf.plantodo.exceptions.*;

@RestControllerAdvice
@RequiredArgsConstructor
public class GlobalControllerAdvice {

    private final ObjectMapper objectMapper;

    @ExceptionHandler(ArgumentValidationException.class)
    ResponseEntity<?> argumentValidationExceptionHandler(ArgumentValidationException ex) throws JsonProcessingException {
        ApiBindingError apiError = new ApiBindingError(ex.getMessage(), ex.getErrors());
        String responseData = objectMapper.writeValueAsString(apiError);
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(responseData);
    }

    @ExceptionHandler(DuplicatedMemberException.class)
    ResponseEntity<?> duplicatedMemberExceptionHandler(DuplicatedMemberException ex) throws JsonProcessingException {
        ApiSimpleError apiError = new ApiSimpleError("Duplicated Member", "Use other email");
        String responseData = objectMapper.writeValueAsString(apiError);
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(responseData);
    }

    @ExceptionHandler(ResourceNotFoundException.class)
    ResponseEntity<?> resourceNotFoundExceptionHandler(ResourceNotFoundException ex) throws JsonProcessingException {
        ApiSimpleError apiSimpleError = new ApiSimpleError("Resource not found", "Ensure that you previously joined in the service");
        String responseData = objectMapper.writeValueAsString(apiSimpleError);
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(responseData);
    }
}
