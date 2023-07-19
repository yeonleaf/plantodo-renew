package yeonleaf.plantodo.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import yeonleaf.plantodo.converter.RepInToOutConverter;
import yeonleaf.plantodo.converter.RepOutToInConverter;
import yeonleaf.plantodo.domain.Checkbox;
import yeonleaf.plantodo.domain.Group;
import yeonleaf.plantodo.domain.Plan;
import yeonleaf.plantodo.domain.Repetition;
import yeonleaf.plantodo.dto.GroupReqDto;
import yeonleaf.plantodo.dto.GroupResDto;
import yeonleaf.plantodo.dto.RepInputDto;
import yeonleaf.plantodo.exceptions.ResourceNotFoundException;
import yeonleaf.plantodo.repository.*;
import yeonleaf.plantodo.util.CheckboxDateCreator;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class GroupServiceImpl implements GroupService {

    private final PlanRepository planRepository;
    private final GroupRepository groupRepository;
    private final CheckboxRepository checkboxRepository;
    private final RepInToOutConverter repInToOutConverter;
    private final RepOutToInConverter repOutToInConverter;

    @Override
    public GroupResDto save(GroupReqDto groupReqDto) {

        Optional<Plan> candidatePlan = planRepository.findById(groupReqDto.getPlanId());
        if (candidatePlan.isEmpty()) {
            throw new ResourceNotFoundException();
        }

        Plan plan = candidatePlan.get();

        RepInputDto repInputDto = new RepInputDto(groupReqDto.getRepOption(), groupReqDto.getRepValue());
        Repetition repetition = repInToOutConverter.convert(repInputDto);
        Group group = groupRepository.save(new Group(plan, groupReqDto.getTitle(), repetition));

        List<LocalDate> dates = CheckboxDateCreator.create(plan, repInputDto);
        dates.forEach(date -> {
            checkboxRepository.save(new Checkbox(group, group.getTitle(), date, false));
        });
        group.setUncheckedCnt(dates.size());
        groupRepository.save(group);

        plan.addUncheckedCnt(dates.size());
        planRepository.save(plan);

        return new GroupResDto(group, repInputDto.getRepOption(), repInputDto.getRepValue());

    }

    @Override
    public List<GroupResDto> findAllByPlanId(Long planId) {
        return null;
    }

    @Override
    public GroupResDto one(Long id) {

        Group group = groupRepository.findById(id).orElseThrow(ResourceNotFoundException::new);
        Repetition repetition = group.getRepetition();
        RepInputDto repInputDto = repOutToInConverter.convert(repetition);
        assert repInputDto != null;
        return new GroupResDto(group, repInputDto.getRepOption(), repInputDto.getRepValue());

    }
}
