package yeonleaf.plantodo.domain;

import jakarta.persistence.*;

@Entity
public class Checkbox {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "checkbox_id")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "group_id")
    private Group group;

}
