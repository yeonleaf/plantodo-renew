package yeonleaf.plantodo.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@AllArgsConstructor
@NoArgsConstructor
public class MemberReqDto {
    @NotBlank(message = "이메일은 Null 이거나 공백이거나 빈 문자열일 수 없습니다.")
    private String email;

    @NotBlank(message = "비밀번호는 Null 이거나 공백이거나 빈 문자열일 수 없습니다.")
    private String password;
}
