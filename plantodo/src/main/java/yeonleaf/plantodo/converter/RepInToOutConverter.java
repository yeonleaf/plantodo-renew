package yeonleaf.plantodo.converter;

import org.springframework.core.convert.converter.Converter;
import yeonleaf.plantodo.dto.RepInputDto;
import yeonleaf.plantodo.domain.Repetition;

import java.util.List;

public class RepInToOutConverter implements Converter<RepInputDto, Repetition> {

    private static final String[] koreanDates = {"월", "화", "수", "목", "금", "토", "일"};

    @Override
    public Repetition convert(RepInputDto source) {

        Repetition repetition = new Repetition();

        int repOption = source.getRepOption();
        List<String> repValue = source.getRepValue();

        repetition.setRepOption(repOption);
        repetition.setRepValue(makeRepValue(repOption, repValue));
        return repetition;

    }

    private String makeRepValue(int repOption, List<String> repList) {

        if (repOption == 1) {
            return "-1";
        } else if (repOption == 2) {
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
