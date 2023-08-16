package yeonleaf.plantodo.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import yeonleaf.plantodo.dto.CalendarRangeReqDto;
import yeonleaf.plantodo.dto.CheckboxResDto;
import yeonleaf.plantodo.dto.PlanResDto;
import yeonleaf.plantodo.exceptions.ArgumentValidationException;
import yeonleaf.plantodo.service.*;
import yeonleaf.plantodo.util.DateRange;

import java.time.LocalDate;
import java.util.LinkedHashMap;
import java.util.List;

@RestController
@RequestMapping("/calendar")
@RequiredArgsConstructor
public class CalendarController {

    private final DateRange dateRange;
    private final MemberService memberService;
    private final PlanService planService;
    private final CheckboxService checkboxService;

    @GetMapping("/range")
    public ResponseEntity<?> getByRange(@Valid @RequestBody CalendarRangeReqDto calendarRangeReqDto, BindingResult bindingResult) {

        if (bindingResult.hasErrors()) {
            throw new ArgumentValidationException("입력값 타입/내용 오류", bindingResult);
        }

        LocalDate searchStart = calendarRangeReqDto.getSearchStart();
        LocalDate searchEnd = calendarRangeReqDto.getSearchEnd();

        validateSearchDates(searchStart, searchEnd, bindingResult);

        if (bindingResult.hasErrors()) {
            throw new ArgumentValidationException("입력값 형식 오류", bindingResult);
        }

        Long memberId = calendarRangeReqDto.getMemberId();
        checkMemberExists(memberId);

        LinkedHashMap<LocalDate, LinkedHashMap<PlanResDto, List<CheckboxResDto>>> result = makeCalendar(memberId, searchStart, searchEnd);

        return ResponseEntity.status(HttpStatus.OK).body(result);
    }

    private LinkedHashMap<LocalDate, LinkedHashMap<PlanResDto, List<CheckboxResDto>>> makeCalendar(Long memberId, LocalDate searchStart, LocalDate searchEnd) {

        LinkedHashMap<LocalDate, LinkedHashMap<PlanResDto, List<CheckboxResDto>>> result = new LinkedHashMap<>();

        dateRange.between(searchStart, searchEnd).forEach(date -> {
            LinkedHashMap<PlanResDto, List<CheckboxResDto>> tmp = new LinkedHashMap<>();

            List<PlanResDto> plans = planService.all(memberId, date);
            plans.forEach(plan -> {
                tmp.put(plan, checkboxService.allByPlan(plan.getId(), date));
            });

            result.put(date, tmp);
        });

        return result;

    }

    private void validateSearchDates(LocalDate searchStart, LocalDate searchEnd, BindingResult bindingResult) {
        if (searchStart.isAfter(searchEnd) || searchEnd.isBefore(searchStart)) {
            bindingResult.rejectValue("searchStart", "시작일은 종료일 이전이어야 합니다.");
            bindingResult.rejectValue("searchEnd", "종료일은 시작일 이후여야 합니다.");
        }
    }

    private void checkMemberExists(Long memberId) {
        memberService.findById(memberId);
    }

}
