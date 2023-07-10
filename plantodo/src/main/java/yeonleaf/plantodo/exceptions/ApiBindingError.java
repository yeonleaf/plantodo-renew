package yeonleaf.plantodo.exceptions;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import java.util.List;
import java.util.Map;

@Getter
@Setter
@NoArgsConstructor
public class ApiBindingError {
    private String message;
    private Map<String, List<String>> errors;

    public ApiBindingError(String message, Map<String, List<String>> errors) {
        this.message = message;
        this.errors = errors;
    }
}
