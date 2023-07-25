package yeonleaf.plantodo.domain;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;
import java.util.Objects;

@Entity
@Getter
@Setter
@NoArgsConstructor
public class Checkbox {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "checkbox_id")
    private Long id;

    private String title;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "group_id")
    private Group group;

    private LocalDate date;
    private boolean checked;

    public Checkbox(Group group, String title, LocalDate date, boolean checked) {
        this.group = group;
        this.title = title;
        this.date = date;
        this.checked = checked;
    }

    public void changeChecked() {
        this.checked = !this.isChecked();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Checkbox checkbox = (Checkbox) o;
        return isChecked() == checkbox.isChecked() && Objects.equals(getId(), checkbox.getId()) && Objects.equals(getTitle(), checkbox.getTitle()) && Objects.equals(getGroup(), checkbox.getGroup()) && Objects.equals(getDate(), checkbox.getDate());
    }

    @Override
    public int hashCode() {
        return Objects.hash(getId(), getTitle(), getGroup(), getDate(), isChecked());
    }
}
