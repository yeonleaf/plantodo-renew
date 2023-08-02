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
import yeonleaf.plantodo.assembler.CheckboxModelAssembler;
import yeonleaf.plantodo.dto.CheckboxReqDto;
import yeonleaf.plantodo.dto.CheckboxResDto;
import yeonleaf.plantodo.dto.CheckboxUpdateReqDto;
import yeonleaf.plantodo.dto.PlanResDto;
import yeonleaf.plantodo.exceptions.ApiBindingError;
import yeonleaf.plantodo.exceptions.ApiSimpleError;
import yeonleaf.plantodo.exceptions.ArgumentValidationException;
import yeonleaf.plantodo.exceptions.QueryStringValidationException;
import yeonleaf.plantodo.service.CheckboxService;

import java.time.LocalDate;
import java.util.List;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.methodOn;

@RestController
@RequestMapping
@RequiredArgsConstructor
public class CheckboxController {

    private final CheckboxService checkboxService;
    private final CheckboxModelAssembler checkboxModelAssembler;

    @Operation(summary = "Checkbox 등록")
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

    @Operation(summary = "Checkbox 단건 조회")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "successful operation", content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = CheckboxResDto.class))),
            @ApiResponse(responseCode = "401", description = "jwt token errors", content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = ApiSimpleError.class))),
            @ApiResponse(responseCode = "404", description = "resource not found", content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = ApiSimpleError.class))),
    })
    @GetMapping("/checkbox/{id}")
    public ResponseEntity<?> one(@PathVariable Long id) {

        CheckboxResDto checkboxResDto = checkboxService.one(id);
        EntityModel<CheckboxResDto> entityModel = checkboxModelAssembler.toModel(checkboxResDto);
        return ResponseEntity.status(HttpStatus.OK).body(entityModel);

    }

    @Operation(summary = "Checkbox 수정")
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

    @Operation(summary = "Checkbox 삭제")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "successful operation", content = @Content),
            @ApiResponse(responseCode = "401", description = "jwt token errors", content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = ApiSimpleError.class))),
            @ApiResponse(responseCode = "404", description = "resource not found", content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = ApiSimpleError.class))),
    })
    @DeleteMapping("/checkbox/{id}")
    public ResponseEntity<?> delete(@PathVariable Long id) {

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
    public ResponseEntity<?> change(@PathVariable Long id) {

        CheckboxResDto checkboxResDto = checkboxService.change(id);
        EntityModel<CheckboxResDto> entityModel = checkboxModelAssembler.toModel(checkboxResDto);
        return ResponseEntity.status(HttpStatus.OK).body(entityModel);

    }

    @Operation(summary = "Plan 내에 있는 모든 Checkbox 조회 (standard = plan) | Group 내에 있는 모든 Checkbox 조회 (standard = group)")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "successful operation", content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = ApiBindingError.class))),
            @ApiResponse(responseCode = "401", description = "jwt token errors", content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = ApiSimpleError.class))),
            @ApiResponse(responseCode = "404", description = "resource not found", content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = ApiSimpleError.class)))
    })
    @GetMapping(value = "/checkboxes", params = {"standard", "standardId"})
    public ResponseEntity<?> all(@RequestParam String standard, @RequestParam Long standardId) {

        CollectionModel<EntityModel<CheckboxResDto>> collectionModel = checkboxModelAssembler.toCollectionModel(allByEntity(standard, standardId));
        return ResponseEntity.status(HttpStatus.OK).body(collectionModel);

    }

    private List<CheckboxResDto> allByEntity(String standard, Long standardId) {

        QueryStringValidationException errors = new QueryStringValidationException();

        if (!standard.equalsIgnoreCase("plan") && !standard.equalsIgnoreCase("group")) {
            errors.rejectValue("standard", "must be plan or group");
            throw errors;
        }

        return standard.equalsIgnoreCase("plan") ? checkboxService.allByPlan(standardId) : checkboxService.allByGroup(standardId);

    }

    @Operation(summary = "(날짜로 필터) Plan 내에서 여러 개의 Checkbox 조회 (standard = plan) | Group 내에서 여러 개의 Checkbox 조회 (standard = group)")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "successful operation", content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = ApiBindingError.class))),
            @ApiResponse(responseCode = "401", description = "jwt token errors", content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = ApiSimpleError.class))),
            @ApiResponse(responseCode = "404", description = "resource not found", content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = ApiSimpleError.class)))
    })
    @GetMapping(value = "/checkboxes", params = {"standard", "standardId", "dateKey"})
    public ResponseEntity<?> all(@RequestParam String standard, @RequestParam Long standardId, @RequestParam LocalDate dateKey) {

        CollectionModel<EntityModel<CheckboxResDto>> collectionModel = checkboxModelAssembler.toCollectionModel(allByEntity(standard, standardId, dateKey));
        return ResponseEntity.status(HttpStatus.OK).body(collectionModel);

    }

    private List<CheckboxResDto> allByEntity(String standard, Long standardId, LocalDate dateKey) {

        QueryStringValidationException errors = new QueryStringValidationException();

        if (!standard.equalsIgnoreCase("plan") && !standard.equalsIgnoreCase("group")) {
            errors.rejectValue("standard", "must be plan or group");
            throw errors;
        }

        return standard.equalsIgnoreCase("plan") ? checkboxService.allByPlan(standardId, dateKey) : checkboxService.allByGroup(standardId, dateKey);

    }

    @GetMapping(value = "/checkboxes", params = {"standard", "standardId", "searchStart", "searchEnd"})
    public ResponseEntity<?> all(@RequestParam String standard, @RequestParam Long standardId,
                                 @RequestParam LocalDate searchStart, @RequestParam LocalDate searchEnd) {

        checkSearchDates(searchStart, searchEnd);
        CollectionModel<EntityModel<CheckboxResDto>> collectionModel = checkboxModelAssembler.toCollectionModel(allByEntity(standard, standardId, searchStart, searchEnd));
        return ResponseEntity.status(HttpStatus.OK).body(collectionModel);

    }

    private List<CheckboxResDto> allByEntity(String standard, Long standardId, LocalDate searchStart, LocalDate searchEnd) {

        QueryStringValidationException errors = new QueryStringValidationException();

        if (!standard.equalsIgnoreCase("plan") && !standard.equalsIgnoreCase("group")) {
            errors.rejectValue("standard", "must be plan or group");
            throw errors;
        }

        return standard.equalsIgnoreCase("plan")
                ? checkboxService.allByPlan(standardId, searchStart, searchEnd)
                : checkboxService.allByGroup(standardId, searchStart, searchEnd);

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
