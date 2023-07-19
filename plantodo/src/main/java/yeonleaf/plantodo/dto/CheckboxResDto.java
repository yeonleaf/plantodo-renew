package yeonleaf.plantodo.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import yeonleaf.plantodo.domain.Checkbox;

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

}
