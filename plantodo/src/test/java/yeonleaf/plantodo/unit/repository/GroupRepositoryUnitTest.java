package yeonleaf.plantodo.unit.repository;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import yeonleaf.plantodo.domain.Group;
import yeonleaf.plantodo.domain.Member;
import yeonleaf.plantodo.domain.Plan;
import yeonleaf.plantodo.domain.Repetition;
import yeonleaf.plantodo.exceptions.ResourceNotFoundException;
import yeonleaf.plantodo.repository.GroupRepository;
import yeonleaf.plantodo.repository.MemberRepository;
import yeonleaf.plantodo.repository.PlanRepository;

import java.time.LocalDate;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
public class GroupRepositoryUnitTest {

    @Autowired
    private MemberRepository memberRepository;

    @Autowired
    private PlanRepository planRepository;

    @Autowired
    private GroupRepository groupRepository;

    private List<String> makeArrToList(String... target) {
        return Arrays.asList(target);
    }

    @Test
    @DisplayName("정상 등록")
    void saveTestNormal() {
        Member member = memberRepository.save(new Member("test@abc.co.kr", "1eab^d2a"));
        Plan plan = planRepository.save(new Plan("title", LocalDate.now(), LocalDate.now().plusDays(3), member));
        Repetition repetition = new Repetition(3L, "0000001");
        Group group = groupRepository.save(new Group(plan, "title", repetition));
        assertThat(group.getId()).isNotNull();
        assertThat(group.getRepetition().getId()).isNotNull();
    }

    @Test
    @DisplayName("정상 조회 - one")
    void getOneTestNormal() {

        Member member = memberRepository.save(new Member("test@abc.co.kr", "1eab^d2a"));
        Plan plan = planRepository.save(new Plan("title", LocalDate.now(), LocalDate.now().plusDays(3), member));

        Repetition repetition = new Repetition(3L, "0000001");
        Group group = groupRepository.save(new Group(plan, "title", repetition));

        Group findGroup = groupRepository.findById(group.getId()).orElseThrow(ResourceNotFoundException::new);
        assertThat(group.equals(findGroup)).isTrue();

    }

//    @Test
//    @DisplayName("정상 조회 - all - by plan id")
//    void getAllTestNormal() {
//
//        Member member = memberRepository.save(new Member("test@abc.co.kr", "1eab^d2a"));
//        Plan plan = planRepository.save(new Plan("title", LocalDate.now(), LocalDate.now().plusDays(3), member));
//
//        Repetition repetition1 = new Repetition(3L, "0000001");
//        Group group1 = groupRepository.save(new Group(plan, "title", repetition1));
//
//        Repetition repetition2 = new Repetition(3L, "0000001");
//        Group group2 = groupRepository.save(new Group(plan, "title", repetition2));
//
//        List<Group> findGroup = groupRepository.findByPlanId(plan.getId());
//
//        assertThat(findGroup.size()).isEqualTo(2);
//        assertThat(findGroup.get(0).getId()).isEqualTo(group1.getId());
//        assertThat(findGroup.get(1).getId()).isEqualTo(group2.getId());
//
//    }

}
