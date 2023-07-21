package yeonleaf.plantodo.repository;

import yeonleaf.plantodo.domain.Checkbox;

import java.util.*;

public class MemoryCheckboxRepository extends MemoryRepository<Checkbox> {

    public Map<Long, Checkbox> data = new HashMap<>();

    private Long id = 1L;

    @Override
    public Checkbox save(Checkbox checkbox) {
        Long previousId = checkbox.getId();
        if (previousId != null) {
            if (data.containsKey(previousId)) {
                data.remove(previousId);
            }
            data.put(previousId, checkbox);
        } else {
            checkbox.setId(this.id);
            data.put(this.id++, checkbox);
        }
        return checkbox;
    }

    @Override
    public Optional<Checkbox> findById(Long id) {
        return Optional.ofNullable(data.get(id));
    }

    @Override
    public void delete(Checkbox checkbox) {
        data.remove(checkbox.getId());
    }

    public List<Checkbox> findByGroupId(Long groupId) {
        List<Checkbox> res = new ArrayList<>();
        for (Checkbox checkbox : data.values()) {
            if (checkbox.getGroup().getId().equals(groupId)) {
                res.add(checkbox);
            }
        }
        return res;
    }

}
