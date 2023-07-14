package yeonleaf.plantodo.domain;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.datatype.jsr310.deser.LocalDateDeserializer;
import com.fasterxml.jackson.datatype.jsr310.ser.LocalDateSerializer;
import jakarta.persistence.*;
import lombok.*;
import yeonleaf.plantodo.dto.PlanReqDto;

import java.time.LocalDate;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Plan {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String title;

    @JsonSerialize(using = LocalDateSerializer.class)
    @JsonDeserialize(using = LocalDateDeserializer.class)
    private LocalDate start;

    @JsonSerialize(using = LocalDateSerializer.class)
    @JsonDeserialize(using = LocalDateDeserializer.class)
    private LocalDate end;

    @ManyToOne
    @JoinColumn(name = "member_id")
    private Member member;

    public Plan(PlanReqDto planReqDto, Member member) {
        this.title = planReqDto.getTitle();
        this.start = planReqDto.getStart();
        this.end = planReqDto.getEnd();
        this.member = member;
    }

    public Plan(String title, LocalDate start, LocalDate end, Member member) {
        this.title = title;
        this.start = start;
        this.end = end;
        this.member = member;
    }

}
