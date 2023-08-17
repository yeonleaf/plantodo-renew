package yeonleaf.plantodo.domain;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "group_table")
@Getter
@Setter
@NoArgsConstructor
public class Group {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "plan_id")
    private Plan plan;

    private String title;

    @OneToOne(cascade = CascadeType.ALL)
    @JoinColumn(name = "rep_id")
    private Repetition repetition;

    public Group(Plan plan, String title, Repetition repetition) {
        this.plan = plan;
        this.title = title;
        this.repetition = repetition;
    }

    public Group(Plan plan, String title) {
        this.plan = plan;
        this.title = title;
        this.repetition = new Repetition(0, "00000000");
    }

}
