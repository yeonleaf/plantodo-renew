package yeonleaf.plantodo.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.hateoas.EntityModel;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import yeonleaf.plantodo.domain.Member;
import yeonleaf.plantodo.dto.JwtTokenDto;
import yeonleaf.plantodo.dto.MemberReqDto;
import yeonleaf.plantodo.dto.MemberResDto;
import yeonleaf.plantodo.exceptions.ApiBindingError;
import yeonleaf.plantodo.exceptions.ApiSimpleError;
import yeonleaf.plantodo.exceptions.ArgumentValidationException;
import yeonleaf.plantodo.exceptions.ResourceNotFoundException;
import yeonleaf.plantodo.provider.JwtBasicProvider;
import yeonleaf.plantodo.provider.JwtProvider;
import yeonleaf.plantodo.service.MemberService;
import yeonleaf.plantodo.validator.JoinFormatCheckValidator;
import java.util.Optional;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.methodOn;

@Tag(name = "member", description = "사용자 API")
@RestController
@RequestMapping("/member")
@RequiredArgsConstructor
public class MemberController {

    private final MemberService memberService;
    private final JoinFormatCheckValidator joinFormatCheckValidator = new JoinFormatCheckValidator();
    private final JwtBasicProvider jwtProvider;

    @Operation(summary = "회원 등록", description = "이메일과 비밀번호를 입력해 회원을 등록합니다.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "successful operation", content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = MemberResDto.class))),
            @ApiResponse(responseCode = "400", description = "validation errors", content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = ApiBindingError.class))),
            @ApiResponse(responseCode = "409", description = "duplicated member", content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = ApiSimpleError.class))),
    })
    @PostMapping(produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<?> save(@Valid @RequestBody MemberReqDto memberReqDto, BindingResult bindingResult) {

        if (bindingResult.hasErrors()) {
            throw new ArgumentValidationException("입력값 타입/내용 오류", bindingResult);
        }

        joinFormatCheckValidator.validate(memberReqDto, bindingResult);
        if (bindingResult.hasErrors()) {
            throw new ArgumentValidationException("입력값 형식 오류", bindingResult);
        }

        MemberResDto memberResDto = memberService.save(memberReqDto);
        return ResponseEntity.status(HttpStatus.CREATED).body(memberResDto);

    }

    @Operation(summary = "로그인", description = "로그인을 통해 jwt 토큰을 발급받습니다.\n" +
            "회원가입, 로그인 외의 모든 API 요청을 할 때는 Bearer [토큰] 형식의 Authorization 헤더가 있어야 합니다.\n" +
            "Swagger UI에서 테스트할 경우 우측 상단에 있는 Authorize 버튼을 클릭해 발급받은 토큰을 입력해 주시기 바랍니다.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "successful operation", content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = JwtTokenDto.class))),
            @ApiResponse(responseCode = "400", description = "validation errors", content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = ApiBindingError.class))),
            @ApiResponse(responseCode = "404", description = "resource not found", content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = ApiSimpleError.class))),
    })
    @PostMapping("/login")
    public ResponseEntity<?> login(@Valid @RequestBody MemberReqDto memberReqDto, BindingResult bindingResult) {

        if (bindingResult.hasErrors()) {
            throw new ArgumentValidationException("입력값 타입/내용 오류", bindingResult);
        }

        Long memberId = memberService.login(memberReqDto);
        JwtTokenDto token = new JwtTokenDto(jwtProvider.generateToken(memberId));
        EntityModel<JwtTokenDto> entityModel = EntityModel.of(token, linkTo(methodOn(PlanController.class).all(memberId)).withRel("plans"));
        return ResponseEntity.status(HttpStatus.OK).body(entityModel);

    }

}
