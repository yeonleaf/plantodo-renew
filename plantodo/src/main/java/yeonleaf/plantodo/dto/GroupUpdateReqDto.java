package yeonleaf.plantodo.dto;

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
    private Long id;

    @NotBlank
    private String title;

    @Range(min = 1, max = 3)
    private int repOption;

    @NotNull
    private List<String> repValue;

}
