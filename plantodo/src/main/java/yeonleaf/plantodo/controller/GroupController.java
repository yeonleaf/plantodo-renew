package yeonleaf.plantodo.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.hateoas.EntityModel;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import yeonleaf.plantodo.dto.GroupReqDto;
import yeonleaf.plantodo.dto.GroupResDto;
import yeonleaf.plantodo.dto.GroupUpdateReqDto;
import yeonleaf.plantodo.dto.RepInputDto;
import yeonleaf.plantodo.exceptions.ApiBindingError;
import yeonleaf.plantodo.exceptions.ApiSimpleError;
import yeonleaf.plantodo.exceptions.ArgumentValidationException;
import yeonleaf.plantodo.service.GroupService;
import yeonleaf.plantodo.validator.RepInputValidator;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.methodOn;

@RestController
@RequestMapping("/group")
@RequiredArgsConstructor
public class GroupController {
    private final RepInputValidator repInputValidator = new RepInputValidator();
    private final GroupService groupService;

    @Operation(summary = "Group 등록")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "successful operation", content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = GroupResDto.class))),
            @ApiResponse(responseCode = "400", description = "validation errors", content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = ApiBindingError.class))),
            @ApiResponse(responseCode = "404", description = "resource not found", content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = ApiSimpleError.class))),
    })
    @PostMapping
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
        EntityModel<GroupResDto> entityModel = EntityModel.of(groupResDto, linkTo(methodOn(GroupController.class).one(groupResDto.getId())).withSelfRel());
        return ResponseEntity.status(HttpStatus.CREATED).body(entityModel);

    }

    @GetMapping("/{id}")
    public ResponseEntity<?> one(@PathVariable Long id) {

        GroupResDto groupResDto = groupService.one(id);
        EntityModel<GroupResDto> entityModel = EntityModel.of(groupResDto, linkTo(methodOn(GroupController.class).one(id)).withSelfRel());
        return ResponseEntity.status(HttpStatus.OK).body(entityModel);

    }

    @Operation(summary = "Group 수정")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "successful operation", content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = GroupResDto.class))),
            @ApiResponse(responseCode = "400", description = "validation errors", content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = ApiBindingError.class))),
            @ApiResponse(responseCode = "404", description = "resource not found", content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = ApiSimpleError.class))),
    })
    @PutMapping
    public ResponseEntity<?> update(@Valid @RequestBody GroupUpdateReqDto groupUpdateReqDto, BindingResult bindingResult) {

        if (bindingResult.hasErrors()) {
            throw new ArgumentValidationException("입력값 타입/내용 오류", bindingResult);
        }

        repInputValidator.validate(new RepInputDto(groupUpdateReqDto.getRepOption(), groupUpdateReqDto.getRepValue()), bindingResult);
        if (bindingResult.hasErrors()) {
            throw new ArgumentValidationException("입력값 형식 오류", bindingResult);
        }

        GroupResDto groupResDto = groupService.update(groupUpdateReqDto);
        EntityModel<GroupResDto> entityModel = EntityModel.of(groupResDto, linkTo(methodOn(GroupController.class).one(groupResDto.getId())).withSelfRel());
        return ResponseEntity.status(HttpStatus.OK).body(entityModel);

    }
}
