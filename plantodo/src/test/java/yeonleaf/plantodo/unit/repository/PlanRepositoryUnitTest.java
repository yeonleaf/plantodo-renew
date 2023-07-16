package yeonleaf.plantodo.unit.repository;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import yeonleaf.plantodo.domain.Member;
import yeonleaf.plantodo.domain.Plan;
import yeonleaf.plantodo.dto.PlanReqDto;
import yeonleaf.plantodo.repository.MemberRepository;
import yeonleaf.plantodo.repository.PlanRepository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
public class PlanRepositoryUnitTest {

    @Autowired
    private MemberRepository memberRepository;

    @Autowired
    private PlanRepository planRepository;

    private Plan savePlan(Member member, int delay) {
        Plan plan1 = new Plan(new PlanReqDto("plan" + delay, LocalDate.now().plusDays(delay), LocalDate.now().plusDays(delay + 3)), member);
        return planRepository.save(plan1);
    }

    @Test
    @DisplayName("Plan 하나 조회")
    void getOnePlanTest() {

        Member member = memberRepository.save(new Member("test@abc.co.kr", "a63d@$ga"));

        Plan plan = savePlan(member, 0);
        Optional<Plan> findPlan = planRepository.findById(plan.getId());

        assertThat(findPlan.isPresent()).isTrue();
        assertThat(findPlan.get().getId()).isEqualTo(plan.getId());

    }

    @Test
    @DisplayName("모든 Plan 조회")
    void getAllPlanTest() {

        Member member = memberRepository.save(new Member("test@abc.co.kr", "a63d@$ga"));
        savePlan(member, 0);
        savePlan(member, 1);
        savePlan(member, 2);

        List<Plan> all = planRepository.findAll();
        assertThat(all.size()).isEqualTo(3);

    }

    @Test
    @DisplayName("Plan 하나 수정")
    void updateOnePlan() {

        Member member = memberRepository.save(new Member("test@abc.co.kr", "a63d@$ga"));
        Plan plan = savePlan(member, 0);

        plan.setTitle("revisedTitle");
        planRepository.save(plan);

        Optional<Plan> updatedPlan = planRepository.findById(plan.getId());
        assertThat(updatedPlan.isPresent()).isTrue();
        assertThat(updatedPlan.get().getTitle()).isEqualTo("revisedTitle");

    }

    @Test
    @DisplayName("Plan 하나 삭제")
    void deleteOnePlan() {

        Member member = memberRepository.save(new Member("test@abc.co.kr", "a63d@$ga"));
        Plan plan = savePlan(member, 0);

        planRepository.delete(plan);

        Optional<Plan> findPlan = planRepository.findById(plan.getId());
        assertThat(findPlan.isEmpty()).isTrue();

    }
}