package yeonleaf.plantodo.unit.transform;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import yeonleaf.plantodo.converter.RepOutToInConverter;
import yeonleaf.plantodo.dto.RepInputDto;
import yeonleaf.plantodo.domain.Repetition;

import static org.assertj.core.api.Assertions.assertThat;

public class RepOutToInConverterTest {

    private final RepOutToInConverter repOutToInConverter = new RepOutToInConverter();

    /**
     * validation 생략 (RepOutputDto에 들어 있는 값의 형식은 정상이라고 가정)
     */
    @Test
    @DisplayName("convert repOption 1")
    void convertRepOption1() {

        Repetition output = new Repetition(1L, "-1");
        RepInputDto input = repOutToInConverter.convert(output);
        assertThat(input.getRepValue()).isEmpty();

    }

    @Test
    @DisplayName("convert repOption 2")
    void convertRepOption2() {

        Repetition output = new Repetition(2L, "3");
        RepInputDto input = repOutToInConverter.convert(output);
        assertThat(input.getRepValue().size()).isEqualTo(1);
        assertThat(input.getRepValue().get(0)).isEqualTo("3");

    }

    @Test
    @DisplayName("convert repOption 3")
    void convertRepOption3() {

        Repetition output1 = new Repetition(3L, "0110000");
        RepInputDto input1 = repOutToInConverter.convert(output1);
        assertThat(input1.getRepValue().size()).isEqualTo(2);
        assertThat(input1.getRepValue().contains("화")).isTrue();
        assertThat(input1.getRepValue().contains("수")).isTrue();

        Repetition output2 = new Repetition(3L, "0000000");
        RepInputDto input2 = repOutToInConverter.convert(output2);
        assertThat(input2.getRepValue()).isEmpty();

        Repetition output3 = new Repetition(3L, "1111111");
        RepInputDto input3 = repOutToInConverter.convert(output3);
        assertThat(input3.getRepValue().size()).isEqualTo(7);

    }

}
