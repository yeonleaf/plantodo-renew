package yeonleaf.plantodo.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class GroupResDto {

    private Long id;
    private String title;
    private Long repOption;
    private List<String> repValue = new ArrayList<>();

}

