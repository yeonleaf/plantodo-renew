package yeonleaf.plantodo.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import yeonleaf.plantodo.domain.Checkbox;
import yeonleaf.plantodo.domain.Group;
import yeonleaf.plantodo.domain.Plan;
import yeonleaf.plantodo.dto.CheckboxReqDto;
import yeonleaf.plantodo.dto.CheckboxResDto;
import yeonleaf.plantodo.exceptions.ResourceNotFoundException;
import yeonleaf.plantodo.repository.CheckboxRepository;
import yeonleaf.plantodo.repository.GroupRepository;
import yeonleaf.plantodo.repository.PlanRepository;

import java.util.List;

@Service
@RequiredArgsConstructor
public class CheckboxServiceImpl implements CheckboxService {

    private final GroupRepository groupRepository;
    private final CheckboxRepository checkboxRepository;
    private final PlanRepository planRepository;

    @Override
    public CheckboxResDto save(CheckboxReqDto checkboxReqDto) {
        Long planId = checkboxReqDto.getPlanId();
        planRepository.findById(planId).orElseThrow(ResourceNotFoundException::new);

        List<Group> candidates = groupRepository.findByPlanId(planId).stream().filter(group -> group.getRepetition().getRepOption().equals(0L)).toList();
        if (candidates.size() != 1) {
            throw new ResourceNotFoundException();
        }

        Group group = candidates.get(0);
        return new CheckboxResDto(checkboxRepository.save(new Checkbox(group, checkboxReqDto.getTitle(), checkboxReqDto.getDate(),false)));
    }
}
