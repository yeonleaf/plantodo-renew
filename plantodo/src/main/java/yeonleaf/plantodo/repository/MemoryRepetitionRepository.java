package yeonleaf.plantodo.repository;

import yeonleaf.plantodo.domain.Plan;
import yeonleaf.plantodo.domain.Repetition;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

public class MemoryRepetitionRepository extends MemoryRepository<Repetition> {

    private Map<Long, Repetition> data = new HashMap<>();
    private Long id = 1L;

    @Override
    public Repetition save(Repetition repetition) {
        Long prevId = repetition.getId();
        if (prevId != null && data.containsKey(prevId)) {
            data.remove(prevId);
            data.put(prevId, repetition);
        } else {
            repetition.setId(id);
            data.put(id++, repetition);
        }
        return repetition;
    }

    @Override
    public Optional<Repetition> findById(Long id) {
        return Optional.empty();
    }

    @Override
    public void delete(Repetition repetition) {

    }
}
