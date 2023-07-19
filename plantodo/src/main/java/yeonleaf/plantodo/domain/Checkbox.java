package yeonleaf.plantodo.domain;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;

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

}
