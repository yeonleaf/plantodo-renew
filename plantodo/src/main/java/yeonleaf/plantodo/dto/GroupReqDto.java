package yeonleaf.plantodo.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.*;
import org.hibernate.validator.constraints.Range;

import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class GroupReqDto {

    @NotBlank
    private String title;

    @NotNull
    @Range(min = 1L, max = 3L)
    private Long repOption;

    private List<String> repValue = new ArrayList<>();

}
