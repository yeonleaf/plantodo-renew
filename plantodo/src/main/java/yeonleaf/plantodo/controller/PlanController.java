package yeonleaf.plantodo.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.hateoas.EntityModel;
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
import java.util.Optional;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.methodOn;

@RestController
@RequestMapping("/plan")
@RequiredArgsConstructor
public class PlanController {

    private final JwtBasicProvider jwtProvider;
    private final PlanService planService;
    private final MemberService memberService;

    @PostMapping
    public ResponseEntity<?> save(@RequestHeader(value = "Authorization") String header,
                                  @Valid @RequestBody PlanReqDto planReqDto,
                                  BindingResult bindingResult) {
        if (bindingResult.hasErrors()) {
            throw new ArgumentValidationException("입력값 타입/내용 오류", bindingResult);
        }

        validateEndIsBeforeStart(planReqDto.getStart(), planReqDto.getEnd(), bindingResult);

        if (bindingResult.hasErrors()) {
            throw new ArgumentValidationException("입력값 형식 오류", bindingResult);
        }

        String token = jwtProvider.extractToken(header);
        Long memberId = jwtProvider.getIdFromToken(token);

        Optional<Member> member = memberService.findById(memberId);
        if (member.isEmpty()) {
            throw new ResourceNotFoundException();
        }

        PlanResDto planResDto = planService.save(member.get(), planReqDto);
        EntityModel<PlanResDto> planEntityModel = EntityModel.of(planResDto, linkTo(methodOn(PlanController.class).one(planResDto.getId())).withSelfRel());
        return ResponseEntity.status(HttpStatus.CREATED).body(planEntityModel);
    }

    private void validateEndIsBeforeStart(LocalDate start, LocalDate end, BindingResult bindingResult) {
        if (end.isBefore(start)) {
            bindingResult.rejectValue("end", "range", "end는 start 이전일 수 없습니다.");
        }
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> one(@PathVariable Long id) {
        return ResponseEntity.status(HttpStatus.OK).body(EntityModel.of(new PlanResDto(id, "test", LocalDate.now(), LocalDate.now().plusDays(3))));
    }

}
