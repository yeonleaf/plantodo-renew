package yeonleaf.plantodo.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import yeonleaf.plantodo.domain.Group;
import yeonleaf.plantodo.domain.Member;
import yeonleaf.plantodo.domain.Plan;
import yeonleaf.plantodo.dto.PlanReqDto;
import yeonleaf.plantodo.dto.PlanResDto;
import yeonleaf.plantodo.repository.GroupRepository;
import yeonleaf.plantodo.repository.PlanRepository;

@Service
@RequiredArgsConstructor
public class PlanServiceImpl implements PlanService {

    private final PlanRepository planRepository;
    private final GroupRepository groupRepository;

    @Override
    public PlanResDto save(Member member, PlanReqDto planReqDto) {
        Plan plan = planRepository.save(new Plan(planReqDto, member));
        groupRepository.save(new Group(plan, "DailyGroup"));
        return new PlanResDto(plan);
    }

}
