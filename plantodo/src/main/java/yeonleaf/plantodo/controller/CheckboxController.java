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
import yeonleaf.plantodo.dto.CheckboxReqDto;
import yeonleaf.plantodo.dto.CheckboxResDto;
import yeonleaf.plantodo.exceptions.ApiBindingError;
import yeonleaf.plantodo.exceptions.ApiSimpleError;
import yeonleaf.plantodo.exceptions.ArgumentValidationException;
import yeonleaf.plantodo.service.CheckboxService;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.methodOn;

@RestController
@RequestMapping("/checkbox")
@RequiredArgsConstructor
public class CheckboxController {

    private final CheckboxService checkboxService;

    @Operation(summary = "Checkbox 등록")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "successful operation", content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = CheckboxResDto.class))),
            @ApiResponse(responseCode = "400", description = "validation errors", content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = ApiBindingError.class))),
            @ApiResponse(responseCode = "404", description = "resource not found", content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = ApiSimpleError.class))),
    })
    @PostMapping
    public ResponseEntity<?> save(@Valid @RequestBody CheckboxReqDto checkboxReqDto, BindingResult bindingResult) {

        if (bindingResult.hasErrors()) {
            throw new ArgumentValidationException("입력값 타입/내용 오류", bindingResult);
        }
        CheckboxResDto checkboxResDto = checkboxService.save(checkboxReqDto);
        EntityModel<CheckboxResDto> entityModel = EntityModel.of(checkboxResDto, linkTo(methodOn(CheckboxController.class).one(checkboxResDto.getId())).withSelfRel());
        return ResponseEntity.status(HttpStatus.CREATED).body(entityModel);

    }

    @GetMapping("/{id}")
    public ResponseEntity<?> one(@PathVariable Long id) {

        CheckboxResDto checkboxResDto = checkboxService.one(id);
        EntityModel<CheckboxResDto> entityModel = EntityModel.of(checkboxResDto, linkTo(methodOn(CheckboxController.class).one(id)).withSelfRel());
        return ResponseEntity.status(HttpStatus.OK).body(entityModel);

    }

}
