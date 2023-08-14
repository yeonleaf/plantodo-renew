package yeonleaf.plantodo.unit.transform;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import yeonleaf.plantodo.converter.RepInToOutConverter;
import yeonleaf.plantodo.dto.RepInputDto;
import yeonleaf.plantodo.domain.Repetition;

import java.util.Arrays;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * target : {@link RepInToOutConverter#convert(RepInputDto)}의 리턴값인 {@link Repetition}의 repValue
 * target description : {@link RepInputDto}를 {@link Repetition}로 변환하는 Converter
 * test description : RepInputDto.repValue(List<String>)가 repOption 조건에 따라 Repetition.repValue(String) 형태로 바뀌는지 검증한다.
 *
 *                    변환 로직은 다음과 같다.
 *                    (repOption == 1) 무조건 "-1"
 *                                     (ex) [] -> "-1"
 *                    (repOption == 2) repValue.get(0)을 그대로 리턴 (RepInputValidator로 인해 size=1을 반드시 보장함)
 *                                     (ex) ["2"] -> "2"
 *                    (repOption == 3) "0" 혹은 "1"로 이루어진 공백 없는 7자리의 문자열.
 *                                     월~일까지의 요일중 RepInputDto.repValue에 있는 요일을 1, 없는 요일을 0으로 체크한다.
 *                                     (ex) ["월", "수", "금"] -> "1010100"
 *
 *                    ※ RepInputValidator를 통해 이미 타입이나 형식이 검증된 데이터라고 가정한다.
 */
public class RepInToOutConverterTest {

    private final RepInToOutConverter repInToOutConverter = new RepInToOutConverter();

    /**
     * 보조 메소드
     * List<String> 형태인 repValue를 파라미터로 편리하게 입력
     */
    private List<String> makeArrToString(String... target) {
        return Arrays.asList(target);
    }

    /**
     * 테스트 메이커
     * @param repOption (int) 할 일이 반복되는 양상을 설정하는 옵션
     *                  1(매일 반복), 2(기간 반복), 3(요일 반복)
     * @param expectedRepValue (String) 변환된 repValue 결과
     * @param repValue (List<String>) 할 일이 반복되는 주기를 설정하는 옵션
     */
    private void assertInputConversionResult(int repOption, String expectedRepValue, String... repValue) {

        // given
        RepInputDto input = new RepInputDto(repOption, makeArrToString(repValue));

        // when
        Repetition output = repInToOutConverter.convert(input);

        // then
        assertThat(output.getRepValue()).isEqualTo(expectedRepValue);

    }

    @Test
    @DisplayName("validate 통과한 이후 convert 결과 검증")
    void convertInputTestNormal() {

        // repOption이 1인 경우 "-1"을 리턴한다.
        assertInputConversionResult(1, "-1");

        // repOption이 2인 경우 repValue.get(0)을 리턴한다.
        assertInputConversionResult(2, "2", "2");

        // repOption이 3인 경우 repValue에 있는 요일을 "0" 혹은 "1"로 이루어진 공백 없는 7자리의 문자열 형태로 바꿔서 리턴한다.
        assertInputConversionResult(3, "1010001", "수", "월", "일");

    }

}
