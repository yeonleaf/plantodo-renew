package yeonleaf.plantodo.unit.service;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import yeonleaf.plantodo.ServiceTestConfig;
import yeonleaf.plantodo.domain.Checkbox;
import yeonleaf.plantodo.domain.Member;
import yeonleaf.plantodo.domain.Plan;
import yeonleaf.plantodo.dto.*;
import yeonleaf.plantodo.exceptions.ResourceNotFoundException;
import yeonleaf.plantodo.repository.MemoryCheckboxRepository;
import yeonleaf.plantodo.repository.MemoryMemberRepository;
import yeonleaf.plantodo.repository.MemoryPlanRepository;
import yeonleaf.plantodo.service.CheckboxService;
import yeonleaf.plantodo.service.GroupService;
import yeonleaf.plantodo.service.PlanService;

import java.time.LocalDate;
import java.util.Arrays;
import java.util.List;

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
    private MemoryCheckboxRepository checkboxRepository;

    @Autowired
    private PlanService planService;

    @Autowired
    private CheckboxService checkboxService;

    @Autowired
    private GroupService groupService;

    private List<String> makeArrToList(String... target) {
        return Arrays.asList(target);
    }

    @Test
    @DisplayName("정상 저장")
    void saveTestNormal() {

        Member member = memberRepository.save(new Member("test@abc.co.kr", "13d^3ea#"));
        PlanResDto planResDto = planService.save(new PlanReqDto("plan", LocalDate.now(), LocalDate.now().plusDays(3), member.getId()));
        CheckboxReqDto checkboxReqDto = new CheckboxReqDto("title", planResDto.getId(), LocalDate.now());

        CheckboxResDto checkboxResDto = checkboxService.save(checkboxReqDto);

        assertThat(checkboxResDto.getId()).isNotNull();

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

    @Test
    @DisplayName("단건 정상 조회")
    void oneTestNormal() {

        Member member = memberRepository.save(new Member("test@abc.co.kr", "13d^3ea#"));
        PlanResDto planResDto = planService.save(new PlanReqDto("plan", LocalDate.now(), LocalDate.now().plusDays(3), member.getId()));
        CheckboxReqDto checkboxReqDto = new CheckboxReqDto("title", planResDto.getId(), LocalDate.now());
        CheckboxResDto checkboxResDto = checkboxService.save(checkboxReqDto);

        CheckboxResDto findCheckboxResDto = checkboxService.one(checkboxResDto.getId());

        assertThat(checkboxResDto.equals(findCheckboxResDto)).isTrue();

    }

    @Test
    @DisplayName("단건 비정상 조회")
    void oneTestAbnormal() {

        assertThrows(ResourceNotFoundException.class, () -> checkboxService.one(Long.MAX_VALUE));

    }

    @Test
    @DisplayName("정상 수정 - checkbox not in group")
    void updateTestNormal_checkboxNotInGroup() {

        Member member = memberRepository.save(new Member("test@abc.co.kr", "13d^3ea#"));
        PlanResDto planResDto = planService.save(new PlanReqDto("plan", LocalDate.now(), LocalDate.now().plusDays(3), member.getId()));
        CheckboxResDto checkboxResDto = checkboxService.save(new CheckboxReqDto("title", planResDto.getId(), LocalDate.now()));

        CheckboxResDto updatedCheckbox = checkboxService.update(new CheckboxUpdateReqDto(checkboxResDto.getId(), "updatedTitle"));
        Checkbox findCheckbox = checkboxRepository.findById(checkboxResDto.getId()).orElseThrow(ResourceNotFoundException::new);

        assertThat(findCheckbox.getId()).isEqualTo(updatedCheckbox.getId());
        assertThat(updatedCheckbox.getTitle()).isEqualTo("updatedTitle");
        assertThat(findCheckbox.getTitle()).isEqualTo(updatedCheckbox.getTitle());

    }

    @Test
    @DisplayName("정상 수정 - checkbox in group")
    void updateTestNormal_checkboxInGroup() {

        Member member = memberRepository.save(new Member("test@abc.co.kr", "13d^3ea#"));
        PlanResDto planResDto = planService.save(new PlanReqDto("plan", LocalDate.now(), LocalDate.now().plusDays(3), member.getId()));
        GroupResDto groupResDto = groupService.save(new GroupReqDto("title", 1, makeArrToList(), planResDto.getId()));
        Checkbox checkbox = checkboxRepository.findByGroupId(groupResDto.getId()).get(0);

        CheckboxUpdateReqDto checkboxUpdateReqDto = new CheckboxUpdateReqDto(checkbox.getId(), "updatedTitle");
        checkboxService.update(checkboxUpdateReqDto);

        Checkbox findCheckbox = checkboxRepository.findById(checkbox.getId()).orElseThrow(ResourceNotFoundException::new);
        assertThat(findCheckbox.getTitle()).isEqualTo("updatedTitle");

    }

    @Test
    @DisplayName("비정상 수정 - Resource not found")
    void updateTestAbnormal_resourceNotFound() {

        CheckboxUpdateReqDto checkboxUpdateReqDto = new CheckboxUpdateReqDto(Long.MAX_VALUE, "updatedTitle");
        assertThrows(ResourceNotFoundException.class, () -> checkboxService.update(checkboxUpdateReqDto));

    }

}
