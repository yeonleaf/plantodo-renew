package yeonleaf.plantodo.repository;

import yeonleaf.plantodo.domain.Group;
import yeonleaf.plantodo.domain.Repetition;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

public class MemoryGroupRepository extends MemoryRepository<Group> {
    private Map<Long, Group> groups = new HashMap<>();
    private Map<Long, Repetition> repetitions = new HashMap<>();
    private Long groupId = 1L;
    private Long repId = 1L;

    @Override
    public Group save(Group group) {
        group.setId(groupId);
        Repetition repetition = group.getRepetition();
        repetition.setId(repId);
        group.setRepetition(repetition);
        repetitions.put(repId++, repetition);
        groups.put(groupId++, group);
        return group;
    }

    public Optional<Group> findByPlanId(Long planId) {
        for (Group group : groups.values()) {
            if (group.getPlan().getId().equals(planId)) {
                return Optional.of(groups.get(group.getId()));
            }
        }
        return Optional.empty();
    }
}
