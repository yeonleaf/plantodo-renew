package yeonleaf.plantodo.wrapper;

import lombok.Getter;
import lombok.NoArgsConstructor;
import yeonleaf.plantodo.dto.PlanResDto;

import java.util.ArrayList;
import java.util.List;

@Getter
@NoArgsConstructor
public class PlanResDtoWrap {

    private List<PlanResDto> wrap = new ArrayList<>();

    public PlanResDtoWrap(List<PlanResDto> planResDtoList) {
        wrap = planResDtoList;
    }

}
