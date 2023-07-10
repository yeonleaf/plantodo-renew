package yeonleaf.plantodo.exceptions;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class ApiSimpleError {
    private String message;
    private String detail;

    public ApiSimpleError(String message, String detail) {
        this.message = message;
        this.detail = detail;
    }
}
