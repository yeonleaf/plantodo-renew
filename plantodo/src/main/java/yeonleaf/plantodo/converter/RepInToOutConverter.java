package yeonleaf.plantodo.converter;

import org.springframework.core.convert.converter.Converter;
import yeonleaf.plantodo.dto.RepInputDto;
import yeonleaf.plantodo.domain.Repetition;

import java.util.List;

/**
 * target description : {@link RepInputDto}를 {@link Repetition}로 변환하는 Converter
 */
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

    /**
     * 리스트 형태의 반복값을 String 형태로 변환하는 메소드
     * @param repOption 반복 옵션 (1 : 매일, 2 : 기간, 3 : 요일)
     * @param repList 반복값이 들어 있는 리스트
     *                ex) repOption=2 2
     *                    repOption=3 월, 수, 금
     * @return 반복 옵션에 따라 형식에 맞는 String 문자열을 리턴
     *         repOption=1 "-1"
     *         repOption=2 리스트에 들어 있던 문자열을 그대로 String으로 변환 후 리턴
     *         repOption=3 "0000000" 형태의 0과 1로 구성된 비트 문자열을 리턴
     *                     각각의 비트는 월 ~ 일 사이의 요일을 의미함
     */
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
