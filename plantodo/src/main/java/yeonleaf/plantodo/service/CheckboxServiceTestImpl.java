package yeonleaf.plantodo.service;

import lombok.RequiredArgsConstructor;
import yeonleaf.plantodo.domain.Checkbox;
import yeonleaf.plantodo.domain.Group;
import yeonleaf.plantodo.domain.Plan;
import yeonleaf.plantodo.dto.CheckboxReqDto;
import yeonleaf.plantodo.dto.CheckboxResDto;
import yeonleaf.plantodo.exceptions.ResourceNotFoundException;
import yeonleaf.plantodo.repository.MemoryCheckboxRepository;
import yeonleaf.plantodo.repository.MemoryGroupRepository;
import yeonleaf.plantodo.repository.MemoryPlanRepository;
import yeonleaf.plantodo.repository.MemoryRepository;

import java.util.List;

@RequiredArgsConstructor
public class CheckboxServiceTestImpl implements CheckboxService {

    private final MemoryPlanRepository planRepository;
    private final MemoryGroupRepository groupRepository;
    private final MemoryCheckboxRepository checkboxRepository;

    @Override
    public CheckboxResDto save(CheckboxReqDto checkboxReqDto) {

        Long planId = checkboxReqDto.getPlanId();
        Plan plan = planRepository.findById(planId).orElseThrow(ResourceNotFoundException::new);

        Group group = findGroupRepOptionZero(planId);
        Checkbox checkbox = checkboxRepository.save(new Checkbox(group, checkboxReqDto.getTitle(), checkboxReqDto.getDate(), false));

        group.addUncheckedCnt(1);
        groupRepository.save(group);

        plan.addUncheckedCnt(1);
        planRepository.save(plan);

        return new CheckboxResDto(checkbox);

    }

    private Group findGroupRepOptionZero(Long planId) {

        List<Group> candidates = groupRepository.findByPlanId(planId).stream().filter(group -> group.getRepetition().getRepOption().equals(0L)).toList();
        if (candidates.size() != 1) {
            throw new ResourceNotFoundException();
        }
        return candidates.get(0);

    }

    @Override
    public CheckboxResDto one(Long id) {
        Checkbox checkbox = checkboxRepository.findById(id).orElseThrow(ResourceNotFoundException::new);
        return new CheckboxResDto(checkbox);
    }
}
