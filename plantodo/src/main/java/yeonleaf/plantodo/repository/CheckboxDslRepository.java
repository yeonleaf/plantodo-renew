package yeonleaf.plantodo.repository;

import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;
import yeonleaf.plantodo.domain.Checkbox;
import yeonleaf.plantodo.domain.QCheckbox;

import java.time.LocalDate;
import java.util.List;

@Repository
@RequiredArgsConstructor
public class CheckboxDslRepository {

    private final JPAQueryFactory qf;
    public static final QCheckbox qc = QCheckbox.checkbox;

    public List<Checkbox> findAllByGroupIdAndDate(Long groupId, LocalDate dateKey) {
        return qf.selectFrom(qc)
                .where(qc.group.id.eq(groupId))
                .where(qc.date.eq(dateKey))
                .fetch();
    }

    public List<Checkbox> findAllByGroupIdAndDateRange(Long groupId, LocalDate searchStart, LocalDate searchEnd) {
        return qf.selectFrom(qc)
                .where(qc.group.id.eq(groupId))
                .where(qc.date.between(searchStart, searchEnd))
                .fetch();
    }

    public List<Checkbox> findAllByPlanIdAndDate(Long planId, LocalDate dateKey) {
        return qf.selectFrom(qc)
                .where(qc.group.plan.id.eq(planId))
                .where(qc.date.eq(dateKey))
                .fetch();
    }

    public List<Checkbox> findAllByPlanIdAndDateRange(Long planId, LocalDate searchStart, LocalDate searchEnd) {
        return qf.selectFrom(qc)
                .where(qc.group.plan.id.eq(planId))
                .where(qc.date.between(searchStart, searchEnd))
                .fetch();
    }

}
