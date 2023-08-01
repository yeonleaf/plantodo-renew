package yeonleaf.plantodo.util;

import yeonleaf.plantodo.domain.Plan;
import yeonleaf.plantodo.dto.PlanUpdateReqDto;

import java.time.LocalDate;
import java.time.Period;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Stream;

public class PlanDateRangeRevisionMaker {

    private HashMap<LocalDate, Integer> resultMap = new HashMap<>();
    public HashMap<LocalDate, Integer> revise(PlanUpdateReqDto newPlan, Plan oldPlan) {

        LocalDate newStart = newPlan.getStart();
        LocalDate newEnd = newPlan.getEnd();
        LocalDate oldStart = oldPlan.getStart();
        LocalDate oldEnd = oldPlan.getEnd();

        if (newStart.isEqual(oldStart) && newEnd.isEqual(oldEnd)) {
            return this.resultMap;
        }

        return makeRangeGap(newStart, newEnd, oldStart, oldEnd);

    }

    public boolean isInRange(LocalDate newStart, LocalDate newEnd, LocalDate oldStart, LocalDate oldEnd) {

        HashMap<LocalDate, Integer> rangeGap = makeRangeGap(newStart, newEnd, oldStart, oldEnd);
        System.out.println(rangeGap);
        return rangeGap.containsValue(5);

    }

    public HashMap<LocalDate, Integer> makeRangeGap(LocalDate newStart, LocalDate newEnd, LocalDate oldStart, LocalDate oldEnd) {

        initiateResultMap(oldStart, oldEnd, newStart, newEnd);

        mark(oldStart, oldEnd, 2);
        mark(newStart, newEnd, 3);

        return this.resultMap;

    }

    void mark(LocalDate start, LocalDate end, int ink) {

        int betweenDays = Period.between(start, end).getDays();
        for (int i = 0; i <= betweenDays; i++) {
            this.resultMap.put(start.plusDays(i), this.resultMap.get(start.plusDays(i)) + ink);
        }

    }
    void initiateResultMap(LocalDate oldStart, LocalDate oldEnd, LocalDate newStart, LocalDate newEnd) {

        HashSet<LocalDate> dates = new HashSet<>();
        dates.add(oldStart);
        dates.add(oldEnd);
        dates.add(newStart);
        dates.add(newEnd);

        LocalDate minDate = dates.stream().min(LocalDate::compareTo).orElseThrow(IllegalArgumentException::new);
        LocalDate maxDate = dates.stream().max(LocalDate::compareTo).orElseThrow(IllegalArgumentException::new);
        int betweenDates = Period.between(minDate, maxDate).getDays();
        for (int i = 0; i <= betweenDates; i++) {
            this.resultMap.putIfAbsent(minDate.plusDays(i), 0);
        }

    }
}
