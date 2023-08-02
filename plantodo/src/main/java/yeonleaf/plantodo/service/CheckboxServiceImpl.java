package yeonleaf.plantodo.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import yeonleaf.plantodo.domain.Checkbox;
import yeonleaf.plantodo.domain.Group;
import yeonleaf.plantodo.domain.Plan;
import yeonleaf.plantodo.dto.CheckboxReqDto;
import yeonleaf.plantodo.dto.CheckboxResDto;
import yeonleaf.plantodo.dto.CheckboxUpdateReqDto;
import yeonleaf.plantodo.exceptions.ResourceNotFoundException;
import yeonleaf.plantodo.repository.*;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class CheckboxServiceImpl implements CheckboxService {

    private final GroupRepository groupRepository;
    private final CheckboxRepositoryAdapter checkboxRepository;
    private final PlanRepository planRepository;

    @Override
    public CheckboxResDto save(CheckboxReqDto checkboxReqDto) {
        Long planId = checkboxReqDto.getPlanId();
        planRepository.findById(planId).orElseThrow(ResourceNotFoundException::new);

        List<Group> candidates = groupRepository.findByPlanId(planId).stream().filter(group -> group.getRepetition().getRepOption() == 0).toList();
        if (candidates.size() != 1) {
            throw new ResourceNotFoundException();
        }

        Group group = candidates.get(0);
        return new CheckboxResDto(checkboxRepository.save(new Checkbox(group, checkboxReqDto.getTitle(), checkboxReqDto.getDate(),false)));
    }

    @Override
    public CheckboxResDto one(Long id) {
        Checkbox checkbox = checkboxRepository.findById(id).orElseThrow(ResourceNotFoundException::new);
        return new CheckboxResDto(checkbox);
    }

    @Override
    public CheckboxResDto update(CheckboxUpdateReqDto checkboxUpdateReqDto) {

        Checkbox oldCheckbox = checkboxRepository.findById(checkboxUpdateReqDto.getId()).orElseThrow(ResourceNotFoundException::new);
        oldCheckbox.setTitle(checkboxUpdateReqDto.getTitle());
        checkboxRepository.save(oldCheckbox);
        return new CheckboxResDto(oldCheckbox);

    }

    @Override
    public void delete(Long id) {

        Checkbox checkbox = checkboxRepository.findById(id).orElseThrow(ResourceNotFoundException::new);
        checkboxRepository.delete(checkbox);

    }

    @Override
    public CheckboxResDto change(Long id) {

        Checkbox checkbox = checkboxRepository.findById(id).orElseThrow(ResourceNotFoundException::new);
        checkbox.changeChecked();
        return new CheckboxResDto(checkboxRepository.save(checkbox));

    }

    @Override
    public List<CheckboxResDto> allByGroup(Long groupId) {

        groupRepository.findById(groupId).orElseThrow(ResourceNotFoundException::new);

        return checkboxRepository.findByGroupId(groupId).stream().map(CheckboxResDto::new).toList();

    }

    @Override
    public List<CheckboxResDto> allByPlan(Long planId) {

        planRepository.findById(planId).orElseThrow(ResourceNotFoundException::new);

        List<Checkbox> checkboxes = new ArrayList<>();
        groupRepository.findByPlanId(planId).forEach(group -> checkboxes.addAll(checkboxRepository.findByGroupId(group.getId())));
        return checkboxes.stream().map(CheckboxResDto::new).toList();

    }

    @Override
    public List<CheckboxResDto> allByGroup(Long groupId, LocalDate dateKey) {

        groupRepository.findById(groupId).orElseThrow(ResourceNotFoundException::new);
        return checkboxRepository.findAllByGroupIdAndDate(groupId, dateKey).stream().map(CheckboxResDto::new).toList();

    }

    @Override
    public List<CheckboxResDto> allByPlan(Long planId, LocalDate dateKey) {

        planRepository.findById(planId).orElseThrow(ResourceNotFoundException::new);
        return checkboxRepository.findAllByPlanIdAndDate(planId, dateKey).stream().map(CheckboxResDto::new).toList();

    }

    @Override
    public List<CheckboxResDto> allByGroup(Long groupId, LocalDate searchStart, LocalDate searchEnd) {

        groupRepository.findById(groupId).orElseThrow(ResourceNotFoundException::new);
        return checkboxRepository.findAllByGroupIdAndDateRange(groupId, searchStart, searchEnd).stream().map(CheckboxResDto::new).toList();

    }

    @Override
    public List<CheckboxResDto> allByPlan(Long planId, LocalDate searchStart, LocalDate searchEnd) {

        planRepository.findById(planId).orElseThrow(ResourceNotFoundException::new);
        return checkboxRepository.findAllByPlanIdAndDateRange(planId, searchStart, searchEnd).stream().map(CheckboxResDto::new).toList();

    }

}
