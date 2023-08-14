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
import yeonleaf.plantodo.repository.MemoryRepository;
import yeonleaf.plantodo.service.CheckboxService;
import yeonleaf.plantodo.service.GroupService;
import yeonleaf.plantodo.service.PlanService;
import yeonleaf.plantodo.service.CheckboxServiceTestImpl;
import yeonleaf.plantodo.domain.Group;

import java.time.LocalDate;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

/**
 * target : {@link CheckboxServiceTestImpl}에 있는 모든 메소드
 * target description : {@link CheckboxService}의 테스트용 구현체
 *                      {@link MemoryRepository}를 상속받은 서브클래스들을 Repository로 주입받음 (DB를 메모리로 대신)
 *
 * ※ 용어 정리
 * 일일 할일 : 할일 그룹 없이 일정에 직접 추가되는 반복되지 않는 할일
 * 그룹 할일 : 할일 그룹에 속한 반복되는 할일
 * 일일 할일과 그룹 할일은 같은 {@link Checkbox} 객체로, {@link Plan}에 직접 속해 있으면 일일 할일, {@link Group}이 생성하고 관리하면 그룹 할일로 분류한다.
 */
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

    /**
     * repValue 입력을 위한 보조 메소드
     */
    private List<String> makeArrToList(String... target) {
        return Arrays.asList(target);
    }


    /**
     * 일일 할일 저장 메소드 관련 테스트
     * @see CheckboxServiceTestImpl#save(CheckboxReqDto)
     */
    @Test
    @DisplayName("정상 저장 - 리턴받은 객체에 회원 id 값이 있는지 확인한다.")
    void saveTestNormal() {

        // given
        Member member = memberRepository.save(new Member("test@abc.co.kr", "13d^3ea#"));
        PlanResDto planResDto = planService.save(new PlanReqDto("plan", LocalDate.now(), LocalDate.now().plusDays(3), member.getId()));
        CheckboxReqDto checkboxReqDto = new CheckboxReqDto("title", planResDto.getId(), LocalDate.now());

        // when
        CheckboxResDto checkboxResDto = checkboxService.save(checkboxReqDto);

        // then
        assertThat(checkboxResDto.getId()).isNotNull();

    }

    @Test
    @DisplayName("비정상 저장 - 일일 할일을 추가할 일정이 없는 경우 ResourceNotFoundException을 던지는지 확인한다.")
    void saveTestAbnormal_ResourceNotFound_Plan() {

        // given
        CheckboxReqDto checkboxReqDto = new CheckboxReqDto("title", 99999L, LocalDate.now());

        // when - then
        assertThrows(ResourceNotFoundException.class, () -> checkboxService.save(checkboxReqDto));

    }


    /**
     * 일일 할일 단건 조회 메소드
     * @see CheckboxServiceTestImpl#one(Long)
     */
    @Test
    @DisplayName("단건 정상 조회 - 저장한 일일 할일과 조회한 일일 할일의 내용이 같은지 확인한다.")
    void oneTestNormal() {

        // given
        Member member = memberRepository.save(new Member("test@abc.co.kr", "13d^3ea#"));
        PlanResDto planResDto = planService.save(new PlanReqDto("plan", LocalDate.now(), LocalDate.now().plusDays(3), member.getId()));
        CheckboxReqDto checkboxReqDto = new CheckboxReqDto("title", planResDto.getId(), LocalDate.now());
        CheckboxResDto checkboxResDto = checkboxService.save(checkboxReqDto);

        // when
        CheckboxResDto findCheckboxResDto = checkboxService.one(checkboxResDto.getId());

        // then
        assertThat(checkboxResDto.equals(findCheckboxResDto)).isTrue();

    }

    @Test
    @DisplayName("단건 비정상 조회 - 조회할 일일 할일이 없는 경우 ResourceNotFoundException을 던지는지 확인한다.")
    void oneTestAbnormal() {

        // given - when - then
        assertThrows(ResourceNotFoundException.class, () -> checkboxService.one(Long.MAX_VALUE));

    }


    /**
     * 할일 수정 메소드 관련 테스트
     * @see CheckboxServiceTestImpl
     * 수정 로직은 title만 수정하며, 로직은 할일의 종류(일일 할일, 그룹 할일)와 상관 없이 동일하다.
     */
    @Test
    @DisplayName("정상 수정 - 일일 할일을 수정한 후 다시 조회한 일일 할일의 title이 정상적으로 수정되었는지 확인한다.")
    void updateTestNormal_checkboxNotInGroup() {

        // given
        Member member = memberRepository.save(new Member("test@abc.co.kr", "13d^3ea#"));
        PlanResDto planResDto = planService.save(new PlanReqDto("plan", LocalDate.now(), LocalDate.now().plusDays(3), member.getId()));
        CheckboxResDto checkboxResDto = checkboxService.save(new CheckboxReqDto("title", planResDto.getId(), LocalDate.now()));

        // when
        CheckboxResDto updatedCheckbox = checkboxService.update(new CheckboxUpdateReqDto(checkboxResDto.getId(), "updatedTitle"));

        // then
        Checkbox findCheckbox = checkboxRepository.findById(checkboxResDto.getId()).orElseThrow(ResourceNotFoundException::new);
        assertThat(findCheckbox.getId()).isEqualTo(updatedCheckbox.getId());
        assertThat(updatedCheckbox.getTitle()).isEqualTo("updatedTitle");
        assertThat(findCheckbox.getTitle()).isEqualTo(updatedCheckbox.getTitle());

    }

    @Test
    @DisplayName("정상 수정 - 그룹 할일을 수정한 후 다시 조회한 그룹 할일의 title이 정상적으로 수정되었는지 확인한다.")
    void updateTestNormal_checkboxInGroup() {

        // given
        Member member = memberRepository.save(new Member("test@abc.co.kr", "13d^3ea#"));
        PlanResDto planResDto = planService.save(new PlanReqDto("plan", LocalDate.now(), LocalDate.now().plusDays(3), member.getId()));
        GroupResDto groupResDto = groupService.save(new GroupReqDto("title", 1, makeArrToList(), planResDto.getId()));
        Checkbox checkbox = checkboxRepository.findByGroupId(groupResDto.getId()).get(0);
        CheckboxUpdateReqDto checkboxUpdateReqDto = new CheckboxUpdateReqDto(checkbox.getId(), "updatedTitle");

        // when
        checkboxService.update(checkboxUpdateReqDto);

        // then
        Checkbox findCheckbox = checkboxRepository.findById(checkbox.getId()).orElseThrow(ResourceNotFoundException::new);
        assertThat(findCheckbox.getTitle()).isEqualTo("updatedTitle");

    }

    @Test
    @DisplayName("비정상 수정 - 수정할 할일이 없는 경우 ResourceNotFoundException을 던지는지 확인한다.")
    void updateTestAbnormal_resourceNotFound() {

        // given
        CheckboxUpdateReqDto checkboxUpdateReqDto = new CheckboxUpdateReqDto(Long.MAX_VALUE, "updatedTitle");

        // when - then
        assertThrows(ResourceNotFoundException.class, () -> checkboxService.update(checkboxUpdateReqDto));

    }

    /**
     * 할일 삭제 메소드 관련 테스트
     * @see CheckboxServiceTestImpl#delete(Long)
     * 할일 삭제는 할일의 종류(일일, 그룹)에 관계없이 동일하다.
     */
    @Test
    @DisplayName("정상 삭제 - 삭제 후 다시 할일을 조회했을 때 빈 결과값을 리턴한다.")
    void deleteTestNormal() {

        // given
        Member member = memberRepository.save(new Member("test@abc.co.kr", "13d^3ea#"));
        PlanResDto planResDto = planService.save(new PlanReqDto("plan", LocalDate.now(), LocalDate.now().plusDays(3), member.getId()));
        CheckboxResDto checkboxResDto = checkboxService.save(new CheckboxReqDto("title", planResDto.getId(), LocalDate.now()));

        // when
        checkboxService.delete(checkboxResDto.getId());

        // then
        Optional<Checkbox> findCheckbox = checkboxRepository.findById(checkboxResDto.getId());
        assertThat(findCheckbox).isEmpty();

    }

    @Test
    @DisplayName("비정상 삭제 - 삭제할 할일이 없는 경우 ResourceNotFoundException을 던지는지 확인한다.")
    void deleteTestAbnormal() {

        assertThrows(ResourceNotFoundException.class, () -> checkboxService.delete(Long.MAX_VALUE));

    }


    /**
     * 할일 상태 변경 메소드 관련 테스트
     * checked ↔ unchecked
     * @see CheckboxServiceTestImpl#change(Long)
     */
    @Test
    @DisplayName("정상 상태 변경 - unchecked to checked - 상태 변경 후 다시 조회한 할일의 상태가 checked가 되었는지 확인한다.")
    void changeStatusTestNormal_uncheckedToChecked() {

        // given
        Member member = memberRepository.save(new Member("test@abc.co.kr", "13d^3ea#"));
        PlanResDto planResDto = planService.save(new PlanReqDto("plan", LocalDate.now(), LocalDate.now().plusDays(3), member.getId()));
        CheckboxResDto checkboxResDto = checkboxService.save(new CheckboxReqDto("title", planResDto.getId(), LocalDate.now()));
        Long checkboxId = checkboxResDto.getId();

        // when
        checkboxService.change(checkboxId);

        // then
        Checkbox findCheckbox = checkboxRepository.findById(checkboxId).orElseThrow(ResourceNotFoundException::new);
        assertThat(findCheckbox.isChecked()).isTrue();

    }

    @Test
    @DisplayName("정상 상태 변경 - checked to unchecked - 상태 변경 후 다시 조회한 할일의 상태가 unchecked가 되었는지 확인한다.")
    void changeStatusTestNormal_checkedToUnchecked() {

        // given
        Member member = memberRepository.save(new Member("test@abc.co.kr", "13d^3ea#"));
        PlanResDto planResDto = planService.save(new PlanReqDto("plan", LocalDate.now(), LocalDate.now().plusDays(3), member.getId()));
        CheckboxResDto checkboxResDto = checkboxService.save(new CheckboxReqDto("title", planResDto.getId(), LocalDate.now()));
        Long checkboxId = checkboxResDto.getId();

        // when
        checkboxService.change(checkboxId);
        checkboxService.change(checkboxId);

        // then
        Checkbox findCheckbox = checkboxRepository.findById(checkboxId).orElseThrow(ResourceNotFoundException::new);
        assertThat(findCheckbox.isChecked()).isFalse();

    }

    @Test
    @DisplayName("비정상 상태 변경 - 상태를 변경할 할일이 없는 경우 ResourceNotFoundException을 던지는지 확인한다.")
    void changeStatusAbnormal() {

        // given - when - then
        assertThrows(ResourceNotFoundException.class, () -> checkboxService.change(Long.MAX_VALUE));

    }


    /**
     * 할일 순수 컬렉션 조회 메소드 관련 테스트
     * 순수 컬렉션이란 필터링이 걸려 있지 않은 컬렉션을 의미함
     *
     * 일정을 기준으로 조회하는 경우 : 일정 내의 모든 일일 할일과 그룹 할일을 조회한다.
     * @see CheckboxServiceTestImpl#allByPlan(Long)
     *
     * 그룹을 기준으로 조회하는 경우 : 그룹 내의 모든 그룹 할일을 조회한다.
     * @see CheckboxServiceTestImpl#allByGroup(Long)
     */
    @Test
    @DisplayName("정상 순수 컬렉션 조회 - 일정 기준 - 일일 할일과 그룹 할일을 같이 조회한다.")
    void allTestNormal_byPlanId() {

        // given
        Member member = memberRepository.save(new Member("test@abc.co.kr", "13d^3ea#"));
        PlanResDto planResDto = planService.save(new PlanReqDto("plan", LocalDate.now(), LocalDate.now().plusDays(3), member.getId()));

        // 일일 할일 등록
        checkboxService.save(new CheckboxReqDto("title", planResDto.getId(), LocalDate.now()));
        checkboxService.save(new CheckboxReqDto("title", planResDto.getId(), LocalDate.now()));
        checkboxService.save(new CheckboxReqDto("title", planResDto.getId(), LocalDate.now()));

        // 그룹 할일 등록
        groupService.save(new GroupReqDto("title", 1, makeArrToList(), planResDto.getId()));

        // when
        List<CheckboxResDto> allByPlan = checkboxService.allByPlan(planResDto.getId());

        // then
        assertThat(allByPlan.size()).isEqualTo(7);

    }

    @Test
    @DisplayName("비정상 순수 컬렉션 조회 - 일정 기준 - 기준으로 삼을 일정이 없는 경우 ResourceNotFoundException을 던지는지 확인한다.")
    void allTestAbnormal_byPlanId() {

        assertThrows(ResourceNotFoundException.class, () -> checkboxService.allByPlan(Long.MAX_VALUE));

    }


    @Test
    @DisplayName("정상 순수 컬렉션 조회 - 그룹 기준 - 그룹 할일만 조회한다.")
    void allTestNormal_byGroupId() {

        // given
        Member member = memberRepository.save(new Member("test@abc.co.kr", "13d^3ea#"));
        PlanResDto planResDto = planService.save(new PlanReqDto("plan", LocalDate.now(), LocalDate.now().plusDays(3), member.getId()));

        // 일일 할일 등록
        checkboxService.save(new CheckboxReqDto("title", planResDto.getId(), LocalDate.now()));
        checkboxService.save(new CheckboxReqDto("title", planResDto.getId(), LocalDate.now()));
        checkboxService.save(new CheckboxReqDto("title", planResDto.getId(), LocalDate.now()));

        // 그룹 할일 등록
        GroupResDto groupResDto = groupService.save(new GroupReqDto("title", 1, makeArrToList(), planResDto.getId()));

        // when
        List<CheckboxResDto> allByGroup = checkboxService.allByGroup(groupResDto.getId());

        // then
        assertThat(allByGroup.size()).isEqualTo(4);

    }

    @Test
    @DisplayName("비정상 순수 컬렉션 조회 - 그룹 기준 - 기준으로 삼을 그룹이 없는 경우 ResourceNotFoundException을 던지는지 확인한다.")
    void allTestAbnormal_byGroupId() {

        // given - when - then
        assertThrows(ResourceNotFoundException.class, () -> checkboxService.allByGroup(Long.MAX_VALUE));

    }


    /**
     * 일별 컬렉션 조회 메소드 관련 테스트
     * 일별 컬렉션이란 검색일과 date 필드가 일치하는 할일만 조회하는 컬렉션이다.
     *
     * 일정을 기준으로 조회하는 경우 : 일정 내의 일일 할일과 일정에 속한 그룹에 속한 그룹 할일 중 검색일에 생성된 할일을 모두 조회한다.
     * @see CheckboxServiceTestImpl#allByPlan(Long, LocalDate)
     *
     * 그룹을 기준으로 조회하는 경우 : 그룹에 속한 그룹 할일 중 검색일에 생성된 할일을 모두 조회한다.
     * @see CheckboxServiceTestImpl#allByGroup(Long, LocalDate)
     */
    @Test
    @DisplayName("일별 컬렉션 정상 조회 - 그룹 기준 - 세 개의 그룹 모두 검색일에 할일을 생성했음을 확인한다.")
    void collectionFilteredByDateTestNormal_byGroup_allMatchedWithKey() {

        // given
        Member member = memberRepository.save(new Member("test@abc.co.kr", "13d^3ea#"));
        Plan plan = planRepository.save(new Plan("plan1", LocalDate.of(2023, 7, 19), LocalDate.of(2023, 7, 31), member, PlanStatus.NOW));
        GroupResDto groupResDto1 = groupService.save(new GroupReqDto("title1", 3, makeArrToList("화", "목"), plan.getId()));
        GroupResDto groupResDto2 = groupService.save(new GroupReqDto("title2", 1, makeArrToList(), plan.getId()));
        GroupResDto groupResDto3 = groupService.save(new GroupReqDto("title3", 2, makeArrToList("2"), plan.getId()));
        LocalDate dateKey = LocalDate.of(2023, 7, 25);

        // when - then
        assertThat(checkboxService.allByGroup(groupResDto1.getId(), dateKey).size()).isEqualTo(1);
        assertThat(checkboxService.allByGroup(groupResDto2.getId(), dateKey).size()).isEqualTo(1);
        assertThat(checkboxService.allByGroup(groupResDto3.getId(), dateKey).size()).isEqualTo(1);

    }

    @Test
    @DisplayName("일별 컬렉션 정상 조회 - 그룹 기준 - 세 개의 그룹 중 두 개의 그룹만 검색일에 할일을 생성했음을 확인한다.")
    void collectionFilteredByDateTestNormal_byGroup_partOfThemMatchedWithKey() {

        // given
        Member member = memberRepository.save(new Member("test@abc.co.kr", "13d^3ea#"));
        Plan plan = planRepository.save(new Plan("plan1", LocalDate.of(2023, 7, 19), LocalDate.of(2023, 7, 31), member, PlanStatus.NOW));
        GroupResDto groupResDto1 = groupService.save(new GroupReqDto("title1", 3, makeArrToList("화", "목"), plan.getId()));
        GroupResDto groupResDto2 = groupService.save(new GroupReqDto("title2", 1, makeArrToList(), plan.getId()));
        GroupResDto groupResDto3 = groupService.save(new GroupReqDto("title3", 2, makeArrToList("2"), plan.getId()));
        LocalDate dateKey = LocalDate.of(2023, 7, 19);

        // when - then
        assertThat(checkboxService.allByGroup(groupResDto1.getId(), dateKey).size()).isEqualTo(0);
        assertThat(checkboxService.allByGroup(groupResDto2.getId(), dateKey).size()).isEqualTo(1);
        assertThat(checkboxService.allByGroup(groupResDto3.getId(), dateKey).size()).isEqualTo(1);

    }

    @Test
    @DisplayName("일별 컬렉션 정상 조회 - 그룹 기준 - 세 개의 그룹 중 어떤 그룹도 검색일에 할일을 생성하지 않았음을 확인한다.")
    void collectionFilteredByDateTestNormal_byGroup_emptyResult() {

        // given
        Member member = memberRepository.save(new Member("test@abc.co.kr", "13d^3ea#"));
        Plan plan = planRepository.save(new Plan("plan1", LocalDate.of(2023, 7, 19), LocalDate.of(2023, 7, 31), member, PlanStatus.NOW));
        GroupResDto groupResDto1 = groupService.save(new GroupReqDto("title1", 3, makeArrToList("화", "목"), plan.getId()));
        GroupResDto groupResDto2 = groupService.save(new GroupReqDto("title2", 1, makeArrToList(), plan.getId()));
        GroupResDto groupResDto3 = groupService.save(new GroupReqDto("title3", 2, makeArrToList("2"), plan.getId()));
        LocalDate dateKey = LocalDate.of(2023, 7, 18);

        // when - then
        assertThat(checkboxService.allByGroup(groupResDto1.getId(), dateKey).size()).isEqualTo(0);
        assertThat(checkboxService.allByGroup(groupResDto2.getId(), dateKey).size()).isEqualTo(0);
        assertThat(checkboxService.allByGroup(groupResDto3.getId(), dateKey).size()).isEqualTo(0);

    }

    @Test
    @DisplayName("일별 컬렉션 정상 조회 - 일정 기준 - 등록한 일일 할일(1)과 그룹 할일(3)이 모두 검색일에 조회된다.")
    void collectionFilteredByDateTestNormal_byPlan_allMatchedWithKey() {

        // given
        Member member = memberRepository.save(new Member("test@abc.co.kr", "13d^3ea#"));
        PlanResDto planResDto = planService.save(new PlanReqDto("plan", LocalDate.of(2023, 7, 19), LocalDate.of(2023, 7, 31), member.getId()));
        Long planId = planResDto.getId();

        // 일일 할일 등록
        checkboxService.save(new CheckboxReqDto("title", planId, LocalDate.of(2023, 7, 23)));

        // 그룹 할일 등록
        groupService.save(new GroupReqDto("title1", 3, makeArrToList("화", "목", "일"), planId));
        groupService.save(new GroupReqDto("title2", 1, makeArrToList(), planId));
        groupService.save(new GroupReqDto("title3", 2, makeArrToList("2"), planId));

        LocalDate dateKey = LocalDate.of(2023, 7, 23);

        // when - then
        assertThat(checkboxService.allByPlan(planId, dateKey).size()).isEqualTo(4);

    }

    @Test
    @DisplayName("일별 컬렉션 정상 조회 - 일정 기준 - 등록한 일일 할일(3)과 그룹 할일(3) 중 일부만 검색일에 조회된다.")
    void collectionFilteredByDateTestNormal_byPlan_partOfThemMatchedWithKey() {

        // given
        Member member = memberRepository.save(new Member("test@abc.co.kr", "13d^3ea#"));
        PlanResDto planResDto = planService.save(new PlanReqDto("plan", LocalDate.of(2023, 7, 19), LocalDate.of(2023, 7, 31), member.getId()));
        Long planId = planResDto.getId();

        // 일일 할일 등록
        checkboxService.save(new CheckboxReqDto("title", planId, LocalDate.of(2023, 7, 19)));
        checkboxService.save(new CheckboxReqDto("title", planId, LocalDate.of(2023, 7, 23)));
        checkboxService.save(new CheckboxReqDto("title", planId, LocalDate.of(2023, 7, 27)));

        // 그룹 할일 등록
        groupService.save(new GroupReqDto("title1", 3, makeArrToList("화", "목", "일"), planId));
        groupService.save(new GroupReqDto("title2", 1, makeArrToList(), planId));
        groupService.save(new GroupReqDto("title3", 2, makeArrToList("2"), planId));

        LocalDate dateKey = LocalDate.of(2023, 7, 23);

        // when - then
        assertThat(checkboxService.allByPlan(planId, dateKey).size()).isEqualTo(4);

    }

    @Test
    @DisplayName("일별 컬렉션 정상 조회 - 일정 기준 - 등록한 일일 할일(3)과 그룹 할일(3) 중 어떤 할일도 검색일에 조회되지 않는다.")
    void collectionFilteredByDateTestNormal_byPlan_emptyResult() {

        // given
        Member member = memberRepository.save(new Member("test@abc.co.kr", "13d^3ea#"));
        PlanResDto planResDto = planService.save(new PlanReqDto("plan", LocalDate.of(2023, 7, 19), LocalDate.of(2023, 7, 31), member.getId()));
        Long planId = planResDto.getId();

        // 일일 할일 등록
        checkboxService.save(new CheckboxReqDto("title", planId, LocalDate.of(2023, 7, 19)));
        checkboxService.save(new CheckboxReqDto("title", planId, LocalDate.of(2023, 7, 23)));
        checkboxService.save(new CheckboxReqDto("title", planId, LocalDate.of(2023, 7, 27)));

        // 그룹 할일 등록
        groupService.save(new GroupReqDto("title1", 3, makeArrToList("화", "목", "일"), planId));
        groupService.save(new GroupReqDto("title2", 1, makeArrToList(), planId));
        groupService.save(new GroupReqDto("title3", 2, makeArrToList("2"), planId));

        LocalDate dateKey = LocalDate.of(2023, 7, 18);

        // when - then
        assertThat(checkboxService.allByPlan(planId, dateKey).size()).isEqualTo(0);

    }

    /**
     * 기간 컬렉션 조회 테스트 메이커
     * @param standard group or plan
     * @param id groupId or planId
     * @param searchStart 검색 시작일
     * @param searchEnd 검색 종료일
     * @param expectedCnt 조회 결과 리스트 사이즈의 예측값
     * @param expectedList 조회 결과 리스트에 들어 있어야 하는 날짜의 리스트
     */
    void assertSearchDates(String standard, Long id, LocalDate searchStart, LocalDate searchEnd, int expectedCnt, LocalDate... expectedList) {

        // when
        List<CheckboxResDto> filteredAll = (standard == "group"
                ? checkboxService.allByGroup(id, searchStart, searchEnd)
                : checkboxService.allByPlan(id, searchStart, searchEnd));

        // then
        assertThat(filteredAll.size()).isEqualTo(expectedCnt);
        assertThat(filteredAll.stream().map(CheckboxResDto::getDate)).containsOnly(expectedList);

    }


    /**
     * 할일 기간 컬렉션 조회 관련 테스트
     * 기간 컬렉션이란 검색 시작일과 검색 종료일 사이에 date 필드가 들어가는 할일만 조회한 컬렉션을 의마한다.
     *
     * 일정을 기준으로 조회하는 경우 : 일일 할일과 그룹 할일 중에서 검색 범위 안에 date가 들어가는 할일을 조회한다.
     * @see CheckboxServiceTestImpl#allByPlan(Long)
     *
     * 그룹을 기준으로 조회하는 경우 : 그룹 할일 중에서 검색 범위 안에 date가 들어가는 할일을 조회한다.
     * @see CheckboxServiceTestImpl#allByGroup(Long)
     */
    @Test
    @DisplayName("기간 컬렉션 정상 조회 - 그룹 기준 - 그룹 할일 중에서 검색 범위 안에 date가 들어가는 할일을 조회하고, 결과의 개수가 예측값과 같은지 확인하며, " +
            "expectedList에 지정한 날짜만 결과 리스트에 들어 있는지 확인한다.")
    void collectionFilteredByDateRangeTestNormal_byGroup() {

        // given
        Member member = memberRepository.save(new Member("test@abc.co.kr", "13d^3ea#"));
        PlanResDto planResDto = planService.save(new PlanReqDto("plan", LocalDate.of(2023, 7, 19), LocalDate.of(2023, 7, 31), member.getId()));
        Long planId = planResDto.getId();
        GroupResDto groupResDto = groupService.save(new GroupReqDto("title", 3, makeArrToList("화", "목", "일"), planId));
        Long groupId = groupResDto.getId();

        // when - then
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
    @DisplayName("기간 컬렉션 비정상 조회 - 그룹 기준 - 기준으로 삼을 그룹이 없는 경우 ResourceNotFoundException을 던지는지 확인한다.")
    void collectionFilteredByDateRangeTestAbnormal_byGroup_resourceNotFound() {

        assertThrows(ResourceNotFoundException.class, () -> checkboxService.allByGroup(Long.MAX_VALUE, LocalDate.of(2023, 7, 23), LocalDate.of(2023, 7, 25)));
    }

    @Test
    @DisplayName("기간 컬렉션 정상 조회 - 일정 기준 - 일일 할일과 그룹 할일 중에서 검색 범위에 date가 들어가는 할일을 조회하고, 결과의 개수가 예측값과 같은지 확인하며, " +
            "expectedList에 지정한 날짜만 결과 리스트에 들어있는지 확인한다.")
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
    @DisplayName("기간 비정상 조회 - 일정 기준 - 기준으로 삼을 일정이 없는 경우 ResourceNotFoundException을 던지는지 확인한다.")
    void collectionFilteredByDateRangeTestAbnormal_byPlan_resourceNotFound() {

        assertThrows(ResourceNotFoundException.class, () -> checkboxService.allByPlan(Long.MAX_VALUE, LocalDate.of(2023, 7, 19), LocalDate.of(2023, 7, 23)));

    }

}
