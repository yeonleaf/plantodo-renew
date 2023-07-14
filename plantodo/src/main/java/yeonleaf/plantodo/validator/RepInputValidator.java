package yeonleaf.plantodo.validator;

import org.springframework.validation.Errors;
import org.springframework.validation.Validator;
import yeonleaf.plantodo.dto.RepInputDto;
import yeonleaf.plantodo.exceptions.RepConversionException;

import java.util.Arrays;
import java.util.List;

public class RepInputValidator implements Validator {

    private static final List<String> koreanDates = Arrays.asList("월", "화", "수", "목", "금", "토", "일");

    @Override
    public boolean supports(Class<?> clazz) {
        return false;
    }

    @Override
    public void validate(Object target, Errors errors) {

        RepInputDto repInputDto = (RepInputDto) target;
        Long repOption = repInputDto.getRepOption();
        List<String> repValue = repInputDto.getRepValue();

        if (!repValueHasSuitableSize(repOption, repValue)) {
            errors.rejectValue("repValue", "size", makeSizeMessage(repOption));
        }

        if (repOption.equals(2L) && onlyPositiveInt(repValue) == -1) {
            errors.rejectValue("repValue", "range", "repOption이 기간(2L)일 때 0보다 큰 정수형 문자열 한 개가 repValue에 들어와야 합니다.");
        }

        if (repOption.equals(3L) && !onlyDates(repValue)) {
            errors.rejectValue("repValue", "range", "repOption이 요일(3L)일 때 repValue에 요일(월, 화, 수, 목, 금, 토, 일) 형태 외에 다른 값이 들어갈 수 없습니다.");
        }

        if (repOption.equals(3L) && !onlyUnique(repValue)) {
            errors.rejectValue("repValue", "range", "repOption이 요일(3L)일 때 repValue에 중복된 요일 값이 들어갈 수 없습니다.");
        }

    }

    private String makeSizeMessage(Long repOption) {
        if (repOption.equals(1L)) {
            return "repOption이 매일(1L)인 경우 repValue에 값을 입력할 수 없습니다.";
        } else if (repOption.equals(2L)) {
            return "repOption이 기간(2L)인 경우 repValue에는 한 개의 0보다 큰 정수형 값이 들어와야 합니다.";
        } else {
            return "repOption이 요일(3L)인 경우 repValue에는 1개 이상 7게 이하의 중복되지 않은 요일값이 들어와야 합니다.";
        }
    }

    private static boolean repValueHasSuitableSize(Long repOption, List<String> repValue) {

        if (repOption.equals(1L) && repValue.size() > 0) {
            return false;
        } else if (repOption.equals(2L) && repValue.isEmpty()) {
            return false;
        } else if (repOption.equals(2L) && repValue.size() > 1) {
            return false;
        } else if (repOption.equals(3L) && repValue.isEmpty()) {
            return false;
        } else return !repOption.equals(3L) || repValue.size() <= 7;

    }

    private static int onlyPositiveInt(List<String> repValue) {

        String target = repValue.get(0);
        int parsedTarget;

        try {
            parsedTarget = Integer.parseInt(target);
        } catch (NumberFormatException ex) {
            return -1;
        }

        if (parsedTarget >= 1) {
            return parsedTarget;
        } else {
            return -1;
        }

    }

    private static boolean onlyDates(List<String> repValue) {

        for (String s : repValue) {
            if (!koreanDates.contains(s)) {
                return false;
            }
        }
        return true;

    }

    private static boolean onlyUnique(List<String> repValue) {
        List<String> uniqueRepVal = repValue.stream().distinct().toList();
        return repValue.size() == uniqueRepVal.size();
    }

}
