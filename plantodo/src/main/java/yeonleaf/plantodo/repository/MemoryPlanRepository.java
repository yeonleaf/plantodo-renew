package yeonleaf.plantodo.repository;

import yeonleaf.plantodo.domain.Member;
import yeonleaf.plantodo.domain.Plan;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

public class MemoryPlanRepository extends MemoryRepository<Plan> {

    private Map<Long, Plan> data = new HashMap<>();
    private Long id = 1L;

    public Plan save(Plan plan) {
        if (plan.getId() == null) {
            plan.setId(id);
            data.put(id++, plan);
        } else {
            Plan previousPlan = data.get(plan.getId());
            if (!previousPlan.equals(plan)) {
                data.remove(previousPlan.getId());
                data.put(plan.getId(), plan);
            }
        }
        return plan;
    }

    public Optional<Plan> findById(Long id) {
        if (data.containsKey(id)) {
            return Optional.of(data.get(id));
        } else {
            return Optional.empty();
        }
    }

    public void delete(Plan plan) {
        data.remove(plan.getId());
    }

}
