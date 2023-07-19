package yeonleaf.plantodo.service;

import lombok.RequiredArgsConstructor;
import yeonleaf.plantodo.domain.Group;
import yeonleaf.plantodo.domain.Member;
import yeonleaf.plantodo.domain.Plan;
import yeonleaf.plantodo.dto.GroupReqDto;
import yeonleaf.plantodo.dto.PlanReqDto;
import yeonleaf.plantodo.dto.PlanResDto;
import yeonleaf.plantodo.dto.RepInputDto;
import yeonleaf.plantodo.exceptions.ResourceNotFoundException;
import yeonleaf.plantodo.repository.MemoryGroupRepository;
import yeonleaf.plantodo.repository.MemoryPlanRepository;
import yeonleaf.plantodo.repository.MemoryRepository;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@RequiredArgsConstructor
public class PlanServiceTestImpl implements PlanService {

    private final MemoryRepository<Member> memberRepository;
    private final MemoryRepository<Plan> planRepository;
    private final MemoryGroupRepository groupRepository;

    @Override
    public PlanResDto save(PlanReqDto planReqDto) {

        Member member = memberRepository.findById(planReqDto.getMemberId()).orElseThrow(ResourceNotFoundException::new);
        Plan plan = planRepository.save(new Plan(planReqDto, member));
        groupRepository.save(new Group(plan, "repOptionZeroGroup"));
        return new PlanResDto(plan);

    }

    @Override
    public PlanResDto one(Long id) {

        Optional<Plan> candidate = planRepository.findById(id);
        if (candidate.isPresent()) {
            return new PlanResDto(candidate.get());
        } else {
            throw new ResourceNotFoundException();
        }

    }

    @Override
    public PlanResDto update(Long id, PlanReqDto planReqDto) {

        Optional<Plan> candidate = planRepository.findById(id);
        if (candidate.isPresent()) {
            Plan beforeUpdate = candidate.get();
            beforeUpdate.setTitle(planReqDto.getTitle());
            beforeUpdate.setStart(planReqDto.getStart());
            beforeUpdate.setEnd(planReqDto.getEnd());
            return new PlanResDto(planRepository.save(beforeUpdate));
        } else {
            throw new ResourceNotFoundException();
        }

    }

    @Override
    public void delete(Long id) {

        Optional<Plan> candidate = planRepository.findById(id);
        if (candidate.isPresent()) {
            planRepository.delete(candidate.get());
        } else {
            throw new ResourceNotFoundException();
        }

    }

    @Override
    public List<PlanResDto> all(Long memberId) {
        return null;
    }
}
