package yeonleaf.plantodo.service;

import org.springframework.stereotype.Service;
import yeonleaf.plantodo.domain.Member;
import yeonleaf.plantodo.domain.Plan;
import yeonleaf.plantodo.dto.PlanReqDto;
import yeonleaf.plantodo.dto.PlanResDto;

import java.util.List;
import java.util.Optional;

public interface PlanService {

    PlanResDto save(Member member, PlanReqDto planReqDto);
    PlanResDto one(Long id);

    PlanResDto update(Long id, PlanReqDto planReqDto);

    void delete(Long id);

    List<PlanResDto> all(Long memberId);

}
