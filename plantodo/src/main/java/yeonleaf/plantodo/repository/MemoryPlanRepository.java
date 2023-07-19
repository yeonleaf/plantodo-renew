package yeonleaf.plantodo.repository;

import yeonleaf.plantodo.domain.Member;
import yeonleaf.plantodo.domain.Plan;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

public class MemoryPlanRepository extends MemoryRepository<Plan> {

    private Map<Long, Plan> plans = new HashMap<>();
    private Long id = 1L;

    public Plan save(Plan plan) {
        if (plan.getId() == null) {
            plan.setId(id);
            plans.put(id++, plan);
        } else {
            Plan previousPlan = plans.get(plan.getId());
            if (!previousPlan.equals(plan)) {
                plans.remove(previousPlan.getId());
                plans.put(plan.getId(), plan);
            }
        }
        return plan;
    }

    public Optional<Plan> findById(Long id) {
        if (plans.containsKey(id)) {
            return Optional.of(plans.get(id));
        } else {
            return Optional.empty();
        }
    }

    public void delete(Plan plan) {
        plans.remove(plan.getId());
    }

}
