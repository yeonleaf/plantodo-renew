package yeonleaf.plantodo.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import yeonleaf.plantodo.domain.Group;
import yeonleaf.plantodo.domain.Repetition;

import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class RepInputDto {

    private int repOption;
    private List<String> repValue = new ArrayList<>();

}
