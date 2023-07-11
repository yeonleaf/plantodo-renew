package yeonleaf.plantodo.domain;

import jakarta.persistence.*;

@Entity
public class Plan {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "member_id")
    private Member member;
}
