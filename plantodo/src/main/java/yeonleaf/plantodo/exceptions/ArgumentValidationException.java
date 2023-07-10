package yeonleaf.plantodo.exceptions;

import lombok.Getter;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Getter
public class ArgumentValidationException extends RuntimeException {
    private String message;
    private Map<String, List<String>> errors = new HashMap<>();

    public ArgumentValidationException(String message, BindingResult bindingResult) {
        super();
        this.message = message;
        bindingResult.getAllErrors().forEach(
                e -> {
                    String field = ((FieldError) e).getField();
                    String defaultMessage = e.getDefaultMessage();
                    errors.computeIfAbsent(field, k -> new ArrayList<>());
                    errors.get(field).add(defaultMessage);
                }
        );
    }

    public ArgumentValidationException(String field, String detail) {
        this.message = "유효하지 않은 입력값";
        errors.computeIfAbsent(field, k -> new ArrayList<>());
        errors.get(field).add(detail);
    }
}
