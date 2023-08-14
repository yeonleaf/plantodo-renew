package yeonleaf.plantodo.unit.validation;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.validation.BindingResult;
import org.springframework.validation.Errors;
import yeonleaf.plantodo.dto.RepInputDto;
import yeonleaf.plantodo.exceptions.ArgumentValidationException;
import yeonleaf.plantodo.validator.RepInputValidator;

import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.spy;

/**
 * target : {@link RepInputValidator#validate(Object, Errors)}
 * target description : {@link RepInputDto}의 repOption 필드와 repValue 필드를 검증하는 Validator
 *
 * test description : repOption, repValue를 입력받아 repOption에 따라 repValue가 올바르게 결정되는지 검증합니다.
 *                 RepInputValidator#validate 함수는 어떤 경우에도 값을 리턴하지 않습니다.
 *                 단, repOption 값이 잘못되었거나 repValue값이 올바르게 결정되지 않은 경우에만 bindingError#rejectValue를 호출합니다.
 *
 *                 테스트에서 검증하는 것
 *                 1. {@link ArgumentValidationException}의 발생 여부
 *                 bindingError#rejectValue의 호출 여부를 Mockito.spy가 감지하도록 하고
 *                 감지되는 경우 ArgumentValidationException을 임의로 발생시킵니다.
 *
 *                 2. 아무것도 발생하지 않는 상태
 *                 올바른 값을 넣어 검증을 통과한다면 아무 일도 일어나지 않습니다.
 */
public class RepInputValidatorTest {

    private RepInputValidator validator = new RepInputValidator();
    private BindingResult bindingResult = spy(BindingResult.class);

    /**
     * 보조 메소드
     * List<String> 형태인 repValue를 파라미터로 편리하게 입력
     */
    private List<String> makeArrToString(String... target) {
        return Arrays.asList(target);
    }

    /**
     * 테스트 메이커
     * 입력값에 따라 같은 형식의 서로 다른 테스트를 생성한다.
     *
     * @param repOption (int) 할 일이 반복되는 양상을 설정하는 옵션
     *                  1(매일 반복), 2(기간 반복), 3(요일 반복)
     * @param reverse (int)
     *                0(잘못된 값을 넣어서 검증을 통과하지 못해야 성공하는 테스트임을 의미하는 변수)
     *                1(올바른 값을 넣어서 검증을 통과해야 성공하는 테스트)
     * @param repValueCandidates repValue(할 일이 반복되는 주기를 설정하는 옵션)에 넣을 요소. 개수 제한 없음
     * @throws ArgumentValidationException 잘못된 값을 입력해서 검증을 통과하지 못한 경우
     */
    private void assertValidation(int repOption, int reverse, String... repValueCandidates) {
        RepInputDto input = new RepInputDto(repOption, makeArrToString(repValueCandidates));
        if (reverse == 0) {
            doThrow(ArgumentValidationException.class).when(bindingResult).rejectValue(any(), any(), any());
            assertThrows(ArgumentValidationException.class, () -> validator.validate(input, bindingResult));
        } else {
            assertDoesNotThrow(() -> validator.validate(input, bindingResult));
        }
    }

    /**
     * repValue size 조건
     * (repOption == 1) repValue.size == 0
     * (repOption == 2) repValue.size == 1
     * (repOption == 3) 1 <= repValue.size && repValue.size <= 7
     */
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
