package yeonleaf.plantodo.unit.repository;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import yeonleaf.plantodo.domain.*;
import yeonleaf.plantodo.repository.CheckboxRepository;
import yeonleaf.plantodo.repository.GroupRepository;
import yeonleaf.plantodo.repository.MemberRepository;
import yeonleaf.plantodo.repository.PlanRepository;

import java.time.LocalDate;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
public class CheckboxRepositoryUnitTest {

    @Autowired
    private MemberRepository memberRepository;

    @Autowired
    private PlanRepository planRepository;

    @Autowired
    private GroupRepository groupRepository;

    @Autowired
    private CheckboxRepository checkboxRepository;

    @Test
    @DisplayName("정상 저장")
    void saveTestNormal() {

        Member member = memberRepository.save(new Member("test@abc.co.kr", "ab3$ax#@"));
        Plan plan = planRepository.save(new Plan("title", LocalDate.now(), LocalDate.now().plusDays(3), member));
        Group group = groupRepository.save(new Group(plan, "title", new Repetition(0L, "-1")));

        Checkbox checkbox = checkboxRepository.save(new Checkbox(group, LocalDate.now(), false));
        Optional<Checkbox> findCheckbox = checkboxRepository.findById(checkbox.getId());

        assertThat(findCheckbox.isPresent()).isTrue();
        assertThat(findCheckbox.get().getId()).isEqualTo(checkbox.getId());

    }
}
