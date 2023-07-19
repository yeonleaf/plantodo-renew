package yeonleaf.plantodo.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.hateoas.CollectionModel;
import org.springframework.hateoas.EntityModel;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import yeonleaf.plantodo.assembler.PlanModelAssembler;
import yeonleaf.plantodo.domain.Member;
import yeonleaf.plantodo.dto.MemberResDto;
import yeonleaf.plantodo.dto.PlanReqDto;
import yeonleaf.plantodo.dto.PlanResDto;
import yeonleaf.plantodo.exceptions.ApiBindingError;
import yeonleaf.plantodo.exceptions.ApiSimpleError;
import yeonleaf.plantodo.exceptions.ArgumentValidationException;
import yeonleaf.plantodo.exceptions.ResourceNotFoundException;
import yeonleaf.plantodo.provider.JwtBasicProvider;
import yeonleaf.plantodo.service.MemberService;
import yeonleaf.plantodo.service.PlanService;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.methodOn;

@RestController
@RequiredArgsConstructor
public class PlanController {

    private final PlanService planService;
    private final PlanModelAssembler planModelAssembler;

    @Operation(summary = "Plan 등록")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "successful operation", content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = PlanResDto.class))),
            @ApiResponse(responseCode = "400", description = "validation errors", content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = ApiBindingError.class))),
            @ApiResponse(responseCode = "404", description = "resource not found", content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = ApiSimpleError.class))),
    })
    @PostMapping("/plan")
    public ResponseEntity<?> save(@Valid @RequestBody PlanReqDto planReqDto,
                                  BindingResult bindingResult) {

        if (bindingResult.hasErrors()) {
            throw new ArgumentValidationException("입력값 타입/내용 오류", bindingResult);
        }

        validateEndIsBeforeStart(planReqDto.getStart(), planReqDto.getEnd(), bindingResult);

        if (bindingResult.hasErrors()) {
            throw new ArgumentValidationException("입력값 형식 오류", bindingResult);
        }

        PlanResDto planResDto = planService.save(planReqDto);
        EntityModel<PlanResDto> entityModel = EntityModel.of(planResDto, linkTo(methodOn(PlanController.class).one(planResDto.getId())).withSelfRel());
        return ResponseEntity.status(HttpStatus.CREATED).body(entityModel);

    }

    private void validateEndIsBeforeStart(LocalDate start, LocalDate end, BindingResult bindingResult) {

        if (end.isBefore(start)) {
            bindingResult.rejectValue("end", "range", "end는 start 이전일 수 없습니다.");
        }

    }

    @GetMapping("/plan/{id}")
    public ResponseEntity<?> one(@PathVariable Long id) {

        PlanResDto planResDto = planService.one(id);
        EntityModel<PlanResDto> entityModel = planModelAssembler.toModel(planResDto);
        return ResponseEntity.status(HttpStatus.OK).body(entityModel);

    }

//    @GetMapping("/plans")
//    public ResponseEntity<?> all(@RequestParam(name = "memberId") Long memberId) {
//
//        List<EntityModel<PlanResDto>> planResDtoList = planService.all(memberId).stream()
//                .map(planModelAssembler::toModel)
//                .collect(Collectors.toList());
//        return ResponseEntity.status(HttpStatus.OK).body(planResDtoList);
//
//    }
//
//    @DeleteMapping("/plan/{id}")
//    public ResponseEntity<?> removal(@PathVariable Long id) {
//        PlanResDto plan = Optional.ofNullable(planService.one(id)).orElseThrow(ResourceNotFoundException::new);
//        planService.delete(plan.getId());
//        return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
//    }
//
//    @PatchMapping("/plan/{id}")
//    public ResponseEntity<?> status(@PathVariable Long id) {
//        return ResponseEntity.status(HttpStatus.OK).build();
//    }

}
