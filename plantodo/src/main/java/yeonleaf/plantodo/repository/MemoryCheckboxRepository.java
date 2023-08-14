package yeonleaf.plantodo.repository;

import yeonleaf.plantodo.domain.Checkbox;

import java.time.LocalDate;
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

    public List<Checkbox> findAllByGroupIdAndDate(Long groupId, LocalDate dateKey) {

        List<Checkbox> res = new ArrayList<>();
        for (Checkbox checkbox : data.values()) {
            if (checkbox.getGroup().getId().equals(groupId) && checkbox.getDate().equals(dateKey)) {
                res.add(checkbox);
            }
        }
        return res;
        
    }

    public List<Checkbox> findAllByGroupIdAndDateRange(Long groupId, LocalDate searchStart, LocalDate searchEnd) {

        List<Checkbox> res = new ArrayList<>();
        for (Checkbox checkbox : data.values()) {
            if (checkbox.getGroup().getId().equals(groupId) && inRange(checkbox.getDate(), searchStart, searchEnd)) {
                res.add(checkbox);
            }
        }
        return res;

    }

    public List<Checkbox> findAllByPlanIdAndDate(Long planId, LocalDate dateKey) {

        List<Checkbox> res = new ArrayList<>();
        for (Checkbox checkbox : data.values()) {
            if (checkbox.getGroup().getPlan().getId().equals(planId) && checkbox.getDate().equals(dateKey)) {
                res.add(checkbox);
            }
        }
        return res;

    }

    public List<Checkbox> findAllByPlanIdAndDateRange(Long planId, LocalDate searchStart, LocalDate searchEnd) {

        List<Checkbox> res = new ArrayList<>();
        for (Checkbox checkbox : data.values()) {
            if (checkbox.getGroup().getPlan().getId().equals(planId) && inRange(checkbox.getDate(), searchStart, searchEnd)) {
                res.add(checkbox);
            }
        }
        return res;

    }

    private boolean inRange(LocalDate a, LocalDate b, LocalDate c) {
        return !a.isBefore(b) && !a.isAfter(c);
    }

    @Override
    public void clear() {
        data.clear();
    }

}
