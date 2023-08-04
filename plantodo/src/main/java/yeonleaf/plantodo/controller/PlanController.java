package yeonleaf.plantodo.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
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
import yeonleaf.plantodo.dto.PlanUpdateReqDto;
import yeonleaf.plantodo.exceptions.*;
import yeonleaf.plantodo.provider.JwtBasicProvider;
import yeonleaf.plantodo.service.MemberService;
import yeonleaf.plantodo.service.PlanService;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.methodOn;

@Tag(name = "plan", description = "일정 API")
@RestController
@RequiredArgsConstructor
public class PlanController {

    private final PlanService planService;
    private final PlanModelAssembler planModelAssembler;

    @Operation(summary = "Plan 등록")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "successful operation", content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = PlanResDto.class))),
            @ApiResponse(responseCode = "400", description = "validation errors", content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = ApiBindingError.class))),
            @ApiResponse(responseCode = "401", description = "jwt token errors", content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = ApiSimpleError.class))),
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
        EntityModel<PlanResDto> entityModel = planModelAssembler.toModel(planResDto);
        return ResponseEntity.status(HttpStatus.CREATED).body(entityModel);

    }

    private void validateEndIsBeforeStart(LocalDate start, LocalDate end, BindingResult bindingResult) {

        if (end.isBefore(start)) {
            bindingResult.rejectValue("end", "range", "end는 start 이전일 수 없습니다.");
        }

    }

    @Operation(summary = "Plan 단건 조회")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "successful operation", content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = PlanResDto.class))),
            @ApiResponse(responseCode = "401", description = "jwt token errors", content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = ApiSimpleError.class))),
            @ApiResponse(responseCode = "404", description = "resource not found", content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = ApiSimpleError.class))),
    })
    @GetMapping("/plan/{id}")
    public ResponseEntity<?> one(@Parameter(description = "일정 ID", required = true, example = "1") @PathVariable Long id) {

        PlanResDto planResDto = planService.one(id);
        EntityModel<PlanResDto> entityModel = planModelAssembler.toModel(planResDto);
        return ResponseEntity.status(HttpStatus.OK).body(entityModel);

    }

    @Operation(summary = "Plan 수정")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "successful operation", content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = PlanResDto.class))),
            @ApiResponse(responseCode = "400", description = "validation errors", content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = ApiBindingError.class))),
            @ApiResponse(responseCode = "401", description = "jwt token errors", content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = ApiSimpleError.class))),
            @ApiResponse(responseCode = "404", description = "resource not found", content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = ApiSimpleError.class))),
    })
    @PutMapping("/plan")
    public ResponseEntity<?> update(@Valid @RequestBody PlanUpdateReqDto planUpdateReqDto, BindingResult bindingResult) {

        if (bindingResult.hasErrors()) {
            throw new ArgumentValidationException("입력값 타입/내용 오류", bindingResult);
        }

        PlanResDto planResDto = planService.update(planUpdateReqDto);
        EntityModel<PlanResDto> entityModel = planModelAssembler.toModel(planResDto);
        return ResponseEntity.status(HttpStatus.OK).body(entityModel);

    }

    @Operation(summary = "Plan 삭제")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "successful operation", content = @Content),
            @ApiResponse(responseCode = "404", description = "resource not found", content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = ApiSimpleError.class))),
    })
    @DeleteMapping("/plan/{id}")
    public ResponseEntity<?> delete(@Parameter(description = "일정 ID", required = true, example = "1") @PathVariable Long id) {

        planService.delete(id);
        return ResponseEntity.status(HttpStatus.NO_CONTENT).build();

    }

    @Operation(summary = "Plan 상태 변경 (NOW ↔ COMPLETED)")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "successful operation", content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = PlanResDto.class))),
            @ApiResponse(responseCode = "401", description = "jwt token errors", content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = ApiSimpleError.class))),
            @ApiResponse(responseCode = "404", description = "resource not found", content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = ApiSimpleError.class))),
    })
    @PatchMapping("/plan/{id}")
    public ResponseEntity<?> change(@Parameter(description = "일정 ID", required = true, example = "1") @PathVariable Long id) {

        PlanResDto planResDto = planService.change(id);
        EntityModel<PlanResDto> entityModel = planModelAssembler.toModel(planResDto);
        return ResponseEntity.status(HttpStatus.OK).body(entityModel);

    }

    @Operation(summary = "모든 Plan 조회")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "successful operation", content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = ApiBindingError.class))),
            @ApiResponse(responseCode = "401", description = "jwt token errors", content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = ApiSimpleError.class))),
            @ApiResponse(responseCode = "404", description = "resource not found", content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = ApiSimpleError.class)))
    })
    @GetMapping(value = "/plans", params = {"memberId"})
    public ResponseEntity<?> all(@Parameter(description = "회원 ID", required = true, example = "1") @RequestParam Long memberId) {

        List<EntityModel<PlanResDto>> all = planService.all(memberId).stream().map(planModelAssembler::toModel).toList();
        CollectionModel<EntityModel<PlanResDto>> collectionModel = CollectionModel.of(all, linkTo(methodOn(PlanController.class)).withSelfRel());
        return ResponseEntity.status(HttpStatus.OK).body(collectionModel);

    }

    @Operation(summary = "여러 개의 Plan 조회 (날짜로 필터)")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "successful operation", content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = ApiBindingError.class))),
            @ApiResponse(responseCode = "401", description = "jwt token errors", content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = ApiSimpleError.class))),
            @ApiResponse(responseCode = "404", description = "resource not found", content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = ApiSimpleError.class)))
    })
    @GetMapping(value = "/plans/date", params = {"memberId", "dateKey"})
    public ResponseEntity<?> all(@Parameter(description = "회원 ID", required = true, example = "1") @RequestParam Long memberId,
                                 @Parameter(description = "검색일", required = true, example = "2023-08-04") @RequestParam LocalDate dateKey) {

        List<EntityModel<PlanResDto>> all = planService.all(memberId, dateKey).stream().map(planModelAssembler::toModel).toList();
        CollectionModel<EntityModel<PlanResDto>> collectionModel = CollectionModel.of(all,
                linkTo(methodOn(PlanController.class).all(memberId, dateKey)).withSelfRel(),
                linkTo(methodOn(PlanController.class).all(memberId)).withRel("plans"));
        return ResponseEntity.status(HttpStatus.OK).body(collectionModel);

    }

    @Operation(summary = "여러 개의 Plan 조회 (기간으로 필터)")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "successful operation", content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = ApiBindingError.class))),
            @ApiResponse(responseCode = "401", description = "jwt token errors", content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = ApiSimpleError.class))),
            @ApiResponse(responseCode = "404", description = "resource not found", content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = ApiSimpleError.class)))
    })
    @GetMapping(value = "/plans/range", params = {"memberId", "searchStart", "searchEnd"})
    public ResponseEntity<?> all(@Parameter(description = "회원 ID", required = true, example = "1") @RequestParam Long memberId,
                                 @Parameter(description = "검색 시작일", required = true, example = "2023-08-04") @RequestParam LocalDate searchStart,
                                 @Parameter(description = "검색 종료일", required = true, example = "2023-08-17")@RequestParam LocalDate searchEnd) {

        checkSearchDates(searchStart, searchEnd);

        List<EntityModel<PlanResDto>> all = planService.all(memberId, searchStart, searchEnd).stream().map(planModelAssembler::toModel).toList();
        CollectionModel<EntityModel<PlanResDto>> collectionModel = CollectionModel.of(all,
                linkTo(methodOn(PlanController.class).all(memberId, searchStart, searchEnd)).withSelfRel(),
                linkTo(methodOn(PlanController.class).all(memberId)).withRel("plans"));
        return ResponseEntity.status(HttpStatus.OK).body(collectionModel);

    }

    private void checkSearchDates(LocalDate searchStart, LocalDate searchEnd) {

        QueryStringValidationException errors = new QueryStringValidationException();
        if (searchStart.isAfter(searchEnd)) {
            errors.rejectValue("searchStart", "searchStart는 searchEnd 이전일 수 없습니다.");
            errors.rejectValue("searchEnd", "searchEnd는 searchStart 이전일 수 없습니다.");
            throw errors;
        }

    }

}
