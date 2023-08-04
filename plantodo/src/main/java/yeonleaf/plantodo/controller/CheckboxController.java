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
import org.springframework.hateoas.Link;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import yeonleaf.plantodo.assembler.CheckboxModelAssembler;
import yeonleaf.plantodo.dto.CheckboxReqDto;
import yeonleaf.plantodo.dto.CheckboxResDto;
import yeonleaf.plantodo.dto.CheckboxUpdateReqDto;
import yeonleaf.plantodo.exceptions.ApiBindingError;
import yeonleaf.plantodo.exceptions.ApiSimpleError;
import yeonleaf.plantodo.exceptions.ArgumentValidationException;
import yeonleaf.plantodo.exceptions.QueryStringValidationException;
import yeonleaf.plantodo.service.CheckboxService;

import java.time.LocalDate;
import java.util.List;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.methodOn;

@Tag(name = "checkbox", description = "할 일 API")
@RestController
@RequestMapping
@RequiredArgsConstructor
public class CheckboxController {

    private final CheckboxService checkboxService;
    private final CheckboxModelAssembler checkboxModelAssembler;

    @Operation(summary = "할 일 등록")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "successful operation", content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = CheckboxResDto.class))),
            @ApiResponse(responseCode = "400", description = "validation errors", content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = ApiBindingError.class))),
            @ApiResponse(responseCode = "401", description = "jwt token errors", content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = ApiSimpleError.class))),
            @ApiResponse(responseCode = "404", description = "resource not found", content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = ApiSimpleError.class))),
    })
    @PostMapping("/checkbox")
    public ResponseEntity<?> save(@Valid @RequestBody CheckboxReqDto checkboxReqDto, BindingResult bindingResult) {

        if (bindingResult.hasErrors()) {
            throw new ArgumentValidationException("입력값 타입/내용 오류", bindingResult);
        }
        CheckboxResDto checkboxResDto = checkboxService.save(checkboxReqDto);
        EntityModel<CheckboxResDto> entityModel = checkboxModelAssembler.toModel(checkboxResDto);
        return ResponseEntity.status(HttpStatus.CREATED).body(entityModel);

    }

    @Operation(summary = "할 일 단건 조회")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "successful operation", content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = CheckboxResDto.class))),
            @ApiResponse(responseCode = "401", description = "jwt token errors", content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = ApiSimpleError.class))),
            @ApiResponse(responseCode = "404", description = "resource not found", content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = ApiSimpleError.class))),
    })
    @GetMapping("/checkbox/{id}")
    public ResponseEntity<?> one(@Parameter(description = "할 일 ID", required = true, example = "1") @PathVariable Long id) {

        CheckboxResDto checkboxResDto = checkboxService.one(id);
        EntityModel<CheckboxResDto> entityModel = checkboxModelAssembler.toModel(checkboxResDto);
        return ResponseEntity.status(HttpStatus.OK).body(entityModel);

    }

    @Operation(summary = "할 일 수정")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "successful operation", content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = CheckboxResDto.class))),
            @ApiResponse(responseCode = "400", description = "validation errors", content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = ApiBindingError.class))),
            @ApiResponse(responseCode = "401", description = "jwt token errors", content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = ApiSimpleError.class))),
            @ApiResponse(responseCode = "404", description = "resource not found", content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = ApiSimpleError.class))),
    })
    @PutMapping("/checkbox")
    public ResponseEntity<?> update(@Valid @RequestBody CheckboxUpdateReqDto checkboxUpdateReqDto, BindingResult bindingResult) {

        if (bindingResult.hasErrors()) {
            throw new ArgumentValidationException("입력값 타입/내용 오류", bindingResult);
        }

        CheckboxResDto checkboxResDto = checkboxService.update(checkboxUpdateReqDto);
        EntityModel<CheckboxResDto> entityModel = checkboxModelAssembler.toModel(checkboxResDto);
        return ResponseEntity.status(HttpStatus.OK).body(entityModel);

    }

    @Operation(summary = "할 일 삭제")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "successful operation", content = @Content),
            @ApiResponse(responseCode = "401", description = "jwt token errors", content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = ApiSimpleError.class))),
            @ApiResponse(responseCode = "404", description = "resource not found", content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = ApiSimpleError.class))),
    })
    @DeleteMapping("/checkbox/{id}")
    public ResponseEntity<?> delete(@Parameter(description = "할 일 ID", required = true, example = "1") @PathVariable Long id) {

        checkboxService.delete(id);
        return ResponseEntity.status(HttpStatus.NO_CONTENT).build();

    }

    @Operation(summary = "Checkbox 상태 변경 (checked ↔ unchecked)")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "successful operation", content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = CheckboxResDto.class))),
            @ApiResponse(responseCode = "401", description = "jwt token errors", content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = ApiSimpleError.class))),
            @ApiResponse(responseCode = "404", description = "resource not found", content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = ApiSimpleError.class))),
    })
    @PatchMapping("/checkbox/{id}")
    public ResponseEntity<?> change(@Parameter(description = "할 일 ID", required = true, example = "1") @PathVariable Long id) {

        CheckboxResDto checkboxResDto = checkboxService.change(id);
        EntityModel<CheckboxResDto> entityModel = checkboxModelAssembler.toModel(checkboxResDto);
        return ResponseEntity.status(HttpStatus.OK).body(entityModel);

    }

    @Operation(summary = "그룹 안에 있는 모든 할 일 조회")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "successful operation", content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = ApiBindingError.class))),
            @ApiResponse(responseCode = "401", description = "jwt token errors", content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = ApiSimpleError.class))),
            @ApiResponse(responseCode = "404", description = "resource not found", content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = ApiSimpleError.class)))
    })
    @GetMapping(value = "/checkboxes/group", params = {"groupId"})
    public ResponseEntity<?> allByGroup(@Parameter(description = "그룹 ID", required = true, example = "1") @RequestParam Long groupId) {

        CollectionModel<EntityModel<CheckboxResDto>> collectionModel = checkboxModelAssembler.toCollectionModel(checkboxService.allByGroup(groupId));
        Link additionalLink = linkTo(methodOn(GroupController.class).one(groupId)).withRel("group");
        collectionModel.add(additionalLink);
        return ResponseEntity.status(HttpStatus.OK).body(collectionModel);

    }

    @Operation(summary = "일정 안에 있는 모든 할 일 조회")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "successful operation", content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = ApiBindingError.class))),
            @ApiResponse(responseCode = "401", description = "jwt token errors", content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = ApiSimpleError.class))),
            @ApiResponse(responseCode = "404", description = "resource not found", content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = ApiSimpleError.class)))
    })
    @GetMapping(value = "/checkboxes/plan", params = {"planId"})
    public ResponseEntity<?> allByPlan(@Parameter(description = "일정 ID", required = true, example = "1") @RequestParam Long planId) {

        CollectionModel<EntityModel<CheckboxResDto>> collectionModel = checkboxModelAssembler.toCollectionModel(checkboxService.allByPlan(planId));
        Link additionalLink = linkTo(methodOn(PlanController.class).one(planId)).withRel("plan");
        collectionModel.add(additionalLink);
        return ResponseEntity.status(HttpStatus.OK).body(collectionModel);

    }

    @Operation(summary = "그룹 안에 있는 모든 할 일 조회 (날짜로 필터)")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "successful operation", content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = ApiBindingError.class))),
            @ApiResponse(responseCode = "401", description = "jwt token errors", content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = ApiSimpleError.class))),
            @ApiResponse(responseCode = "404", description = "resource not found", content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = ApiSimpleError.class)))
    })
    @GetMapping(value = "/checkboxes/group/date", params = {"groupId", "dateKey"})
    public ResponseEntity<?> allByGroup(@Parameter(description = "그룹 ID", required = true, example = "1") @RequestParam Long groupId,
                                        @Parameter(description = "검색일", required = true, example = "2023-08-04") @RequestParam LocalDate dateKey) {

        CollectionModel<EntityModel<CheckboxResDto>> collectionModel = checkboxModelAssembler.toCollectionModel(checkboxService.allByGroup(groupId, dateKey));
        Link additionalLink = linkTo(methodOn(GroupController.class).one(groupId)).withRel("group");
        collectionModel.add(additionalLink);
        return ResponseEntity.status(HttpStatus.OK).body(collectionModel);

    }

    @Operation(summary = "일정 안에 있는 모든 할 일 조회 (날짜로 필터)")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "successful operation", content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = ApiBindingError.class))),
            @ApiResponse(responseCode = "401", description = "jwt token errors", content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = ApiSimpleError.class))),
            @ApiResponse(responseCode = "404", description = "resource not found", content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = ApiSimpleError.class)))
    })
    @GetMapping(value = "/checkboxes/plan/date", params = {"planId", "dateKey"})
    public ResponseEntity<?> allByPlan(@Parameter(description = "일정 ID", required = true, example = "1") @RequestParam Long planId,
                                       @Parameter(description = "검색일", required = true, example = "2023-08-04") @RequestParam LocalDate dateKey) {

        CollectionModel<EntityModel<CheckboxResDto>> collectionModel = checkboxModelAssembler.toCollectionModel(checkboxService.allByPlan(planId, dateKey));
        Link additionalLink = linkTo(methodOn(PlanController.class).one(planId)).withRel("plan");
        collectionModel.add(additionalLink);
        return ResponseEntity.status(HttpStatus.OK).body(collectionModel);

    }

    @Operation(summary = "그룹 안에 있는 모든 할 일 조회 (기간으로 필터)")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "successful operation", content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = ApiBindingError.class))),
            @ApiResponse(responseCode = "401", description = "jwt token errors", content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = ApiSimpleError.class))),
            @ApiResponse(responseCode = "404", description = "resource not found", content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = ApiSimpleError.class)))
    })
    @GetMapping(value = "/checkboxes/group/range", params = {"groupId", "searchStart", "searchEnd"})
    public ResponseEntity<?> allByGroup(@Parameter(description = "그룹 ID", required = true, example = "1") @RequestParam Long groupId,
                                        @Parameter(description = "검색 시작일", required = true, example = "2023-08-04") @RequestParam LocalDate searchStart,
                                        @Parameter(description = "검색 종료일", required = true, example = "2023-08-10") @RequestParam LocalDate searchEnd) {

        checkSearchDates(searchStart, searchEnd);
        CollectionModel<EntityModel<CheckboxResDto>> collectionModel = checkboxModelAssembler.toCollectionModel(checkboxService.allByGroup(groupId, searchStart, searchEnd));
        Link additionalLink = linkTo(methodOn(GroupController.class).one(groupId)).withRel("group");
        collectionModel.add(additionalLink);
        return ResponseEntity.status(HttpStatus.OK).body(collectionModel);

    }

    @Operation(summary = "일정 안에 있는 모든 할 일 조회 (기간으로 필터)")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "successful operation", content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = ApiBindingError.class))),
            @ApiResponse(responseCode = "401", description = "jwt token errors", content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = ApiSimpleError.class))),
            @ApiResponse(responseCode = "404", description = "resource not found", content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = ApiSimpleError.class)))
    })
    @GetMapping(value = "/checkboxes/plan/range", params = {"planId", "searchStart", "searchEnd"})
    public ResponseEntity<?> allByPlan(@Parameter(description = "일정 ID", required = true, example = "1") @RequestParam Long planId,
                                       @Parameter(description = "검색 시작일", required = true, example = "2023-08-04") @RequestParam LocalDate searchStart,
                                       @Parameter(description = "검색 종료일", required = true, example = "2023-08-10") @RequestParam LocalDate searchEnd) {

        checkSearchDates(searchStart, searchEnd);
        CollectionModel<EntityModel<CheckboxResDto>> collectionModel = checkboxModelAssembler.toCollectionModel(checkboxService.allByPlan(planId, searchStart, searchEnd));
        Link additionalLink = linkTo(methodOn(PlanController.class).one(planId)).withRel("plan");
        collectionModel.add(additionalLink);
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
