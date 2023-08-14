package yeonleaf.plantodo.repository;

import lombok.RequiredArgsConstructor;
import yeonleaf.plantodo.domain.Group;
import yeonleaf.plantodo.domain.Repetition;

import java.util.*;

@RequiredArgsConstructor
public class MemoryGroupRepository extends MemoryRepository<Group> {

    private final MemoryRepetitionRepository repetitionRepository;

    private Map<Long, Group> data = new HashMap<>();
    private Long id = 1L;

    @Override
    public Group save(Group group) {
        Long prevGroupId = group.getId();
        if (data.containsKey(prevGroupId)) {
            data.remove(prevGroupId);
            Repetition repetition = repetitionRepository.save(group.getRepetition());
            group.setRepetition(repetition);
            data.put(prevGroupId, group);
        } else {
            group.setId(id);
            Repetition repetition = repetitionRepository.save(group.getRepetition());
            group.setRepetition(repetition);
            data.put(id++, group);
        }
        return group;
    }

    public List<Group> findByPlanId(Long planId) {
        List<Group> res = new ArrayList<>();
        for (Group group : data.values()) {
            if (group.getPlan().getId().equals(planId)) {
                res.add(group);
            }
        }
        return res;
    }

    @Override
    public Optional<Group> findById(Long id) {
        return Optional.ofNullable(data.get(id));
    }

    @Override
    public void delete(Group group) {
        data.remove(group.getId());
    }

    @Override
    public void clear() {
        data.clear();
    }

}
