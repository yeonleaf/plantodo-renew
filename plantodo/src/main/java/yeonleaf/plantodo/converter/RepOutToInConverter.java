package yeonleaf.plantodo.converter;

import org.springframework.core.convert.converter.Converter;
import yeonleaf.plantodo.dto.RepInputDto;
import yeonleaf.plantodo.domain.Repetition;

import java.util.ArrayList;
import java.util.List;

public class RepOutToInConverter implements Converter<Repetition, RepInputDto> {

    private static final String[] koreanDates = {"월", "화", "수", "목", "금", "토", "일"};

    @Override
    public RepInputDto convert(Repetition source) {

        Long repOption = source.getRepOption();
        String repValue = source.getRepValue();

        RepInputDto repInputDto = new RepInputDto();
        repInputDto.setRepOption(repOption);

        if (repOption.equals(1L)) {
            return repInputDto;
        } else if (repOption.equals(2L)) {
            repInputDto.getRepValue().add(repValue);
            return repInputDto;
        } else {
            repInputDto.setRepValue(parseToList(repValue));
            return repInputDto;
        }

    }

    private List<String> parseToList(String repValue) {
        List<String> repList = new ArrayList<>();
        for (int i = 0; i < 7; i++) {
            if (repValue.charAt(i) == '1') {
                repList.add(koreanDates[i]);
            }
        }
        return repList;
    }
}
