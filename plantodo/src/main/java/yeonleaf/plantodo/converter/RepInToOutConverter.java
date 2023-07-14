package yeonleaf.plantodo.converter;

import org.springframework.core.convert.converter.Converter;
import yeonleaf.plantodo.dto.RepInputDto;
import yeonleaf.plantodo.domain.Repetition;
import yeonleaf.plantodo.exceptions.RepConversionException;
import yeonleaf.plantodo.validator.RepInputValidator;

import java.util.List;

public class RepInToOutConverter implements Converter<RepInputDto, Repetition> {

    private static final String[] koreanDates = {"월", "화", "수", "목", "금", "토", "일"};

    @Override
    public Repetition convert(RepInputDto source) {

        Repetition repetition = new Repetition();

        Long repOption = source.getRepOption();
        List<String> repValue = source.getRepValue();

        repetition.setRepOption(repOption);
        repetition.setRepValue(makeRepValue(repOption, repValue));
        return repetition;

    }

    private String makeRepValue(Long repOption, List<String> repList) {

        if (repOption.equals(1L)) {
            return "-1";
        } else if (repOption.equals(2L)) {
            return repList.get(0);
        } else {
            StringBuilder repStr = new StringBuilder("0000000");
            for (int i = 0; i < 7; i++) {
                if (repList.contains(koreanDates[i])) {
                    repStr.setCharAt(i, '1');
                }
            }
            return repStr.toString();
        }

    }

}
