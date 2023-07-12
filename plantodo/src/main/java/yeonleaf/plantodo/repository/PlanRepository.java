package yeonleaf.plantodo.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import yeonleaf.plantodo.domain.Plan;

@Repository
public interface PlanRepository extends JpaRepository<Plan, Long> {
}
