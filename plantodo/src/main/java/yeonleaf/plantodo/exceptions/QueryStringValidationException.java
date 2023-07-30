package yeonleaf.plantodo.exceptions;

import lombok.Getter;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Getter
public class QueryStringValidationException extends RuntimeException {

    private Map<String, List<String>> errors;

    public void rejectValue(String field, String detail) {

        errors.computeIfAbsent(field, k -> new ArrayList<>());
        errors.get(field).add(detail);

    }

    public QueryStringValidationException() {
        this.errors = new HashMap<>();
    }

}
