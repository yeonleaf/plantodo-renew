package yeonleaf.plantodo.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import yeonleaf.plantodo.domain.Checkbox;

import java.util.Objects;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class CheckboxResDto {

    private Long id;
    private String title;
    private boolean checked;

    public CheckboxResDto(Checkbox checkbox) {
        this.id = checkbox.getId();
        this.title = checkbox.getTitle();
        this.checked = checkbox.isChecked();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        CheckboxResDto that = (CheckboxResDto) o;
        return isChecked() == that.isChecked() && Objects.equals(getId(), that.getId()) && Objects.equals(getTitle(), that.getTitle());
    }

    @Override
    public int hashCode() {
        return Objects.hash(getId(), getTitle(), isChecked());
    }
}
