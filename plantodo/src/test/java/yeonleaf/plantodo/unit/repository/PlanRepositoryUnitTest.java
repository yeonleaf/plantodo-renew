package yeonleaf.plantodo.unit.repository;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.ActiveProfiles;
import yeonleaf.plantodo.domain.Member;
import yeonleaf.plantodo.domain.Plan;
import yeonleaf.plantodo.domain.PlanStatus;
import yeonleaf.plantodo.dto.PlanReqDto;
import yeonleaf.plantodo.exceptions.ResourceNotFoundException;
import yeonleaf.plantodo.repository.MemberRepository;
import yeonleaf.plantodo.repository.PlanRepository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@ActiveProfiles("test")
public class PlanRepositoryUnitTest {

    @Autowired
    private MemberRepository memberRepository;

    @Autowired
    private PlanRepository planRepository;

    @Test
    @DisplayName("단건 정상 조회")
    void getOnePlanTest() {

        Member member = memberRepository.save(new Member("test@abc.co.kr", "a63d@$ga"));
        Plan plan = planRepository.save(new Plan("plan", LocalDate.of(2023, 7, 19), LocalDate.of(2023, 7, 31), member));

        Plan findPlan = planRepository.findById(plan.getId()).orElseThrow(ResourceNotFoundException::new);

        assertThat(findPlan.getId()).isEqualTo(plan.getId());
        assertThat(findPlan.getTitle()).isEqualTo(plan.getTitle());
        assertThat(findPlan.getStatus()).isEqualTo(plan.getStatus());

    }

    @Test
    @DisplayName("Plan 단건 삭제")
    void deleteOnePlan() {

        Member member = memberRepository.save(new Member("test@abc.co.kr", "a63d@$ga"));
        Plan plan = planRepository.save(new Plan("plan", LocalDate.of(2023, 7, 19), LocalDate.of(2023, 7, 31), member));

        planRepository.delete(plan);

        Optional<Plan> findPlan = planRepository.findById(plan.getId());
        assertThat(findPlan.isEmpty()).isTrue();

    }

    @Test
    @DisplayName("상태 변경 - NOW to COMPLETED")
    void changeOnePlan_nowToCompleted() {

        Member member = memberRepository.save(new Member("test@abc.co.kr", "a63d@$ga"));
        Plan plan = planRepository.save(new Plan("plan", LocalDate.of(2023, 7, 19), LocalDate.of(2023, 7, 31), member));

        plan.changeStatus();
        planRepository.save(plan);

        Plan findPlan = planRepository.findById(plan.getId()).orElseThrow(ResourceNotFoundException::new);
        assertThat(findPlan.getStatus()).isEqualTo(PlanStatus.COMPLETED);

    }

    @Test
    @DisplayName("상태 변경 - COMPLETED to NOW")
    void changeOnePlan_completedToNow() {

        Member member = memberRepository.save(new Member("test@abc.co.kr", "a63d@$ga"));
        Plan plan = planRepository.save(new Plan("plan", LocalDate.of(2023, 7, 19), LocalDate.of(2023, 7, 31), member, PlanStatus.COMPLETED));

        plan.changeStatus();
        planRepository.save(plan);

        Plan findPlan = planRepository.findById(plan.getId()).orElseThrow(ResourceNotFoundException::new);
        assertThat(findPlan.getStatus()).isEqualTo(PlanStatus.NOW);

    }

    @Test
    @DisplayName("상태 변경 - NOW to PAST")
    void changeOnePlan_nowToPast() {

        Member member = memberRepository.save(new Member("test@abc.co.kr", "a63d@$ga"));
        Plan plan = planRepository.save(new Plan("plan", LocalDate.of(2023, 7, 19), LocalDate.of(2023, 7, 31), member, PlanStatus.NOW));

        plan.changeToPast();
        planRepository.save(plan);

        Plan findPlan = planRepository.findById(plan.getId()).orElseThrow(ResourceNotFoundException::new);
        assertThat(findPlan.getStatus()).isEqualTo(PlanStatus.PAST);

    }

    @Test
    @DisplayName("상태 변경 - COMPLETED to PAST")
    void changeOnePlan_completedToPast() {

        Member member = memberRepository.save(new Member("test@abc.co.kr", "a63d@$ga"));
        Plan plan = planRepository.save(new Plan("plan", LocalDate.of(2023, 7, 19), LocalDate.of(2023, 7, 31), member, PlanStatus.COMPLETED));

        plan.changeToPast();
        planRepository.save(plan);

        Plan findPlan = planRepository.findById(plan.getId()).orElseThrow(ResourceNotFoundException::new);
        assertThat(findPlan.getStatus()).isEqualTo(PlanStatus.PAST);

    }

}
