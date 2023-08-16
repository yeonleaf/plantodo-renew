package yeonleaf.plantodo.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;
import java.util.LinkedHashMap;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
public class CalendarResDto {
    private LinkedHashMap<LocalDate, LinkedHashMap<PlanResDto, List<CheckboxResDto>>> result;
}
