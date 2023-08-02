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
import yeonleaf.plantodo.domain.PlanStatus;
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
import java.util.Optional;

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

    @Test
    @DisplayName("정상 삭제")
    void deleteTestNormal() {

        Member member = memberRepository.save(new Member("test@abc.co.kr", "13d^3ea#"));
        PlanResDto planResDto = planService.save(new PlanReqDto("plan", LocalDate.now(), LocalDate.now().plusDays(3), member.getId()));
        CheckboxResDto checkboxResDto = checkboxService.save(new CheckboxReqDto("title", planResDto.getId(), LocalDate.now()));

        checkboxService.delete(checkboxResDto.getId());

        Optional<Checkbox> findCheckbox = checkboxRepository.findById(checkboxResDto.getId());
        assertThat(findCheckbox).isEmpty();

    }

    @Test
    @DisplayName("비정상 삭제 - Resource not found")
    void deleteTestAbnormal() {

        assertThrows(ResourceNotFoundException.class, () -> checkboxService.delete(Long.MAX_VALUE));

    }

    @Test
    @DisplayName("정상 상태 변경 - unchecked to checked")
    void changeStatusTestNormal_uncheckedToChecked() {

        Member member = memberRepository.save(new Member("test@abc.co.kr", "13d^3ea#"));
        PlanResDto planResDto = planService.save(new PlanReqDto("plan", LocalDate.now(), LocalDate.now().plusDays(3), member.getId()));
        CheckboxResDto checkboxResDto = checkboxService.save(new CheckboxReqDto("title", planResDto.getId(), LocalDate.now()));
        Long checkboxId = checkboxResDto.getId();

        checkboxService.change(checkboxId);

        Checkbox findCheckbox = checkboxRepository.findById(checkboxId).orElseThrow(ResourceNotFoundException::new);
        assertThat(findCheckbox.isChecked()).isTrue();

    }

    @Test
    @DisplayName("정상 상태 변경 - checked to unchecked")
    void changeStatusTestNormal_checkedToUnchecked() {

        Member member = memberRepository.save(new Member("test@abc.co.kr", "13d^3ea#"));
        PlanResDto planResDto = planService.save(new PlanReqDto("plan", LocalDate.now(), LocalDate.now().plusDays(3), member.getId()));
        CheckboxResDto checkboxResDto = checkboxService.save(new CheckboxReqDto("title", planResDto.getId(), LocalDate.now()));
        Long checkboxId = checkboxResDto.getId();

        checkboxService.change(checkboxId);
        checkboxService.change(checkboxId);

        Checkbox findCheckbox = checkboxRepository.findById(checkboxId).orElseThrow(ResourceNotFoundException::new);
        assertThat(findCheckbox.isChecked()).isFalse();

    }

    @Test
    @DisplayName("비정상 상태 변경 - Resource not found")
    void changeStatusAbnormal() {

        assertThrows(ResourceNotFoundException.class, () -> checkboxService.change(Long.MAX_VALUE));

    }

    @Test
    @DisplayName("정상 순수 컬렉션 조회 - planId로 조회")
    void allTestNormal_byPlanId() {

        Member member = memberRepository.save(new Member("test@abc.co.kr", "13d^3ea#"));
        PlanResDto planResDto = planService.save(new PlanReqDto("plan", LocalDate.now(), LocalDate.now().plusDays(3), member.getId()));
        checkboxService.save(new CheckboxReqDto("title", planResDto.getId(), LocalDate.now()));
        checkboxService.save(new CheckboxReqDto("title", planResDto.getId(), LocalDate.now()));
        checkboxService.save(new CheckboxReqDto("title", planResDto.getId(), LocalDate.now()));
        GroupResDto groupResDto = groupService.save(new GroupReqDto("title", 1, makeArrToList(), planResDto.getId()));

        List<CheckboxResDto> allByPlan = checkboxService.allByPlan(planResDto.getId());
        assertThat(allByPlan.size()).isEqualTo(7);

    }

    @Test
    @DisplayName("비정상 순수 컬렉션 조회 - planId로 조회")
    void allTestAbnormal_byPlanId() {

        assertThrows(ResourceNotFoundException.class, () -> checkboxService.allByPlan(Long.MAX_VALUE));

    }


    @Test
    @DisplayName("정상 순수 컬렉션 조회 - groupId로 조회")
    void allTestNormal_byGroupId() {

        Member member = memberRepository.save(new Member("test@abc.co.kr", "13d^3ea#"));
        PlanResDto planResDto = planService.save(new PlanReqDto("plan", LocalDate.now(), LocalDate.now().plusDays(3), member.getId()));
        checkboxService.save(new CheckboxReqDto("title", planResDto.getId(), LocalDate.now()));
        checkboxService.save(new CheckboxReqDto("title", planResDto.getId(), LocalDate.now()));
        checkboxService.save(new CheckboxReqDto("title", planResDto.getId(), LocalDate.now()));
        GroupResDto groupResDto = groupService.save(new GroupReqDto("title", 1, makeArrToList(), planResDto.getId()));

        List<CheckboxResDto> allByGroup = checkboxService.allByGroup(groupResDto.getId());
        assertThat(allByGroup.size()).isEqualTo(4);

    }

    @Test
    @DisplayName("비정상 순수 컬렉션 조회 - groupId로 조회")
    void allTestAbnormal_byGroupId() {

        assertThrows(ResourceNotFoundException.class, () -> checkboxService.allByGroup(Long.MAX_VALUE));

    }

    @Test
    @DisplayName("일별 컬렉션 정상 조회 - by group - all matched with key")
    void collectionFilteredByDateTestNormal_byGroup_allMatchedWithKey() {

        Member member = memberRepository.save(new Member("test@abc.co.kr", "13d^3ea#"));
        Plan plan = planRepository.save(new Plan("plan1", LocalDate.of(2023, 7, 19), LocalDate.of(2023, 7, 31), member, PlanStatus.NOW));
        GroupResDto groupResDto1 = groupService.save(new GroupReqDto("title1", 3, makeArrToList("화", "목"), plan.getId()));
        GroupResDto groupResDto2 = groupService.save(new GroupReqDto("title2", 1, makeArrToList(), plan.getId()));
        GroupResDto groupResDto3 = groupService.save(new GroupReqDto("title3", 2, makeArrToList("2"), plan.getId()));
        LocalDate dateKey = LocalDate.of(2023, 7, 25);

        assertThat(checkboxService.allByGroup(groupResDto1.getId(), dateKey).size()).isEqualTo(1);
        assertThat(checkboxService.allByGroup(groupResDto2.getId(), dateKey).size()).isEqualTo(1);
        assertThat(checkboxService.allByGroup(groupResDto3.getId(), dateKey).size()).isEqualTo(1);

    }

    @Test
    @DisplayName("일별 컬렉션 정상 조회 - by group - part of them matched with key")
    void collectionFilteredByDateTestNormal_byGroup_partOfThemMatchedWithKey() {

        Member member = memberRepository.save(new Member("test@abc.co.kr", "13d^3ea#"));
        Plan plan = planRepository.save(new Plan("plan1", LocalDate.of(2023, 7, 19), LocalDate.of(2023, 7, 31), member, PlanStatus.NOW));
        GroupResDto groupResDto1 = groupService.save(new GroupReqDto("title1", 3, makeArrToList("화", "목"), plan.getId()));
        GroupResDto groupResDto2 = groupService.save(new GroupReqDto("title2", 1, makeArrToList(), plan.getId()));
        GroupResDto groupResDto3 = groupService.save(new GroupReqDto("title3", 2, makeArrToList("2"), plan.getId()));
        LocalDate dateKey = LocalDate.of(2023, 7, 19);

        assertThat(checkboxService.allByGroup(groupResDto1.getId(), dateKey).size()).isEqualTo(0);
        assertThat(checkboxService.allByGroup(groupResDto2.getId(), dateKey).size()).isEqualTo(1);
        assertThat(checkboxService.allByGroup(groupResDto3.getId(), dateKey).size()).isEqualTo(1);

    }

    @Test
    @DisplayName("일별 컬렉션 정상 조회 - by group - no one matched with key")
    void collectionFilteredByDateTestNormal_byGroup_emptyResult() {

        Member member = memberRepository.save(new Member("test@abc.co.kr", "13d^3ea#"));
        Plan plan = planRepository.save(new Plan("plan1", LocalDate.of(2023, 7, 19), LocalDate.of(2023, 7, 31), member, PlanStatus.NOW));
        GroupResDto groupResDto1 = groupService.save(new GroupReqDto("title1", 3, makeArrToList("화", "목"), plan.getId()));
        GroupResDto groupResDto2 = groupService.save(new GroupReqDto("title2", 1, makeArrToList(), plan.getId()));
        GroupResDto groupResDto3 = groupService.save(new GroupReqDto("title3", 2, makeArrToList("2"), plan.getId()));
        LocalDate dateKey = LocalDate.of(2023, 7, 18);

        assertThat(checkboxService.allByGroup(groupResDto1.getId(), dateKey).size()).isEqualTo(0);
        assertThat(checkboxService.allByGroup(groupResDto2.getId(), dateKey).size()).isEqualTo(0);
        assertThat(checkboxService.allByGroup(groupResDto3.getId(), dateKey).size()).isEqualTo(0);

    }

    @Test
    @DisplayName("일별 컬렉션 정상 조회 - by plan - all matched with key")
    void collectionFilteredByDateTestNormal_byPlan_allMatchedWithKey() {

        Member member = memberRepository.save(new Member("test@abc.co.kr", "13d^3ea#"));
        PlanResDto planResDto = planService.save(new PlanReqDto("plan", LocalDate.of(2023, 7, 19), LocalDate.of(2023, 7, 31), member.getId()));
        Long planId = planResDto.getId();

        // daily checkbox
        checkboxService.save(new CheckboxReqDto("title", planId, LocalDate.of(2023, 7, 23)));

        // group
        groupService.save(new GroupReqDto("title1", 3, makeArrToList("화", "목", "일"), planId));
        groupService.save(new GroupReqDto("title2", 1, makeArrToList(), planId));
        groupService.save(new GroupReqDto("title3", 2, makeArrToList("2"), planId));

        LocalDate dateKey = LocalDate.of(2023, 7, 23);

        assertThat(checkboxService.allByPlan(planId, dateKey).size()).isEqualTo(4);

    }

    @Test
    @DisplayName("일별 컬렉션 정상 조회 - by plan - part of them matched with key")
    void collectionFilteredByDateTestNormal_byPlan_partOfThemMatchedWithKey() {

        Member member = memberRepository.save(new Member("test@abc.co.kr", "13d^3ea#"));
        PlanResDto planResDto = planService.save(new PlanReqDto("plan", LocalDate.of(2023, 7, 19), LocalDate.of(2023, 7, 31), member.getId()));
        Long planId = planResDto.getId();

        // daily checkbox
        checkboxService.save(new CheckboxReqDto("title", planId, LocalDate.of(2023, 7, 19)));
        checkboxService.save(new CheckboxReqDto("title", planId, LocalDate.of(2023, 7, 23)));
        checkboxService.save(new CheckboxReqDto("title", planId, LocalDate.of(2023, 7, 27)));

        // group
        groupService.save(new GroupReqDto("title1", 3, makeArrToList("화", "목", "일"), planId));
        groupService.save(new GroupReqDto("title2", 1, makeArrToList(), planId));
        groupService.save(new GroupReqDto("title3", 2, makeArrToList("2"), planId));

        LocalDate dateKey = LocalDate.of(2023, 7, 23);

        assertThat(checkboxService.allByPlan(planId, dateKey).size()).isEqualTo(4);

    }

    @Test
    @DisplayName("일별 컬렉션 정상 조회 - by plan - empty result")
    void collectionFilteredByDateTestNormal_byPlan_emptyResult() {

        Member member = memberRepository.save(new Member("test@abc.co.kr", "13d^3ea#"));
        PlanResDto planResDto = planService.save(new PlanReqDto("plan", LocalDate.of(2023, 7, 19), LocalDate.of(2023, 7, 31), member.getId()));
        Long planId = planResDto.getId();

        // daily checkbox
        checkboxService.save(new CheckboxReqDto("title", planId, LocalDate.of(2023, 7, 19)));
        checkboxService.save(new CheckboxReqDto("title", planId, LocalDate.of(2023, 7, 23)));
        checkboxService.save(new CheckboxReqDto("title", planId, LocalDate.of(2023, 7, 27)));

        // group
        groupService.save(new GroupReqDto("title1", 3, makeArrToList("화", "목", "일"), planId));
        groupService.save(new GroupReqDto("title2", 1, makeArrToList(), planId));
        groupService.save(new GroupReqDto("title3", 2, makeArrToList("2"), planId));

        LocalDate dateKey = LocalDate.of(2023, 7, 18);

        assertThat(checkboxService.allByPlan(planId, dateKey).size()).isEqualTo(0);

    }

    void assertSearchDates(String standard, Long id, LocalDate searchStart, LocalDate searchEnd, int expectedCnt, LocalDate... expectedList) {

        List<CheckboxResDto> filteredAll = (standard == "group"
                ? checkboxService.allByGroup(id, searchStart, searchEnd)
                : checkboxService.allByPlan(id, searchStart, searchEnd));
        assertThat(filteredAll.size()).isEqualTo(expectedCnt);
        assertThat(filteredAll.stream().map(CheckboxResDto::getDate)).containsOnly(expectedList);

    }

    @Test
    @DisplayName("기간 컬렉션 정상 조회 - by group")
    void collectionFilteredByDateRangeTestNormal_byGroup() {

        Member member = memberRepository.save(new Member("test@abc.co.kr", "13d^3ea#"));
        PlanResDto planResDto = planService.save(new PlanReqDto("plan", LocalDate.of(2023, 7, 19), LocalDate.of(2023, 7, 31), member.getId()));
        Long planId = planResDto.getId();
        GroupResDto groupResDto = groupService.save(new GroupReqDto("title", 3, makeArrToList("화", "목", "일"), planId));
        Long groupId = groupResDto.getId();

        assertSearchDates(
                "group",
                groupId,
                LocalDate.of(2023, 7, 19),
                LocalDate.of(2023, 7, 31),
                5,
                LocalDate.of(2023, 7, 20),
                LocalDate.of(2023, 7, 23),
                LocalDate.of(2023, 7, 25),
                LocalDate.of(2023, 7, 27),
                LocalDate.of(2023, 7, 30)
        );

        assertSearchDates(
                "group",
                groupId,
                LocalDate.of(2023, 7, 23),
                LocalDate.of(2023, 7, 26),
                2,
                LocalDate.of(2023, 7, 23),
                LocalDate.of(2023, 7, 25)
        );

        assertSearchDates(
                "group",
                groupId,
                LocalDate.of(2023, 7, 21),
                LocalDate.of(2023, 7, 22),
                0
        );

    }

    @Test
    @DisplayName("기간 컬렉션 비정상 조회 - by group - Resource not found")
    void collectionFilteredByDateRangeTestAbnormal_byGroup_resourceNotFound() {

        assertThrows(ResourceNotFoundException.class, () -> checkboxService.allByGroup(Long.MAX_VALUE, LocalDate.of(2023, 7, 23), LocalDate.of(2023, 7, 25)));
    }

    @Test
    @DisplayName("기간 컬렉션 정상 조회 - by plan")
    void collectionFilteredByDateRangeTestNormal_byPlan() {

        Member member = memberRepository.save(new Member("test@abc.co.kr", "13d^3ea#"));
        PlanResDto planResDto = planService.save(new PlanReqDto("plan", LocalDate.of(2023, 7, 19), LocalDate.of(2023, 7, 31), member.getId()));
        Long planId = planResDto.getId();

        // daily checkbox
        checkboxService.save(new CheckboxReqDto("title", planId, LocalDate.of(2023, 7, 19)));
        checkboxService.save(new CheckboxReqDto("title", planId, LocalDate.of(2023, 7, 23)));
        checkboxService.save(new CheckboxReqDto("title", planId, LocalDate.of(2023, 7, 27)));

        // group
        groupService.save(new GroupReqDto("title1", 3, makeArrToList("화", "목", "일"), planId));
        groupService.save(new GroupReqDto("title3", 2, makeArrToList("2"), planId));

        assertSearchDates(
                "plan",
                planId,
                LocalDate.of(2023, 7, 19),
                LocalDate.of(2023, 7, 21),
                4,
                LocalDate.of(2023, 7, 19),
                LocalDate.of(2023, 7, 20),
                LocalDate.of(2023, 7, 21)
        );

        assertSearchDates(
                "plan",
                planId,
                LocalDate.of(2023, 7, 23),
                LocalDate.of(2023, 7, 24),
                3,
                LocalDate.of(2023, 7, 23)
        );

        assertSearchDates(
                "plan",
                planId,
                LocalDate.of(2023, 7, 24),
                LocalDate.of(2023, 7, 28),
                5,
                LocalDate.of(2023, 7, 25),
                LocalDate.of(2023, 7, 27)
        );

        assertSearchDates(
                "plan",
                planId,
                LocalDate.of(2023, 7, 16),
                LocalDate.of(2023, 7, 18),
                0
        );

    }

    @Test
    @DisplayName("기간 비정상 조회 - by plan - Resource not found")
    void collectionFilteredByDateRangeTestAbnormal_byPlan_resourceNotFound() {

        assertThrows(ResourceNotFoundException.class, () -> checkboxService.allByPlan(Long.MAX_VALUE, LocalDate.of(2023, 7, 19), LocalDate.of(2023, 7, 23)));

    }

}
