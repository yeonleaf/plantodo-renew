package yeonleaf.plantodo.controller;

import io.jsonwebtoken.JwtBuilder;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.MethodParameter;
import org.springframework.hateoas.EntityModel;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.*;
import yeonleaf.plantodo.domain.Member;
import yeonleaf.plantodo.dto.JwtTokenDto;
import yeonleaf.plantodo.dto.MemberReqDto;
import yeonleaf.plantodo.dto.MemberResDto;
import yeonleaf.plantodo.exceptions.ArgumentValidationException;
import yeonleaf.plantodo.exceptions.ResourceNotFoundException;
import yeonleaf.plantodo.service.MemberService;
import yeonleaf.plantodo.service.MemberServiceImpl;
import yeonleaf.plantodo.validator.JoinFormatCheckValidator;

@RestController
@RequestMapping("/member")
@RequiredArgsConstructor
public class MemberController {
    private final MemberService memberService;
    private final JoinFormatCheckValidator joinFormatCheckValidator = new JoinFormatCheckValidator();

    private final JwtBuilder jwtBuilder;

    @PostMapping
    @Operation(description = "회원가입")
    public ResponseEntity<?> save(@Valid @RequestBody MemberReqDto memberReqDto, BindingResult bindingResult) {
        if (bindingResult.hasErrors()) {
            throw new ArgumentValidationException("입력값 타입/내용 오류", bindingResult);
        }
        joinFormatCheckValidator.validate(memberReqDto, bindingResult);
        if (bindingResult.hasErrors()) {
            throw new ArgumentValidationException("입력값 형식 오류", bindingResult);
        }
        Member member = memberService.save(memberReqDto);
        MemberResDto memberResDto = new MemberResDto(member);
        return ResponseEntity.status(HttpStatus.CREATED).body(EntityModel.of(memberResDto));
    }

    @PostMapping("/login")
    @Operation(description = "로그인")
    public ResponseEntity<?> login(@Valid @RequestBody MemberReqDto memberReqDto, BindingResult bindingResult) {
        if (bindingResult.hasErrors()) {
            throw new ArgumentValidationException("입력값 타입/내용 오류", bindingResult);
        }
        boolean canLogin = memberService.login(memberReqDto);
        if (canLogin) {
            String jwtKey = buildKey(memberReqDto.getEmail());
            JwtTokenDto token = new JwtTokenDto(jwtKey);
            return ResponseEntity.status(HttpStatus.OK).body(EntityModel.of(token));
        } else {
            throw new ResourceNotFoundException();
        }
    }

    private String buildKey(String value) {
        return jwtBuilder.claim("email", value)
                .compact();
    }
}
