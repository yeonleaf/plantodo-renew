package yeonleaf.plantodo.dto;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.datatype.jsr310.deser.LocalDateDeserializer;
import com.fasterxml.jackson.datatype.jsr310.ser.LocalDateSerializer;
import lombok.*;
import yeonleaf.plantodo.domain.Plan;
import yeonleaf.plantodo.domain.PlanStatus;

import java.time.LocalDate;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class PlanResDto {

    private Long id;
    private String title;

    @JsonSerialize(using = LocalDateSerializer.class)
    @JsonDeserialize(using = LocalDateDeserializer.class)
    private LocalDate start;

    @JsonSerialize(using = LocalDateSerializer.class)
    @JsonDeserialize(using = LocalDateDeserializer.class)
    private LocalDate end;

    private PlanStatus status;
    private int uncheckedCnt;
    private int checkedCnt;

    public PlanResDto(Plan plan) {
        this.id = plan.getId();
        this.title = plan.getTitle();
        this.start = plan.getStart();
        this.end = plan.getEnd();
        this.status = plan.getStatus();
        this.uncheckedCnt = plan.getUncheckedCnt();
        this.checkedCnt = plan.getCheckedCnt();
    }

    public PlanResDto(Long id, String title, LocalDate start, LocalDate end, PlanStatus status) {
        this.id = id;
        this.title = title;
        this.start = start;
        this.end = end;
        this.status = status;
        this.uncheckedCnt = 0;
        this.checkedCnt = 0;
    }

}
