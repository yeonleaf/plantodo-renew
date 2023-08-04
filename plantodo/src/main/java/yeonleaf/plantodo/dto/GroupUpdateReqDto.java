package yeonleaf.plantodo.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.validator.constraints.Range;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class GroupUpdateReqDto {

    @NotNull
    @Schema(description = "반복 할 일 ID", example = "1")
    private Long id;

    @NotBlank
    @Schema(example = "updatedGroupTitle")
    private String title;

    @Range(min = 1, max = 3)
    @Schema(description = "1: 매일 반복 | 2: 기간 반복 (예) 2일 | 3: 요일 반복 (예) 월, 수, 금", example = "3", allowableValues = {"1", "2", "3"})
    private int repOption;

    @NotNull
    @Schema(description = "(repOption 조건) if 1: 빈 array | if 2: [\"2\"] | if 3: [\"월\", \"수\", \"금\"]", example = "[\"월\", \"수\", \"금\"]")
    private List<String> repValue;

}
