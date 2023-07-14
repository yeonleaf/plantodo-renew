package yeonleaf.plantodo.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import yeonleaf.plantodo.dto.GroupReqDto;
import yeonleaf.plantodo.dto.GroupResDto;
import yeonleaf.plantodo.dto.RepInputDto;
import yeonleaf.plantodo.exceptions.ArgumentValidationException;
import yeonleaf.plantodo.service.GroupService;
import yeonleaf.plantodo.validator.RepInputValidator;

@RestController
@RequestMapping("/group")
@RequiredArgsConstructor
public class GroupController {

    private final RepInputValidator repInputValidator = new RepInputValidator();
    private final GroupService groupService;

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

        GroupResDto groupResDto = groupService.save(groupReqDto, repInputDto);

        return ResponseEntity.status(HttpStatus.CREATED).body(groupResDto);
    }

}
