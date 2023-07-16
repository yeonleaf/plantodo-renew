package yeonleaf.plantodo.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import yeonleaf.plantodo.domain.Member;
import yeonleaf.plantodo.dto.PlanReqDto;
import yeonleaf.plantodo.dto.PlanResDto;
import yeonleaf.plantodo.exceptions.ArgumentValidationException;
import yeonleaf.plantodo.exceptions.ResourceNotFoundException;
import yeonleaf.plantodo.provider.JwtBasicProvider;
import yeonleaf.plantodo.service.MemberService;
import yeonleaf.plantodo.service.PlanService;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@RestController
@RequiredArgsConstructor
public class PlanController {

    private final JwtBasicProvider jwtProvider;
    private final PlanService planService;
    private final MemberService memberService;

    @PostMapping("/plan")
    public ResponseEntity<?> save(@Valid @RequestBody PlanReqDto planReqDto,
                                  BindingResult bindingResult,
                                  @RequestHeader(value = "Authorization") String header) {

        if (bindingResult.hasErrors()) {
            throw new ArgumentValidationException("입력값 타입/내용 오류", bindingResult);
        }

        validateEndIsBeforeStart(planReqDto.getStart(), planReqDto.getEnd(), bindingResult);

        if (bindingResult.hasErrors()) {
            throw new ArgumentValidationException("입력값 형식 오류", bindingResult);
        }

        Long memberId = extractMemberId(header);

        Optional<Member> member = memberService.findById(memberId);
        if (member.isEmpty()) {
            throw new ResourceNotFoundException();
        }

        PlanResDto planResDto = planService.save(member.get(), planReqDto);
        return ResponseEntity.status(HttpStatus.CREATED).body(planResDto);

    }

    private void validateEndIsBeforeStart(LocalDate start, LocalDate end, BindingResult bindingResult) {

        if (end.isBefore(start)) {
            bindingResult.rejectValue("end", "range", "end는 start 이전일 수 없습니다.");
        }

    }

    @GetMapping("/plan/{id}")
    public ResponseEntity<?> one(@PathVariable Long id) {
        PlanResDto planResDto = planService.one(id);
        return ResponseEntity.status(HttpStatus.OK).body(planResDto);
    }

    @GetMapping("/plans")
    public ResponseEntity<?> all(@RequestHeader(value = "Authorization") String header) {
        Long memberId = extractMemberId(header);
        List<PlanResDto> planResDtoList = planService.all(memberId);
        return ResponseEntity.status(HttpStatus.OK).body(planResDtoList);
    }

    @DeleteMapping("/plan/{id}")
    public ResponseEntity<?> delete(@PathVariable Long id) {
        PlanResDto plan = Optional.ofNullable(planService.one(id)).orElseThrow(ResourceNotFoundException::new);
        planService.delete(plan.getId());
        return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
    }
//
//    @PatchMapping("/plan/{id}")
//    public ResponseEntity<?> status(@PathVariable Long id) {
//        return ResponseEntity.status(HttpStatus.OK).build();
//    }

    private Long extractMemberId(String header) {
        String token = jwtProvider.extractToken(header);
        return jwtProvider.getIdFromToken(token);
    }

}
