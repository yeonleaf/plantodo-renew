package yeonleaf.plantodo.exceptions;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class DuplicatedMemberException extends RuntimeException {

    private String message;

    public DuplicatedMemberException(String message) {
        super();
        this.message = message;
    }

}
