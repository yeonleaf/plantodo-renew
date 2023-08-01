package yeonleaf.plantodo.repository;

import com.querydsl.core.types.Projections;
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

}
