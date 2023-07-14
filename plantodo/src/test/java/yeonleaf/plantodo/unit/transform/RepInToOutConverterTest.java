package yeonleaf.plantodo.unit.transform;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import yeonleaf.plantodo.converter.RepInToOutConverter;
import yeonleaf.plantodo.dto.RepInputDto;
import yeonleaf.plantodo.domain.Repetition;

import java.util.Arrays;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

public class RepInToOutConverterTest {

    /**
     * repOption 1, 2, 3 아니면 throw Exception
     * repOption 1 -> repValue must empty -> return "-1"
     * repOption 2 -> repValue size must 1 -> return repValue.get(0)
     * repOption 3 -> repValue size 1 <= must <= 7
     */

    private final RepInToOutConverter repInToOutConverter = new RepInToOutConverter();

    private List<String> makeArrToString(String... target) {
        return Arrays.asList(target);
    }

    private void assertInputConversionResult(Long repOption, String expectedRepValue, String... target) {

        RepInputDto input = new RepInputDto(repOption, makeArrToString(target));
        Repetition output = repInToOutConverter.convert(input);
        assertThat(output.getRepValue()).isEqualTo(expectedRepValue);

    }

    @Test
    @DisplayName("validate 통과한 이후 convert 결과 검증")
    void convertInputTestNormal() {

        assertInputConversionResult(1L, "-1");
        assertInputConversionResult(2L, "2", "2");
        assertInputConversionResult(3L, "1010001", "수", "월", "일");

    }

}
