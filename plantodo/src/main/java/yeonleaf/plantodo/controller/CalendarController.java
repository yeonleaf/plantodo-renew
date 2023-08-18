package yeonleaf.plantodo.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import yeonleaf.plantodo.dto.CalendarRangeReqDto;
import yeonleaf.plantodo.dto.CalendarResDto;
import yeonleaf.plantodo.dto.CheckboxResDto;
import yeonleaf.plantodo.dto.PlanResDto;
import yeonleaf.plantodo.exceptions.ApiBindingError;
import yeonleaf.plantodo.exceptions.ApiSimpleError;
import yeonleaf.plantodo.exceptions.QueryStringValidationException;
import yeonleaf.plantodo.service.*;
import yeonleaf.plantodo.util.DateRange;

import java.time.LocalDate;
import java.util.LinkedHashMap;
import java.util.List;

@Tag(name = "calendar", description = "날짜를 기준으로 조회하는 API")
@RestController
@RequestMapping("/calendar")
@RequiredArgsConstructor
public class CalendarController {

    private final DateRange dateRange;
    private final MemberService memberService;
    private final PlanService planService;
    private final CheckboxService checkboxService;

    @Operation(summary = "기간 캘린더 조회 API", description = "검색 시작일부터 종료일까지의 날짜를 기준으로 일정과 일정과 연관된 할일 (그룹 할일, 일일 할일)을 함께 조회하는 API")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "successful operation", content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = CalendarResDto.class))),
            @ApiResponse(responseCode = "400", description = "query string validation", content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = ApiBindingError.class))),
            @ApiResponse(responseCode = "401", description = "jwt token errors", content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = ApiSimpleError.class))),
            @ApiResponse(responseCode = "404", description = "resource not found", content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = ApiSimpleError.class))),
    })
    @GetMapping("/range")
    public ResponseEntity<?> getByRange(@RequestParam Long memberId, @RequestParam LocalDate searchStart,
                                        @RequestParam LocalDate searchEnd) {

        checkSearchDates(searchStart, searchEnd);

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

    private void checkSearchDates(LocalDate searchStart, LocalDate searchEnd) {

        QueryStringValidationException errors = new QueryStringValidationException();
        if (searchStart.isAfter(searchEnd)) {
            errors.rejectValue("searchStart", "searchStart는 searchEnd 이전일 수 없습니다.");
            errors.rejectValue("searchEnd", "searchEnd는 searchStart 이전일 수 없습니다.");
            throw errors;
        }

    }

    private void checkMemberExists(Long memberId) {
        memberService.findById(memberId);
    }

}
