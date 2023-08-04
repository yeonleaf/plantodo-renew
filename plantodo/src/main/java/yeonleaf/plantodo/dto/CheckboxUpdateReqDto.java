package yeonleaf.plantodo.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class CheckboxUpdateReqDto {

    @NotNull
    @Schema(description = "할 일 ID", example = "1")
    private Long id;

    @NotBlank
    @Schema(example = "updatedCheckboxTitle")
    private String title;

}
