package yeonleaf.plantodo.repository;

import yeonleaf.plantodo.domain.Group;
import yeonleaf.plantodo.domain.Repetition;

import java.util.*;

public class MemoryGroupRepository extends MemoryRepository<Group> {
    private Map<Long, Group> groups = new HashMap<>();
    private Map<Long, Repetition> repetitions = new HashMap<>();
    private Long groupId = 1L;
    private Long repId = 1L;

    @Override
    public Group save(Group group) {
        Long prevGroupId = group.getId();
        Long prevRepeatId = group.getRepetition().getId();
        if (groups.containsKey(prevGroupId)) {
            groups.remove(prevGroupId);
            groups.put(prevGroupId, group);
            if (repetitions.containsKey(prevRepeatId)) {
                repetitions.remove(prevRepeatId);
                repetitions.put(prevRepeatId, group.getRepetition());
            }
        } else {
            group.setId(groupId);
            Repetition repetition = group.getRepetition();
            repetition.setId(repId);
            group.setRepetition(repetition);
            repetitions.put(repId++, repetition);
            groups.put(groupId++, group);
        }
        return group;
    }

    public List<Group> findByPlanId(Long planId) {
        List<Group> res = new ArrayList<>();
        for (Group group : groups.values()) {
            if (group.getPlan().getId().equals(planId)) {
                res.add(group);
            }
        }
        return res;
    }

    @Override
    public Optional<Group> findById(Long id) {
        return Optional.ofNullable(groups.get(id));
    }

    @Override
    public void delete(Group group) {

    }
}
