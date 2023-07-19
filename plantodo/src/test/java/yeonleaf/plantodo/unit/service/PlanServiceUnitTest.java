package yeonleaf.plantodo.unit.service;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import yeonleaf.plantodo.ServiceTestConfig;
import yeonleaf.plantodo.converter.RepInToOutConverter;
import yeonleaf.plantodo.converter.RepOutToInConverter;
import yeonleaf.plantodo.domain.Member;
import yeonleaf.plantodo.dto.*;
import yeonleaf.plantodo.exceptions.ResourceNotFoundException;
import yeonleaf.plantodo.repository.MemoryCheckboxRepository;
import yeonleaf.plantodo.repository.MemoryGroupRepository;
import yeonleaf.plantodo.repository.MemoryPlanRepository;
import yeonleaf.plantodo.service.CheckboxServiceTestImpl;
import yeonleaf.plantodo.service.GroupServiceTestImpl;
import yeonleaf.plantodo.service.MemberServiceTestImpl;
import yeonleaf.plantodo.service.PlanServiceTestImpl;

import java.time.LocalDate;
import java.util.Arrays;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

@ExtendWith(SpringExtension.class)
@ContextConfiguration(classes = ServiceTestConfig.class)
public class PlanServiceUnitTest {

    @Autowired
    private MemberServiceTestImpl memberService;

    @Autowired
    private PlanServiceTestImpl planService;

    @Autowired
    private GroupServiceTestImpl groupService;

    private List<String> makeArrToList(String... target) {
        return Arrays.asList(target);
    }

    @Test
    @DisplayName("정상 등록 - repOption = 0인 group 하나 생성 확인")
    void saveTestNormal() {

        MemberResDto member = memberService.save(new MemberReqDto("test@abc.co.kr", "3s1@adf2"));
        PlanResDto plan = planService.save(new PlanReqDto("title", LocalDate.now(), LocalDate.now().plusDays(3), member.getId()));

        List<GroupResDto> groups = groupService.findAllByPlanId(plan.getId());

        assertThat(groups.size()).isEqualTo(1);
        assertThat(groups.get(0).getRepOption()).isEqualTo(0L);

    }

    @Test
    @DisplayName("정상 조회")
    void oneTestNormal() {

        MemberResDto member = memberService.save(new MemberReqDto("test@abc.co.kr", "3s1@adf2"));
        PlanResDto plan = planService.save(new PlanReqDto("title", LocalDate.now(), LocalDate.now().plusDays(3), member.getId()));

        PlanResDto findPlan = planService.one(plan.getId());

        assertThat(findPlan.getId()).isEqualTo(plan.getId());
        assertThat(findPlan.getStart()).isEqualTo(plan.getStart());
        assertThat(findPlan.getEnd()).isEqualTo(plan.getEnd());
        assertThat(findPlan.getCheckedCnt()).isEqualTo(plan.getCheckedCnt());
        assertThat(findPlan.getUncheckedCnt()).isEqualTo(plan.getUncheckedCnt());
        assertThat(findPlan.getStatus()).isEqualTo(plan.getStatus());

    }

    @Test
    @DisplayName("비정상 조회")
    void oneTestAbnormal() {

        assertThrows(ResourceNotFoundException.class, () -> planService.one(9999L));

    }

}
