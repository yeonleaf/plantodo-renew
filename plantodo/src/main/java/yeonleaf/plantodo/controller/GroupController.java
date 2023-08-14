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
import yeonleaf.plantodo.assembler.GroupModelAssembler;
import yeonleaf.plantodo.dto.*;
import yeonleaf.plantodo.exceptions.ApiBindingError;
import yeonleaf.plantodo.exceptions.ApiSimpleError;
import yeonleaf.plantodo.exceptions.ArgumentValidationException;
import yeonleaf.plantodo.exceptions.QueryStringValidationException;
import yeonleaf.plantodo.service.GroupService;
import yeonleaf.plantodo.validator.RepInputValidator;

import java.time.LocalDate;

@Tag(name = "group", description = "주기적으로 반복되는 할 일을 관리하는 그룹 API")
@RestController
@RequiredArgsConstructor
public class GroupController {
    private final RepInputValidator repInputValidator = new RepInputValidator();
    private final GroupService groupService;
    private final GroupModelAssembler groupModelAssembler;

    @Operation(summary = "그룹 등록", description = "그룹 등록시 할 일이 자동으로 생성")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "successful operation", content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = GroupResDto.class))),
            @ApiResponse(responseCode = "400", description = "validation errors", content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = ApiBindingError.class))),
            @ApiResponse(responseCode = "401", description = "jwt token errors", content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = ApiSimpleError.class))),
            @ApiResponse(responseCode = "404", description = "resource not found", content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = ApiSimpleError.class))),
    })
    @PostMapping("/group")
    public ResponseEntity<?> save(@Valid @RequestBody GroupReqDto groupReqDto, BindingResult bindingResult) {

        if (bindingResult.hasErrors()) {
            throw new ArgumentValidationException("입력값 타입/내용 오류", bindingResult);
        }

        RepInputDto repInputDto = new RepInputDto(groupReqDto.getRepOption(), groupReqDto.getRepValue());
        repInputValidator.validate(repInputDto, bindingResult);

        if (bindingResult.hasErrors()) {
            throw new ArgumentValidationException("입력값 형식 오류", bindingResult);
        }

        GroupResDto groupResDto = groupService.save(groupReqDto);
        EntityModel<GroupResDto> entityModel = groupModelAssembler.toModel(groupResDto);
        return ResponseEntity.status(HttpStatus.CREATED).body(entityModel);

    }

    @Operation(summary = "그룹 단건 조회")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "successful operation", content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = GroupResDto.class))),
            @ApiResponse(responseCode = "401", description = "jwt token errors", content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = ApiSimpleError.class))),
            @ApiResponse(responseCode = "404", description = "resource not found", content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = ApiSimpleError.class))),
    })
    @GetMapping("/group/{id}")
    public ResponseEntity<?> one(@Parameter(description = "그룹 ID", required = true, example = "1") @PathVariable Long id) {

        GroupResDto groupResDto = groupService.one(id);
        EntityModel<GroupResDto> entityModel = groupModelAssembler.toModel(groupResDto);
        return ResponseEntity.status(HttpStatus.OK).body(entityModel);

    }

    @Operation(summary = "그룹 수정")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "successful operation", content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = GroupResDto.class))),
            @ApiResponse(responseCode = "400", description = "validation errors", content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = ApiBindingError.class))),
            @ApiResponse(responseCode = "401", description = "jwt token errors", content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = ApiSimpleError.class))),
            @ApiResponse(responseCode = "404", description = "resource not found", content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = ApiSimpleError.class))),
    })
    @PutMapping("/group")
    public ResponseEntity<?> update(@Valid @RequestBody GroupUpdateReqDto groupUpdateReqDto, BindingResult bindingResult) {

        if (bindingResult.hasErrors()) {
            throw new ArgumentValidationException("입력값 타입/내용 오류", bindingResult);
        }

        repInputValidator.validate(new RepInputDto(groupUpdateReqDto.getRepOption(), groupUpdateReqDto.getRepValue()), bindingResult);
        if (bindingResult.hasErrors()) {
            throw new ArgumentValidationException("입력값 형식 오류", bindingResult);
        }

        GroupResDto groupResDto = groupService.update(groupUpdateReqDto);
        EntityModel<GroupResDto> entityModel = groupModelAssembler.toModel(groupResDto);
        return ResponseEntity.status(HttpStatus.OK).body(entityModel);

    }

    @Operation(summary = "그룹 삭제")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "successful operation", content = @Content),
            @ApiResponse(responseCode = "401", description = "jwt token errors", content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = ApiSimpleError.class))),
            @ApiResponse(responseCode = "404", description = "resource not found", content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = ApiSimpleError.class))),
    })
    @DeleteMapping("/group/{id}")
    public ResponseEntity<?> delete(@Parameter(description = "그룹 ID", required = true, example = "1") @PathVariable Long id) {

        groupService.delete(id);
        return ResponseEntity.status(HttpStatus.NO_CONTENT).build();

    }

    @Operation(summary = "일정 내의 모든 그룹을 조회")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "successful operation", content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = CollectionModel.class))),
            @ApiResponse(responseCode = "401", description = "jwt token errors", content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = ApiSimpleError.class))),
            @ApiResponse(responseCode = "404", description = "resource not found", content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = ApiSimpleError.class)))
    })
    @GetMapping(value = "/groups", params = {"planId"})
    public ResponseEntity<?> all(@Parameter(description = "일정 ID", required = true, example = "1") @RequestParam Long planId) {

        CollectionModel<EntityModel<GroupResDto>> collectionModel = groupModelAssembler.toCollectionModel(groupService.all(planId));
        return ResponseEntity.status(HttpStatus.OK).body(collectionModel);

    }

    @Operation(summary = "일정 내의 모든 그룹을 조회 (날짜로 필터)")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "successful operation", content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = CollectionModel.class))),
            @ApiResponse(responseCode = "400", description = "query string validation", content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = ApiBindingError.class))),
            @ApiResponse(responseCode = "401", description = "jwt token errors", content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = ApiSimpleError.class))),
            @ApiResponse(responseCode = "404", description = "resource not found", content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = ApiSimpleError.class)))
    })
    @GetMapping(value = "/groups/date", params = {"planId", "dateKey"})
    public ResponseEntity<?> all(@Parameter(description = "일정 ID", required = true, example = "1") @RequestParam Long planId,
                                 @Parameter(description = "검색일", required = true, example = "2023-08-04") @RequestParam LocalDate dateKey) {

        CollectionModel<EntityModel<GroupResDto>> collectionModel = groupModelAssembler.toCollectionModel(groupService.all(planId, dateKey));
        return ResponseEntity.status(HttpStatus.OK).body(collectionModel);

    }

    @Operation(summary = "일정 내의 모든 그룹을 조회 (기간으로 필터)")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "successful operation", content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = CollectionModel.class))),
            @ApiResponse(responseCode = "400", description = "query string validation", content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = ApiBindingError.class))),
            @ApiResponse(responseCode = "401", description = "jwt token errors", content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = ApiSimpleError.class))),
            @ApiResponse(responseCode = "404", description = "resource not found", content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = ApiSimpleError.class)))
    })
    @GetMapping(value = "/groups/range", params = {"planId", "searchStart", "searchEnd"})
    public ResponseEntity<?> all(@Parameter(description = "일정 ID", required = true, example = "1") @RequestParam Long planId,
                                 @Parameter(description = "검색 시작일", required = true, example = "2023-08-04") @RequestParam LocalDate searchStart,
                                 @Parameter(description = "검색 종료일", required = true, example = "2023-08-15") @RequestParam LocalDate searchEnd) {

        checkSearchDates(searchStart, searchEnd);
        CollectionModel<EntityModel<GroupResDto>> collectionModel = groupModelAssembler.toCollectionModel(groupService.all(planId, searchStart, searchEnd));
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
