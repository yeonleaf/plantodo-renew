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
    private int repOption;
    private List<String> repValue = new ArrayList<>();

    public GroupResDto(Group group, int repOption, List<String> repValue) {
        this.id = group.getId();
        this.title = group.getTitle();
        this.repOption = repOption;
        this.repValue = repValue;
    }

    public GroupResDto(Group group, RepInputDto repInputDto) {
        this.id = group.getId();
        this.title = group.getTitle();
        this.repOption = repInputDto.getRepOption();
        this.repValue = repInputDto.getRepValue();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        GroupResDto that = (GroupResDto) o;
        return getRepOption() == that.getRepOption() && Objects.equals(getId(), that.getId()) && Objects.equals(getTitle(), that.getTitle()) && Objects.equals(getRepValue(), that.getRepValue());
    }

    @Override
    public int hashCode() {
        return Objects.hash(getId(), getTitle(), getRepOption(), getRepValue());
    }

}

