package yeonleaf.plantodo.unit.repository;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import yeonleaf.plantodo.domain.Member;
import yeonleaf.plantodo.domain.Plan;
import yeonleaf.plantodo.dto.PlanReqDto;
import yeonleaf.plantodo.exceptions.ResourceNotFoundException;
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

    @Test
    @DisplayName("단건 정상 조회")
    void getOnePlanTest() {

        Member member = memberRepository.save(new Member("test@abc.co.kr", "a63d@$ga"));
        Plan plan = planRepository.save(new Plan("plan", LocalDate.of(2023, 7, 19), LocalDate.of(2023, 7, 31), member));

        Plan findPlan = planRepository.findById(plan.getId()).orElseThrow(ResourceNotFoundException::new);

        assertThat(findPlan.getId()).isEqualTo(plan.getId());
        assertThat(findPlan.getTitle()).isEqualTo(plan.getTitle());
        assertThat(findPlan.getStatus()).isEqualTo(plan.getStatus());
        assertThat(findPlan.getCheckedCnt()).isEqualTo(plan.getCheckedCnt());
        assertThat(findPlan.getUncheckedCnt()).isEqualTo(plan.getUncheckedCnt());

    }
//
//    @Test
//    @DisplayName("모든 Plan 조회")
//    void getAllPlanTest() {
//
//        Member member = memberRepository.save(new Member("test@abc.co.kr", "a63d@$ga"));
//        savePlan(member, 0);
//        savePlan(member, 1);
//        savePlan(member, 2);
//
//        List<Plan> all = planRepository.findAll();
//        assertThat(all.size()).isEqualTo(3);
//
//    }

//
//    @Test
//    @DisplayName("Plan 하나 삭제")
//    void deleteOnePlan() {
//
//        Member member = memberRepository.save(new Member("test@abc.co.kr", "a63d@$ga"));
//        Plan plan = savePlan(member, 0);
//
//        planRepository.delete(plan);
//
//        Optional<Plan> findPlan = planRepository.findById(plan.getId());
//        assertThat(findPlan.isEmpty()).isTrue();
//
//    }
}
