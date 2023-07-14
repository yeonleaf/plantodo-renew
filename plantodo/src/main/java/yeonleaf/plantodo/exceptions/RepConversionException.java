package yeonleaf.plantodo.exceptions;

import org.springframework.core.convert.ConversionException;

public class RepConversionException extends ConversionException {
    public RepConversionException(String message) {
        super(message);
    }
}
