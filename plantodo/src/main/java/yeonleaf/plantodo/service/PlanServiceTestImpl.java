package yeonleaf.plantodo.service;

import lombok.RequiredArgsConstructor;
import yeonleaf.plantodo.domain.Group;
import yeonleaf.plantodo.domain.Member;
import yeonleaf.plantodo.domain.Plan;
import yeonleaf.plantodo.dto.PlanReqDto;
import yeonleaf.plantodo.dto.PlanResDto;
import yeonleaf.plantodo.repository.MemoryGroupRepository;
import yeonleaf.plantodo.repository.MemoryPlanRepository;
import yeonleaf.plantodo.repository.MemoryRepository;

@RequiredArgsConstructor
public class PlanServiceTestImpl implements PlanService {

    private final MemoryRepository<Plan> memoryPlanRepository;
    private final MemoryRepository<Group> memoryGroupRepository;

    @Override
    public PlanResDto save(Member member, PlanReqDto planReqDto) {
        Plan plan = memoryPlanRepository.save(new Plan(planReqDto, member));
        memoryGroupRepository.save(new Group(plan, "DailyGroup"));
        return new PlanResDto(plan);
    }

}
