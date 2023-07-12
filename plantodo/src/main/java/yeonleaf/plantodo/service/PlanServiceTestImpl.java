package yeonleaf.plantodo.service;

import lombok.RequiredArgsConstructor;
import yeonleaf.plantodo.domain.Member;
import yeonleaf.plantodo.domain.Plan;
import yeonleaf.plantodo.dto.PlanReqDto;
import yeonleaf.plantodo.dto.PlanResDto;
import yeonleaf.plantodo.repository.MemoryPlanRepository;

@RequiredArgsConstructor
public class PlanServiceTestImpl implements PlanService {

    private final MemoryPlanRepository memoryPlanRepository;

    @Override
    public PlanResDto save(Member member, PlanReqDto planReqDto) {
        Plan plan = new Plan(planReqDto, member);
        return new PlanResDto(memoryPlanRepository.save(plan));
    }

}
