package yeonleaf.plantodo.util;

import yeonleaf.plantodo.domain.Plan;
import yeonleaf.plantodo.dto.RepInputDto;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.Period;
import java.util.ArrayList;
import java.util.List;

public class CheckboxDateCreator {
    public static List<LocalDate> create(Plan plan, RepInputDto repInputDto) {

        List<LocalDate> res = new ArrayList<>();
        LocalDate start = plan.getStart();
        LocalDate end = plan.getEnd();
        int repOption = repInputDto.getRepOption();
        List<String> repValue = repInputDto.getRepValue();

        int betweenDays = Period.between(start, end).getDays();

        for (int i = 0; i < betweenDays + 1; i++) {
            LocalDate now = start.plusDays(i);
            if (canMakeCheckboxNow(start, now, repOption, repValue)) {
                res.add(now);
            }
        }
        return res;

    }

    static boolean canMakeCheckboxNow(LocalDate start, LocalDate now, int repOption, List<String> repValue) {

        List<DayOfWeek> dayOfWeeks = repValue.stream().map(CheckboxDateCreator::parseKoreanDateToDayOfWeek).toList();

        if (repOption == 3) {
            return dayOfWeeks.contains(now.getDayOfWeek());
        } else if (repOption == 2) {
            if (repValue.isEmpty()) {
                throw new IllegalArgumentException();
            }
            int interval = Integer.parseInt(repValue.get(0));
            int diffDays = Period.between(start, now).getDays();
            if ((diffDays % interval) == 0) {
                return true;
            }
            return false;
        }
        return true;

    }

    static DayOfWeek parseKoreanDateToDayOfWeek(String koreanDate) {
        return switch (koreanDate) {
            case "월" -> DayOfWeek.MONDAY;
            case "화" -> DayOfWeek.TUESDAY;
            case "수" -> DayOfWeek.WEDNESDAY;
            case "목" -> DayOfWeek.THURSDAY;
            case "금" -> DayOfWeek.FRIDAY;
            case "토" -> DayOfWeek.SATURDAY;
            default -> DayOfWeek.SUNDAY;
        };
    }
}
