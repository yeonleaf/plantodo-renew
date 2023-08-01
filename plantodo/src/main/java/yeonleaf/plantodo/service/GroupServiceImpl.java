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
import yeonleaf.plantodo.dto.GroupUpdateReqDto;
import yeonleaf.plantodo.dto.RepInputDto;
import yeonleaf.plantodo.exceptions.ResourceNotFoundException;
import yeonleaf.plantodo.repository.*;
import yeonleaf.plantodo.util.CheckboxDateCreator;

import java.time.LocalDate;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class GroupServiceImpl implements GroupService {

    private final PlanRepository planRepository;
    private final GroupRepository groupRepository;
    private final CheckboxRepository checkboxRepository;
    private final CheckboxDslRepository checkboxDslRepository;
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
        groupRepository.save(group);
        planRepository.save(plan);

        return new GroupResDto(group, repInputDto.getRepOption(), repInputDto.getRepValue());

    }

    @Override
    public List<GroupResDto> all(Long planId) {

        planRepository.findById(planId).orElseThrow(ResourceNotFoundException::new);

        return groupRepository.findByPlanId(planId).stream().filter(group -> group.getRepetition().getRepOption() != 0).map(group -> {
            Repetition repetition = group.getRepetition();
            RepInputDto repInputDto = repOutToInConverter.convert(repetition);
            return new GroupResDto(group, repInputDto.getRepOption(), repInputDto.getRepValue());
        }).collect(Collectors.toList());

    }

    @Override
    public GroupResDto one(Long id) {

        Group group = groupRepository.findById(id).orElseThrow(ResourceNotFoundException::new);
        Repetition repetition = group.getRepetition();
        RepInputDto repInputDto = repOutToInConverter.convert(repetition);
        assert repInputDto != null;
        return new GroupResDto(group, repInputDto.getRepOption(), repInputDto.getRepValue());

    }

    @Override
    public GroupResDto update(GroupUpdateReqDto groupUpdateReqDto) {

        Group oldGroup = groupRepository.findById(groupUpdateReqDto.getId()).orElseThrow(ResourceNotFoundException::new);

        String oldTitle = oldGroup.getTitle();
        int oldRepOption = oldGroup.getRepetition().getRepOption();
        String oldRepValue = oldGroup.getRepetition().getRepValue();

        String newTitle = groupUpdateReqDto.getTitle();
        int newRepOption = groupUpdateReqDto.getRepOption();
        List<String> newRepValueList = groupUpdateReqDto.getRepValue();
        String newRepValue = Objects.requireNonNull(repInToOutConverter.convert(new RepInputDto(newRepOption, newRepValueList))).getRepValue();

        boolean hasDifferentTitle = hasDifferentTitle(oldTitle, newTitle);
        boolean hasDifferentRepOption = hasDifferentRepOption(oldRepOption, newRepOption);
        boolean hasDifferentRepValue = hasDifferentRepValue(oldRepValue, newRepValue);

        if (!hasDifferentTitle && !hasDifferentRepOption && !hasDifferentRepValue) {
            return new GroupResDto(oldGroup, oldRepOption, newRepValueList);
        }

        oldGroup.setTitle(newTitle);

        if (!hasDifferentRepOption && !hasDifferentRepValue) {
            Group newGroup = groupRepository.save(oldGroup);
            return new GroupResDto(newGroup, oldRepOption, newRepValueList);
        }

        resetCheckboxes(oldGroup, newRepOption, newRepValueList);

        Repetition oldRepetition = oldGroup.getRepetition();
        oldRepetition.setRepOption(newRepOption);
        oldRepetition.setRepValue(newRepValue);
        oldGroup.setRepetition(oldRepetition);

        Group newGroup = groupRepository.save(oldGroup);

        return new GroupResDto(newGroup, newRepOption, newRepValueList);
    }

    private boolean hasDifferentTitle(String oldTitle, String newTitle) {
        return !oldTitle.equals(newTitle);
    }

    private boolean hasDifferentRepOption(int oldRepOption, int newRepOption) {
        return oldRepOption != newRepOption;
    }

    private boolean hasDifferentRepValue(String oldRepValue, String newRepValue) {
        return !oldRepValue.equals(newRepValue);
    }

    private void resetCheckboxes(Group group, int newRepOption, List<String> newRepValue) {

        checkboxRepository.findByGroupId(group.getId()).forEach(checkboxRepository::delete);
        List<LocalDate> dates = CheckboxDateCreator.create(group.getPlan(), new RepInputDto(newRepOption, newRepValue));
        dates.forEach(date -> checkboxRepository.save(new Checkbox(group, group.getTitle(), date, false)));

    }

    @Override
    public void delete(Long id) {

        Group group = groupRepository.findById(id).orElseThrow(ResourceNotFoundException::new);
        checkboxRepository.findByGroupId(group.getId()).forEach(checkboxRepository::delete);
        groupRepository.delete(group);

    }

    @Override
    public List<GroupResDto> all(Long planId, LocalDate dateKey) {

        return all(planId).stream()
                .filter(groupResDto -> isNotEmptyToday(groupResDto.getId(), dateKey)).toList();

    }

    private boolean isNotEmptyToday(Long groupId, LocalDate dateKey) {
        return checkboxDslRepository.findAllByGroupIdAndDate(groupId, dateKey).size() > 0;
    }

}
