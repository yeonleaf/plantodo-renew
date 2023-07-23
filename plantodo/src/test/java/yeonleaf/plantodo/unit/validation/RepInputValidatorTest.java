package yeonleaf.plantodo.unit.validation;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.validation.BindingResult;
import yeonleaf.plantodo.dto.RepInputDto;
import yeonleaf.plantodo.exceptions.RepConversionException;
import yeonleaf.plantodo.validator.RepInputValidator;

import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.spy;

public class RepInputValidatorTest {

    private RepInputValidator validator = new RepInputValidator();
    private BindingResult bindingResult = spy(BindingResult.class);

    private List<String> makeArrToString(String... target) {
        return Arrays.asList(target);
    }

    private void assertValidation(int repOption, int reverse, String... target) {
        RepInputDto input = new RepInputDto(repOption, makeArrToString(target));
        doThrow(RepConversionException.class).when(bindingResult).rejectValue(any(), any(), any());
        if (reverse == 0) {
            assertThrows(RepConversionException.class, () -> validator.validate(input, bindingResult));
        } else {
            assertDoesNotThrow(() -> validator.validate(input, bindingResult));
        }
    }

    @Test
    @DisplayName("다른 조건을 모두 만족하고 repValue size 조건을 만족하지 못함")
    void validateInputTestAbnormalRepValueSize() {
        assertValidation(1, 0, "월");
        assertValidation(2, 0);
        assertValidation(2, 0, "-2", "3");
        assertValidation(3, 0);
        assertValidation(3, 0, "월", "화", "수", "목", "금", "토", "월", "일");
    }

    @Test
    @DisplayName("다른 조건을 모두 만족하고 repOption이 2일 때 양의 정수만 들어가야 한다는 조건을 만족하지 못함")
    void validateInputTestAbnormalRep2NeedsNumber() {

        assertValidation(2, 0, "한2");
        assertValidation(2, 0, "%5");
        assertValidation(2, 0, "3.05");
        assertValidation(2, 0, "3L");
        assertValidation(2, 0, "3e6F");
        assertValidation(2, 0, "3e6");
        assertValidation(2, 0, "-3");
        assertValidation(2, 0, "0");
        assertValidation(2, 1, "+3");

    }

    @Test
    @DisplayName("다른 조건을 모두 만족하고 repOption이 3일 때 월, 화, 수, 목, 금, 토, 일 외의 다른 입력값이 있으면 안 된다는 조건을 만족하지 못함")
    void validateInputTestAbnormalRep3NeedsDates() {
        assertValidation(3, 0, "사과", "월", "수");
    }

    @Test
    @DisplayName("다른 조건을 모두 만족하고 repOption이 3일 때 중복된 요일 값이 있으면 안 된다는 조건을 만족하지 못함")
    void validateInputTestAbnormalRep3NeedsUnique() {
        assertValidation(3, 0, "월", "월", "수", "토");
        assertValidation(3, 1, "월", "수", "토");
    }

}
