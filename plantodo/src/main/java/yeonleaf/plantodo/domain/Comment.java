package yeonleaf.plantodo.domain;

import jakarta.persistence.*;

@Entity
public class Comment {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "checkbox_id")
    private Checkbox checkbox;
}
