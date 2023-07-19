package yeonleaf.plantodo.unit.service;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import yeonleaf.plantodo.ServiceTestConfig;
import yeonleaf.plantodo.domain.Member;
import yeonleaf.plantodo.domain.Plan;
import yeonleaf.plantodo.dto.CheckboxReqDto;
import yeonleaf.plantodo.dto.CheckboxResDto;
import yeonleaf.plantodo.dto.PlanReqDto;
import yeonleaf.plantodo.dto.PlanResDto;
import yeonleaf.plantodo.exceptions.ResourceNotFoundException;
import yeonleaf.plantodo.repository.MemoryMemberRepository;
import yeonleaf.plantodo.repository.MemoryPlanRepository;
import yeonleaf.plantodo.service.CheckboxService;
import yeonleaf.plantodo.service.PlanService;

import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

@ExtendWith(SpringExtension.class)
@ContextConfiguration(classes = ServiceTestConfig.class)
public class CheckboxServiceUnitTest {

    @Autowired
    private MemoryPlanRepository planRepository;

    @Autowired
    private MemoryMemberRepository memberRepository;

    @Autowired
    private PlanService planService;

    @Autowired
    private CheckboxService checkboxService;


    @Test
    @DisplayName("정상 저장")
    void saveTestNormal() {

        Member member = memberRepository.save(new Member("test@abc.co.kr", "13d^3ea#"));
        PlanResDto planResDto = planService.save(new PlanReqDto("plan", LocalDate.now(), LocalDate.now().plusDays(3), member.getId()));
        CheckboxReqDto checkboxReqDto = new CheckboxReqDto("title", planResDto.getId(), LocalDate.now());

        CheckboxResDto checkboxResDto = checkboxService.save(checkboxReqDto);

        assertThat(checkboxResDto.getId()).isNotNull();

        Plan plan = planRepository.findById(planResDto.getId()).orElseThrow(ResourceNotFoundException::new);
        assertThat(plan.getUncheckedCnt()).isEqualTo(1);

    }

    @Test
    @DisplayName("비정상 저장 - Resource not found - Plan")
    void saveTestAbnormal_ResourceNotFound_Plan() {

        CheckboxReqDto checkboxReqDto = new CheckboxReqDto("title", 99999L, LocalDate.now());
        assertThrows(ResourceNotFoundException.class, () -> checkboxService.save(checkboxReqDto));

    }

    @Test
    @DisplayName("비정상 저장 - Resource not found - Group")
    void saveTestAbnormal_ResourceNotFound_Group() {

        Member member = memberRepository.save(new Member("test@abc.co.kr", "13d^3ea#"));
        Plan plan = planRepository.save(new Plan("plan", LocalDate.now(), LocalDate.now().plusDays(3), member));
        CheckboxReqDto checkboxReqDto = new CheckboxReqDto("title", plan.getId(), LocalDate.now());
        assertThrows(ResourceNotFoundException.class, () -> checkboxService.save(checkboxReqDto));

    }
}
