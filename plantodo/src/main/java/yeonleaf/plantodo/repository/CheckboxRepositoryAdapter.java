package yeonleaf.plantodo.repository;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import yeonleaf.plantodo.domain.Checkbox;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Component
@RequiredArgsConstructor
public class CheckboxRepositoryAdapter {

    private final CheckboxDslRepository checkboxDslRepository;
    private final CheckboxRepository checkboxRepository;

    public Checkbox save(Checkbox checkbox) {
        return checkboxRepository.save(checkbox);
    }

    public Optional<Checkbox> findById(Long id) {
        return checkboxRepository.findById(id);
    }

    public void delete(Checkbox checkbox) {
        checkboxRepository.delete(checkbox);
    }

    public List<Checkbox> findByGroupId(Long groupId) {
        return checkboxRepository.findByGroupId(groupId);
    }

    public List<Checkbox> findAllByGroupIdAndDate(Long groupId, LocalDate dateKey) {
        return checkboxDslRepository.findAllByGroupIdAndDate(groupId, dateKey);
    }

    public List<Checkbox> findAllByGroupIdAndDateRange(Long groupId, LocalDate searchStart, LocalDate searchEnd) {
        return checkboxDslRepository.findAllByGroupIdAndDateRange(groupId, searchStart, searchEnd);
    }

    public List<Checkbox> findAllByPlanIdAndDate(Long planId, LocalDate dateKey) {
        return checkboxDslRepository.findAllByPlanIdAndDate(planId, dateKey);
    }

    public List<Checkbox> findAllByPlanIdAndDateRange(Long planId, LocalDate searchStart, LocalDate searchEnd) {
        return checkboxDslRepository.findAllByPlanIdAndDateRange(planId, searchStart, searchEnd);
    }

}
