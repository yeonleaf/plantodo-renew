package yeonleaf.plantodo.domain;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.datatype.jsr310.deser.LocalDateDeserializer;
import com.fasterxml.jackson.datatype.jsr310.ser.LocalDateSerializer;
import jakarta.persistence.*;
import lombok.*;
import yeonleaf.plantodo.dto.PlanReqDto;

import java.time.LocalDate;
import java.util.Objects;

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

    private PlanStatus status;

    public Plan(PlanReqDto planReqDto, Member member) {
        this.title = planReqDto.getTitle();
        this.start = planReqDto.getStart();
        this.end = planReqDto.getEnd();
        this.member = member;
        this.status = PlanStatus.NOW;
    }

    public Plan(String title, LocalDate start, LocalDate end, Member member) {
        this.title = title;
        this.start = start;
        this.end = end;
        this.member = member;
        this.status = PlanStatus.NOW;
    }

    public Plan(Long id, String title, LocalDate start, LocalDate end, Member member) {
        this.id = id;
        this.title = title;
        this.start = start;
        this.end = end;
        this.member = member;
        this.status = PlanStatus.NOW;
    }

    public Plan(String title, LocalDate start, LocalDate end, Member member, PlanStatus status) {
        this.title = title;
        this.start = start;
        this.end = end;
        this.member = member;
        this.status = status;
    }

    public void changeStatus() {

        if (this.status.equals(PlanStatus.NOW)) {
            this.status = PlanStatus.COMPLETED;
        } else if (this.status.equals(PlanStatus.COMPLETED)) {
            this.status = PlanStatus.NOW;
        }

    }

    public void changeToPast() {
        this.status = PlanStatus.PAST;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Plan plan = (Plan) o;
        return Objects.equals(getId(), plan.getId()) && Objects.equals(getTitle(), plan.getTitle()) && Objects.equals(getStart(), plan.getStart()) && Objects.equals(getEnd(), plan.getEnd()) && Objects.equals(getMember(), plan.getMember());
    }

    @Override
    public int hashCode() {
        return Objects.hash(getId(), getTitle(), getStart(), getEnd(), getMember());
    }

}
