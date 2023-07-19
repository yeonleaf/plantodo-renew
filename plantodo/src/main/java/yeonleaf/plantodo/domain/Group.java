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
    @Column(name = "group_id")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "plan_id")
    private Plan plan;

    private String title;
    private int checkedCnt;
    private int uncheckedCnt;

    @OneToOne(cascade = CascadeType.ALL)
    @JoinColumn(name = "rep_id")
    private Repetition repetition;

    public Group(Plan plan, String title, Repetition repetition) {
        this.plan = plan;
        this.title = title;
        this.repetition = repetition;
        this.checkedCnt = 0;
        this.uncheckedCnt = 0;
    }

    public Group(Plan plan, String title) {
        this.plan = plan;
        this.title = title;
        this.checkedCnt = 0;
        this.uncheckedCnt = 0;
        this.repetition = new Repetition(0L, "00000000");
    }

    public void addUncheckedCnt(int uncheckedCnt) {
        this.uncheckedCnt += uncheckedCnt;
    }

    public void addCheckedCnt(int checkedCnt) {
        this.checkedCnt += checkedCnt;
    }

}
