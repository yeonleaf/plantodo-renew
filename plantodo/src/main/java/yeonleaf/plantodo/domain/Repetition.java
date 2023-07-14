package yeonleaf.plantodo.domain;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Repetition {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "rep_id")
    private Long id;

    private Long repOption;
    private String repValue;

    public Repetition(Long repOption, String repValue) {
        this.repOption = repOption;
        this.repValue = repValue;
    }

}
