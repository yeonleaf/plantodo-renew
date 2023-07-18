package yeonleaf.plantodo.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class CheckboxReqDto {

    @NotBlank
    private String title;

    @NotBlank
    private Long planId;

}
