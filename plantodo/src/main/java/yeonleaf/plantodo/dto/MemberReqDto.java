package yeonleaf.plantodo.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@AllArgsConstructor
@NoArgsConstructor
public class MemberReqDto {

    @NotBlank(message = "email should be not null, not blank, not empty string")
    @Schema(example = "test@abc.co.kr")
    private String email;

    @NotBlank(message = "password should be not null, not blank, not empty string")
    @Schema(example = "dw43%5e@")
    private String password;

}
