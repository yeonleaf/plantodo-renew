package yeonleaf.plantodo.unit.service;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import yeonleaf.plantodo.ServiceTestConfig;
import yeonleaf.plantodo.domain.*;
import yeonleaf.plantodo.dto.*;
import yeonleaf.plantodo.repository.MemoryCheckboxRepository;
import yeonleaf.plantodo.repository.MemoryPlanRepository;
import yeonleaf.plantodo.service.GroupServiceTestImpl;
import yeonleaf.plantodo.service.PlanServiceTestImpl;

import java.time.LocalDate;
import java.util.Arrays;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@ExtendWith(SpringExtension.class)
@ContextConfiguration(classes = ServiceTestConfig.class)
public class GroupServiceUnitTest {

    @Autowired
    private GroupServiceTestImpl groupService;

    @Autowired
    private MemoryCheckboxRepository checkboxRepository;

    @Autowired
    private MemoryPlanRepository planRepository;

    @Autowired
    private PlanServiceTestImpl planService;

    private Member makeMember(String email, String password) {
        Member member = new Member(email, password);
        member.setId(1L);
        return member;
    }

    private List<String> makeArrToList(String... target) {
        return Arrays.asList(target);
    }

    private void makeSaveRepOptionTest(Long repOption, List<String> repValue, LocalDate start, LocalDate end, int expectedCnt) {

        Member member = makeMember("test@abc.co.kr", "3d^$a2df");
        Plan plan = planRepository.save(new Plan("plan", start, end, member));
        GroupResDto group = groupService.save(new GroupReqDto("group", repOption, repValue, plan.getId()));

        List<Checkbox> checkboxes = checkboxRepository.findByGroupId(group.getId());
        assertThat(checkboxes.size()).isEqualTo(expectedCnt);

        GroupResDto findGroup = groupService.one(group.getId());
        assertThat(findGroup.getUncheckedCnt()).isEqualTo(expectedCnt);

        PlanResDto findPlan = planService.one(plan.getId());
        assertThat(findPlan.getUncheckedCnt()).isEqualTo(expectedCnt);

    }

    @Test
    @DisplayName("repOption = 1L, start < end")
    void saveTestRepOption1L_EndGreaterThanStart() {

        makeSaveRepOptionTest(1L, makeArrToList(), LocalDate.of(2023, 7, 18), LocalDate.of(2023, 7, 31), 14);

    }

    @Test
    @DisplayName("repOption = 1L, start = end")
    void saveTestRepOption1L_EndEqualToStart() {

        makeSaveRepOptionTest(1L, makeArrToList(), LocalDate.of(2023, 7, 18), LocalDate.of(2023, 7, 18), 1);

    }

    @Test
    @DisplayName("repOption = 2L, start < end")
    void saveTestRepOption2L_EndGreaterThanStart() {

        makeSaveRepOptionTest(2L, makeArrToList("2"), LocalDate.of(2023, 7, 18), LocalDate.of(2023, 7, 31), 7);

    }

    @Test
    @DisplayName("repOption = 2L, start = end")
    void saveTestRepOption2L_EndEqualToStart() {

        makeSaveRepOptionTest(2L, makeArrToList("2"), LocalDate.of(2023, 7, 18), LocalDate.of(2023, 7, 18), 1);

    }

    @Test
    @DisplayName("repOption = 3L, start < end")
    void saveTestRepOption3L_EndGreaterThanStart() {

        makeSaveRepOptionTest(3L, makeArrToList("월", "수", "금"), LocalDate.of(2023, 7, 18), LocalDate.of(2023, 7, 31), 6);

    }

    @Test
    @DisplayName("repOption = 3L, start = end")
    void saveTestRepOption3L_EndEqualToStart() {

        makeSaveRepOptionTest(3L, makeArrToList("월", "수", "금"), LocalDate.of(2023, 7, 18), LocalDate.of(2023, 7, 18), 0);

    }

}
