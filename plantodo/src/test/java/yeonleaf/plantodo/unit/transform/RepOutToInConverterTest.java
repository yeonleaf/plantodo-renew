package yeonleaf.plantodo.unit.transform;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import yeonleaf.plantodo.converter.RepOutToInConverter;
import yeonleaf.plantodo.dto.RepInputDto;
import yeonleaf.plantodo.domain.Repetition;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * target : {@link RepOutToInConverter#convert(Repetition)}의 리턴값인 {@link RepInputDto}의 repValue
 * target description : {@link Repetition}을 {@link RepInputDto}로 변환하는 Converter
 *
 * test description : Repetition.repValue(String)이 repOption 조건에 따라 RepInputDto.repValue(List<String>) 형태로 바뀌는지 검증한다.
 *
 *                    변환 로직은 다음과 같다.
 *                    (repOption == 1) 빈 리스트를 리턴한다.
 *                                     (ex) "-1" -> []
 *                    (repOption == 2) RepInputDto.repValue 값을 배열에 담아 리턴한다.
 *                    (repOption == 3) ["월", "화Repetition.repValue의 비트값이 1인 요일이 담긴 리스트를 리턴한다.
 *                                     (ex) "0101010" -> ["화", "목", "토"]
 *
 *  @param repOption (int) 할 일이 반복되는 양상을 설정하는 옵션
 *                   1(매일 반복), 2(기간 반복), 3(요일 반복)
 *  @param repValue (List<String> -> String) 할 일이 반복되는 주기를 설정하는 옵션
 */
public class RepOutToInConverterTest {

    private final RepOutToInConverter repOutToInConverter = new RepOutToInConverter();

    @Test
    @DisplayName("repOption이 1인 경우 빈 리스트를 리턴한다.")
    void convertRepOption1() {

        // given
        Repetition output = new Repetition(1, "-1");

        // when
        RepInputDto input = repOutToInConverter.convert(output);

        // then
        assertThat(input.getRepValue()).isEmpty();

    }

    @Test
    @DisplayName("repOption이 2인 경우 RepInputDto.repValue 값을 배열에 담아 리턴한다.")
    void convertRepOption2() {

        // given
        Repetition output = new Repetition(2, "3");

        // when
        RepInputDto input = repOutToInConverter.convert(output);

        // then
        assertThat(input.getRepValue().size()).isEqualTo(1);
        assertThat(input.getRepValue().get(0)).isEqualTo("3");

    }

    @Test
    @DisplayName("repOption이 3인 경우 요일이 담긴 리스트를 반환한다.")
    void convertRepOption3() {

        Repetition output1 = new Repetition(3, "0110000");
        RepInputDto input1 = repOutToInConverter.convert(output1);
        assertThat(input1.getRepValue().size()).isEqualTo(2);
        assertThat(input1.getRepValue().contains("화")).isTrue();
        assertThat(input1.getRepValue().contains("수")).isTrue();

        Repetition output2 = new Repetition(3, "0000000");
        RepInputDto input2 = repOutToInConverter.convert(output2);
        assertThat(input2.getRepValue()).isEmpty();

        Repetition output3 = new Repetition(3, "1111111");
        RepInputDto input3 = repOutToInConverter.convert(output3);
        assertThat(input3.getRepValue().size()).isEqualTo(7);

    }

}
