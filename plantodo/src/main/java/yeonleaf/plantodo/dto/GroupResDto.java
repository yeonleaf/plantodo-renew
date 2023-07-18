package yeonleaf.plantodo.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import yeonleaf.plantodo.domain.Group;

import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class GroupResDto {

    private Long id;
    private String title;
    private int checkedCnt;
    private int uncheckedCnt;
    private Long repOption;
    private List<String> repValue = new ArrayList<>();

    public GroupResDto(Group group, Long repOption, List<String> repValue) {
        this.id = group.getId();
        this.title = group.getTitle();
        this.checkedCnt = group.getCheckedCnt();
        this.uncheckedCnt = group.getUncheckedCnt();
        this.repOption = repOption;
        this.repValue = repValue;
    }

}

