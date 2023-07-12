package yeonleaf.plantodo.repository;

import yeonleaf.plantodo.domain.Member;
import yeonleaf.plantodo.domain.Plan;

import java.util.HashMap;
import java.util.Map;

public class MemoryPlanRepository {

    private Map<Long, Plan> plans = new HashMap<>();
    private Long id = 1L;

    public Plan save(Plan plan) {
        plan.setId(id);
        plans.put(id++, plan);
        return plan;
    }

}
