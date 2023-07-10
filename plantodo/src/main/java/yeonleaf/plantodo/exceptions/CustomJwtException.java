package yeonleaf.plantodo.exceptions;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class CustomJwtException extends RuntimeException {
    private String message;
}
