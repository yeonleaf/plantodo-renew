package yeonleaf.plantodo.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import yeonleaf.plantodo.domain.Group;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class GroupResDto {

    private Long id;
    private String title;
    private int checkedCnt;
    private int uncheckedCnt;
    private int repOption;
    private List<String> repValue = new ArrayList<>();

    public GroupResDto(Group group, int repOption, List<String> repValue) {
        this.id = group.getId();
        this.title = group.getTitle();
        this.checkedCnt = group.getCheckedCnt();
        this.uncheckedCnt = group.getUncheckedCnt();
        this.repOption = repOption;
        this.repValue = repValue;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        GroupResDto that = (GroupResDto) o;
        return getCheckedCnt() == that.getCheckedCnt() && getUncheckedCnt() == that.getUncheckedCnt() && Objects.equals(getId(), that.getId()) && Objects.equals(getTitle(), that.getTitle()) && Objects.equals(getRepOption(), that.getRepOption()) && Objects.equals(getRepValue(), that.getRepValue());
    }

    @Override
    public int hashCode() {
        return Objects.hash(getId(), getTitle(), getCheckedCnt(), getUncheckedCnt(), getRepOption(), getRepValue());
    }
}

