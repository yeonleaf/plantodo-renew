package yeonleaf.plantodo.unit.service;

import org.junit.jupiter.api.*;

import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import yeonleaf.plantodo.ServiceTestConfig;
import yeonleaf.plantodo.controller.PlanController;
import yeonleaf.plantodo.converter.RepInToOutConverter;
import yeonleaf.plantodo.converter.RepOutToInConverter;
import yeonleaf.plantodo.domain.*;
import yeonleaf.plantodo.dto.*;
import yeonleaf.plantodo.exceptions.ResourceNotFoundException;
import yeonleaf.plantodo.repository.*;
import yeonleaf.plantodo.service.*;
import yeonleaf.plantodo.util.DateRange;
import yeonleaf.plantodo.validator.RepInputValidator;

import java.time.LocalDate;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.stream.IntStream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;

/**
 * target : {@link PlanServiceTestImpl}에 있는 모든 메소드
 * target description : {@link PlanService}의 테스트용 구현체.
 *                      {@link MemoryRepository}를 상속받은 서브클래스들을 Repository로 주입받음 (DB를 메모리로 대신)
 */
@ExtendWith(SpringExtension.class)
@ContextConfiguration(classes = {ServiceTestConfig.class})
public class PlanServiceUnitTest {

    @Autowired
    private MemberServiceTestImpl memberService;

    @Autowired
    private PlanServiceTestImpl planService;

    @Autowired
    private GroupServiceTestImpl groupService;

    @Autowired
    private MemoryMemberRepository memberRepository;

    @Autowired
    private MemoryPlanRepository planRepository;

    @Autowired
    private MemoryGroupRepository groupRepository;

    @Autowired
    private MemoryRepetitionRepository repetitionRepository;

    @Autowired
    private MemoryCheckboxRepository checkboxRepository;

    @Autowired
    private CheckboxService checkboxService;


    /**
     * 테스트 종료 후 메모리에 저장된 데이터를 모두 삭제해서 롤백
     * (DuplicatedMemberException 발생 방지)
     */
    @AfterEach
    void clear() {
        memberRepository.clear();
        planRepository.clear();
        groupRepository.clear();
        checkboxRepository.clear();
        repetitionRepository.clear();
    }


    /**
     * 등록 메소드 관련 테스트
     * @see PlanServiceTestImpl#save(PlanReqDto)
     */
    @Test
    @DisplayName("정상 등록 - repOption = 0인 group 하나가 생성되어야 한다.")
    void saveTestNormal() {

        // given
        MemberResDto member = memberService.save(new MemberReqDto("test@abc.co.kr", "3s1@adf2"));
        PlanResDto plan = planService.save(new PlanReqDto("title", LocalDate.now(), LocalDate.now().plusDays(3), member.getId()));

        // when
        List<Group> groups = groupRepository.findByPlanId(plan.getId());

        // then
        assertThat(groups.size()).isEqualTo(1);
        assertThat(groups.get(0).getRepetition().getRepOption()).isEqualTo(0);

    }


    /**
     * 단건 조회 메소드 관련 테스트
     * @see PlanServiceTestImpl#one(Long)
     */
    @Test
    @DisplayName("정상 조회 - id가 있는 PlanResDto 객체를 리턴하는지 확인한다.")
    void oneTestNormal() {

        // given
        MemberResDto member = memberService.save(new MemberReqDto("test@abc.co.kr", "3s1@adf2"));
        PlanResDto plan = planService.save(new PlanReqDto("title", LocalDate.now(), LocalDate.now().plusDays(3), member.getId()));

        // when
        PlanResDto findPlan = planService.one(plan.getId());

        // then
        assertThat(findPlan.getId()).isEqualTo(plan.getId());
        assertThat(findPlan.getStart()).isEqualTo(plan.getStart());
        assertThat(findPlan.getEnd()).isEqualTo(plan.getEnd());
        assertThat(findPlan.getStatus()).isEqualTo(plan.getStatus());

    }

    @Test
    @DisplayName("정상 조회 - 이미 기간이 지난 Plan을 저장했을 때, 조회 후 status가 PAST로 변경되었는지 확인한다.")
    void oneTestNormal_planBecomesPast() {

        // given
        MemberResDto member = memberService.save(new MemberReqDto("test@abc.co.kr", "3s1@adf2"));
        PlanResDto plan = planService.save(new PlanReqDto("title", LocalDate.of(2023, 7, 18), LocalDate.of(2023, 7, 24), member.getId()));

        // when
        PlanResDto findPlan = planService.one(plan.getId());

        // then
        assertThat(findPlan.getId()).isEqualTo(plan.getId());
        assertThat(findPlan.getStart()).isEqualTo(plan.getStart());
        assertThat(findPlan.getEnd()).isEqualTo(plan.getEnd());
        assertThat(findPlan.getStatus()).isEqualTo(PlanStatus.PAST);

    }

    @Test
    @DisplayName("비정상 조회 - 대상이 없는 경우 ResourceNotFoundException을 던지는지 확인한다.")
    void oneTestAbnormal() {

        // given - when - then
        assertThrows(ResourceNotFoundException.class, () -> planService.one(Long.MAX_VALUE));

    }

    /**
     * 수정 메소드 관련 테스트
     * @see PlanServiceTestImpl#update(PlanUpdateReqDto)
     */
    @Test
    @DisplayName("정상 타이틀 수정 - 타이틀이 정상적으로 변경되었는지 확인한다.")
    void updateTitleTestNormal() {

        // given
        MemberResDto member = memberService.save(new MemberReqDto("test@abc.co.kr", "3s1@adf2"));
        PlanResDto plan = planService.save(new PlanReqDto("title", LocalDate.now(), LocalDate.now().plusDays(3), member.getId()));
        groupService.save(new GroupReqDto("group", 1, List.of(), plan.getId()));

        // when
        planService.update(new PlanUpdateReqDto(plan.getId(), "updatedTitle", plan.getStart(), plan.getEnd()));

        // then
        Plan findPlan = planRepository.findById(plan.getId()).orElseThrow(ResourceNotFoundException::new);
        assertThat(findPlan.getTitle()).isEqualTo("updatedTitle");

    }

    @Test
    @DisplayName("비정상 타이틀 수정 - 대상이 없는 경우 ResourceNotFoundException을 던지는지 확인한다.")
    void updateTitleTestAbnormal() {

        // given - when - then
        assertThrows(ResourceNotFoundException.class, () -> planService.update(
                new PlanUpdateReqDto(Long.MAX_VALUE, "updatedTitle",
                        LocalDate.of(2023, 7, 18), LocalDate.of(2023, 7, 18))));

    }

    /**
     * 테스트 메이커
     * 일정의 기간을 수정하는 테스트를 작성하는 메소드
     * @param repOption (int) 할 일이 반복되는 양상을 설정하는 옵션
     *                  1(매일 반복), 2(기간 반복), 3(요일 반복)
     * @param repValue (List<String>) 할 일이 반복되는 주기를 설정하는 옵션
     * @param newStart 변경 후 일정 시작일
     * @param newEnd 변경 후 일정 종료일
     * @return 일정 변경 후 Checkbox.date를 리스트로 담아 리턴한다.
     */
    private List<LocalDate> makeDateRangeTest(int repOption, List<String> repValue, LocalDate newStart, LocalDate newEnd) {

        PlanResDto oldPlan = makeOldPlan();
        GroupResDto group = groupService.save(new GroupReqDto("group", repOption, repValue, oldPlan.getId()));

        PlanUpdateReqDto planUpdateReqDto = new PlanUpdateReqDto(oldPlan.getId(), oldPlan.getTitle(), newStart, newEnd);
        planService.update(planUpdateReqDto);

        return checkboxRepository.findByGroupId(group.getId()).stream().map(Checkbox::getDate).toList();

    }

    /**
     * 시작일이 2023년 7월 18일, 종료일이 2023년 7월 25일로 고정된 변경 전 일정을 생성하는 메소드
     * 기간 수정 메소드 관련 테스트에서는 수정 전 일정은 고정하고 수정 후 일정 기간을 변경하면서 경우의 수를 확인한다.
     */
    private PlanResDto makeOldPlan() {
        MemberResDto member = memberService.save(new MemberReqDto("test@abc.co.kr", "3s1@adf2"));
        return planService.save(new PlanReqDto("title", LocalDate.of(2023, 7, 18), LocalDate.of(2023, 7, 25), member.getId()));
    }


    /**
     * 기간 수정 메소드 관련 테스트
     * 수정 후 올바른 일자에 Checkbox가 생성되었는지 확인한다.
     * @see PlanServiceTestImpl#update(PlanUpdateReqDto)
     */
    @Test
    @DisplayName("정상 기간 수정 - repOption = 1 - oldStart < newStart < newEnd < oldEnd")
    void updateDateRangeTestNormal_repOption1_allDatesUnique_1() {

        // given - when
        List<LocalDate> dateResult = makeDateRangeTest(1, List.of(), LocalDate.of(2023, 7, 20), LocalDate.of(2023, 7, 23));

        // then
        assertThat(dateResult).containsOnly(
                LocalDate.of(2023, 7, 20),
                LocalDate.of(2023, 7, 21),
                LocalDate.of(2023, 7, 22),
                LocalDate.of(2023, 7, 23)
        );

    }

    @Test
    @DisplayName("정상 기간 수정 - repOption = 1 - newStart < oldStart < newEnd < oldEnd의 경우 " +
            "newStart ~ oldStart-1 사이의 할 일은 삭제, newEnd+1 ~ oldEnd 사이에는 할 일을 새로 생성한다.")
    void updateDateRangeTestNormal_repOption1_allDatesUnique_2() {

        // given - when
        List<LocalDate> dateResult = makeDateRangeTest(1, List.of(), LocalDate.of(2023, 7, 16), LocalDate.of(2023, 7, 23));

        // then
        IntStream.rangeClosed(16, 23).forEach(i -> assertThat(dateResult).contains(LocalDate.of(2023, 7, i)));
        IntStream.rangeClosed(24, 25).forEach(i -> assertThat(dateResult).doesNotContain(LocalDate.of(2023, 7, i)));

    }

    @Test
    @DisplayName("정상 기간 수정 - repOption = 1 - newStart < oldStart < oldEnd < newEnd의 경우 " +
            "newStart ~ newEnd 사이의 날짜에 할 일이 있어야 한다.")
    void updateDateRangeTestNormal_repOption1_allDatesUnique_3() {

        // given - when
        List<LocalDate> dateResult = makeDateRangeTest(1, List.of(), LocalDate.of(2023, 7, 16), LocalDate.of(2023, 7, 29));

        // then
        IntStream.rangeClosed(16, 29).forEach(i -> assertThat(dateResult).contains(LocalDate.of(2023, 7, i)));

    }

    @Test
    @DisplayName("정상 기간 수정 - repOption = 1 - oldStart < newStart < oldEnd < newEnd의 경우 " +
            "oldStart ~ newEnd 사이의 날짜에 Checkbox가 있어야 한다.")
    void updateDateRangeTestNormal_repOption1_allDatesUnique_4() {

        // given - when
        List<LocalDate> dateResult = makeDateRangeTest(1, List.of(), LocalDate.of(2023, 7, 23), LocalDate.of(2023, 7, 29));

        // then
        IntStream.rangeClosed(23, 29).forEach(i -> assertThat(dateResult).contains(LocalDate.of(2023, 7, i)));
        IntStream.rangeClosed(18, 22).forEach(i -> assertThat(dateResult).doesNotContain(LocalDate.of(2023, 7, i)));

    }

    @Test
    @DisplayName("정상 기간 수정 - repOption = 1 - newStart < newEnd < oldStart < oldEnd의 경우 " +
            "newStart ~ newEnd 사이에는 할 일이 있어야 하고 oldStart ~ oldEnd 사이에는 할 일이 없어야 한다.")
    void updateDateRangeTestNormal_repOption1_allDatesUnique_5() {

        // given - when
        List<LocalDate> dateResult = makeDateRangeTest(1, List.of(), LocalDate.of(2023, 7, 13), LocalDate.of(2023, 7, 16));

        // then
        IntStream.rangeClosed(13, 16).forEach(i -> assertThat(dateResult).contains(LocalDate.of(2023, 7, i)));
        IntStream.rangeClosed(17, 25).forEach(i -> assertThat(dateResult).doesNotContain(LocalDate.of(2023, 7, i)));

    }

    @Test
    @DisplayName("정상 기간 수정 - repOption = 1 - oldStart < oldEnd < newStart < newEnd의 경우 " +
            "newStart ~ newEnd 사이에는 할 일이 있어야 하고 oldStart ~ oldEnd 사이에는 할 일이 없어야 한다.")
    void updateDateRangeTestNormal_repOption1_allDatesUnique_6() {

        // given - when
        List<LocalDate> dateResult = makeDateRangeTest(1, List.of(), LocalDate.of(2023, 7, 29), LocalDate.of(2023, 7, 31));

        // then
        IntStream.rangeClosed(29, 31).forEach(i -> assertThat(dateResult).contains(LocalDate.of(2023, 7, i)));
        IntStream.rangeClosed(18, 28).forEach(i -> assertThat(dateResult).doesNotContain(LocalDate.of(2023, 7, i)));

    }

    @Test
    @DisplayName("정상 기간 수정 - repOption = 1 - oldStart == newStart && oldEnd == newEnd의 경우 " +
            "oldStart에만 할 일이 있어야 한다.")
    void updateDateRangeTestNormal_repOption1_datesArentChanged() {

        // given - when
        List<LocalDate> dateResult = makeDateRangeTest(1, List.of(), LocalDate.of(2023, 7, 18), LocalDate.of(2023, 7, 25));

        // then
        IntStream.rangeClosed(18, 25).forEach(i -> assertThat(dateResult).contains(LocalDate.of(2023, 7, i)));

    }

    @Test
    @DisplayName("정상 기간 수정 - repOption = 1 - oldStart = newStart && oldEnd < newEnd의 경우 " +
            "oldStart ~ newEnd 사이에 할 일이 있어야 한다.")
    void updateDateRangeTestNormal_repOption1_newEndGreaterThanOldEnd() {

        // given - when
        List<LocalDate> dateResult = makeDateRangeTest(1, List.of(), LocalDate.of(2023, 7, 18), LocalDate.of(2023, 7, 29));

        // then
        IntStream.rangeClosed(18, 29).forEach(i -> assertThat(dateResult).contains(LocalDate.of(2023, 7, i)));

    }

    @Test
    @DisplayName("정상 기간 수정 - repOption = 1 - oldStart = newStart, oldEnd > newEnd의 경우 " +
            "oldStart ~ newEnd 사이에 할 일이 있어야 하고 newStart+1 ~ oldEnd 사이에는 할 일이 없어야 한다.")
    void updateDateRangeTestNormal_repOption1_oldEndGreaterThanNewEnd() {

        // given - when
        List<LocalDate> dateResult = makeDateRangeTest(1, List.of(), LocalDate.of(2023, 7, 18), LocalDate.of(2023, 7, 23));

        // then
        IntStream.rangeClosed(18, 23).forEach(i -> assertThat(dateResult).contains(LocalDate.of(2023, 7, i)));
        IntStream.rangeClosed(24, 25).forEach(i -> assertThat(dateResult).doesNotContain(LocalDate.of(2023, 7, i)));

    }

    @Test
    @DisplayName("정상 기간 수정 - repOption = 1 - oldStart < newStart, oldEnd = newEnd의 경우 " +
            "oldStart ~ newStart-1 사이에 할 일이 없어야 하고 newStart ~ newEnd 사이에 할 일이 있어야 한다.")
    void updateDateRangeTestNormal_repOption1_newStartGreaterThanOldStart() {

        // given - when
        List<LocalDate> dateResult = makeDateRangeTest(1, List.of(), LocalDate.of(2023, 7, 20), LocalDate.of(2023, 7, 25));

        // then
        IntStream.rangeClosed(20, 25).forEach(i -> assertThat(dateResult).contains(LocalDate.of(2023, 7, i)));
        IntStream.rangeClosed(18, 19).forEach(i -> assertThat(dateResult).doesNotContain(LocalDate.of(2023, 7, i)));

    }

    @Test
    @DisplayName("정상 기간 수정 - repOption = 1 - oldStart > newStart, oldEnd = newEnd의 경우 " +
            "newStart ~ newEnd 사이에 할 일이 있어야 한다.")
    void updateDateRangeTestNormal_repOption1_oldStartGreaterThanNewStart() {

        // given - when
        List<LocalDate> dateResult = makeDateRangeTest(1, List.of(), LocalDate.of(2023, 7, 16), LocalDate.of(2023, 7, 25));

        // then
        IntStream.rangeClosed(16, 25).forEach(i -> assertThat(dateResult).contains(LocalDate.of(2023, 7, i)));

    }

    @Test
    @DisplayName("정상 기간 수정 - repOption = 2 - oldStart < newStart < newEnd < oldEnd의 경우 " +
            "oldStart ~ newStart-1, newEnd+1 ~ oldEnd 사이에는 할 일이 없어야 하고 newStart ~ newEnd 사이에는 할 일이 있어야 한다.")
    void updateDateRangeTestNormal_repOption2_allDatesUnique_1() {

        // given - when
        List<LocalDate> dateResult = makeDateRangeTest(2, List.of("3"), LocalDate.of(2023, 7, 20), LocalDate.of(2023, 7, 23));

        // then
        assertThat(dateResult).containsOnly(
                LocalDate.of(2023, 7, 20),
                LocalDate.of(2023, 7, 23)
        );

    }

    @Test
    @DisplayName("정상 기간 수정 - repOption = 2 - newStart < oldStart < newEnd < oldEnd의 경우 " +
            "newEnd+1 ~ oldEnd 사이에는 할 일이 없어야 하고 newStart ~ newEnd 사이에는 3일 간격으로 할 일이 있어야 한다.")
    void updateDateRangeTestNormal_repOption2_allDatesUnique_2() {

        // given - when
        List<LocalDate> dateResult = makeDateRangeTest(2, List.of("3"), LocalDate.of(2023, 7, 16), LocalDate.of(2023, 7, 23));

        // then
        IntStream.iterate(16, i -> i + 3).limit(3).forEach(i -> assertThat(dateResult).contains(LocalDate.of(2023, 7, i)));

    }

    @Test
    @DisplayName("정상 기간 수정 - repOption = 2 - newStart < oldStart < oldEnd < newEnd의 경우 " +
            "newStart ~ newEnd 사이에 3일 간격으로 할 일이 있어야 한다.")
    void updateDateRangeTestNormal_repOption2_allDatesUnique_3() {

        // given - when
        List<LocalDate> dateResult = makeDateRangeTest(2, List.of("3"), LocalDate.of(2023, 7, 16), LocalDate.of(2023, 7, 29));

        // then
        IntStream.iterate(16, i -> i + 3).limit(5).forEach(i -> assertThat(dateResult).contains(LocalDate.of(2023, 7, i)));

    }

    @Test
    @DisplayName("정상 기간 수정 - repOption = 2 - oldStart < newStart < oldEnd < newEnd의 경우 " +
            "oldStart ~ newStart-1 사이에 할 일이 없어야 하고 newStart ~ newEnd 사이에 3일 간격으로 할 일이 있어야 한다.")
    void updateDateRangeTestNormal_repOption2_allDatesUnique_4() {

        // given - when
        List<LocalDate> dateResult = makeDateRangeTest(2, List.of("3"), LocalDate.of(2023, 7, 23), LocalDate.of(2023, 7, 29));

        // then
        IntStream.iterate(23, i -> i + 3).limit(3).forEach(i -> assertThat(dateResult).contains(LocalDate.of(2023, 7, i)));

    }

    @Test
    @DisplayName("정상 기간 수정 - repOption = 2 - newStart < newEnd < oldStart < oldEnd의 경우 " +
            "oldStart ~ oldEnd 사이에는 할 일이 없어야 하고 newStart ~ newEnd 사이에는 3일 간격으로 할 일이 있어야 한다.")
    void updateDateRangeTestNormal_repOption2_allDatesUnique_5() {

        // given - when
        List<LocalDate> dateResult = makeDateRangeTest(2, List.of("3"), LocalDate.of(2023, 7, 13), LocalDate.of(2023, 7, 16));

        // then
        assertThat(dateResult).containsOnly(
                LocalDate.of(2023, 7, 13),
                LocalDate.of(2023, 7, 16)
        );

    }

    @Test
    @DisplayName("정상 기간 수정 - repOption = 2 - oldStart < oldEnd < newStart < newEnd의 경우 " +
            "oldStart ~ oldEnd 사이에 할 일이 없어야 하고 newStart ~ newEnd 사이에는 3일 간격으로 할 일이 있어야 한다.")
    void updateDateRangeTestNormal_repOption2_allDatesUnique_6() {

        // given - when
        List<LocalDate> dateResult = makeDateRangeTest(2, List.of("3"), LocalDate.of(2023, 7, 29), LocalDate.of(2023, 7, 31));

        // then
        assertThat(dateResult).containsOnly(
                LocalDate.of(2023, 7, 29)
        );

    }

    @Test
    @DisplayName("정상 기간 수정 - repOption = 2 - oldStart == newStart && oldEnd == newEnd의 경우 " +
            "oldStart ~ oldEnd 사이에 3일 간격으로 할 일이 있어야 한다.")
    void updateDateRangeTestNormal_repOption2_datesArentChanged() {

        // given - when
        List<LocalDate> dateResult = makeDateRangeTest(2, List.of("3"), LocalDate.of(2023, 7, 18), LocalDate.of(2023, 7, 25));

        // then
        assertThat(dateResult).containsOnly(
                LocalDate.of(2023, 7, 18),
                LocalDate.of(2023, 7, 21),
                LocalDate.of(2023, 7, 24)
        );

    }

    @Test
    @DisplayName("정상 기간 수정 - repOption = 2 - oldStart = newStart && oldEnd < newEnd의 경우 " +
            "newStart ~ newEnd 사이에 3일 간격으로 할 일이 있어야 한다.")
    void updateDateRangeTestNormal_repOption2_newEndGreaterThanOldEnd() {

        // given - when
        List<LocalDate> dateResult = makeDateRangeTest(2, List.of("3"), LocalDate.of(2023, 7, 18), LocalDate.of(2023, 7, 29));

        // then
        IntStream.iterate(18, i -> i + 3).limit(4).forEach(i -> assertThat(dateResult).contains(LocalDate.of(2023, 7, i)));

    }

    @Test
    @DisplayName("정상 기간 수정 - repOption = 2 - oldStart = newStart, oldEnd > newEnd의 경우 " +
            "newStart ~ newEnd 사이에 3일 간격으로 할 일이 있어야 하고 newEnd+1 ~ oldEnd 사이에는 할 일이 없어야 한다.")
    void updateDateRangeTestNormal_repOption2_oldEndGreaterThanNewEnd() {

        // given - when
        List<LocalDate> dateResult = makeDateRangeTest(2, List.of("3"), LocalDate.of(2023, 7, 18), LocalDate.of(2023, 7, 23));

        // then
        assertThat(dateResult).containsOnly(
                LocalDate.of(2023, 7, 18),
                LocalDate.of(2023, 7, 21)
        );

    }

    @Test
    @DisplayName("정상 기간 수정 - repOption = 2 - oldStart < newStart, oldEnd = newEnd의 경우 " +
            "newStart ~ newEnd 사이에 3일 간격으로 할 일이 있어야 하고 oldStart ~ newStart-1 사이에는 할 일이 없어야 한다.")
    void updateDateRangeTestNormal_repOption2_newStartGreaterThanOldStart() {

        // given - when
        List<LocalDate> dateResult = makeDateRangeTest(2, List.of("3"), LocalDate.of(2023, 7, 20), LocalDate.of(2023, 7, 25));

        // then
        assertThat(dateResult).containsOnly(
                LocalDate.of(2023, 7, 20),
                LocalDate.of(2023, 7, 23)
        );

    }

    @Test
    @DisplayName("정상 기간 수정 - repOption = 2 - oldStart > newStart, oldEnd = newEnd의 경우 " +
            "newStart ~ newEnd 사이에 3일 간격으로 할 일이 있어야 한다.")
    void updateDateRangeTestNormal_repOption2_oldStartGreaterThanNewStart() {

        // given - when
        List<LocalDate> dateResult = makeDateRangeTest(2, List.of("3"), LocalDate.of(2023, 7, 16), LocalDate.of(2023, 7, 25));

        // then
        assertThat(dateResult).containsOnly(
                LocalDate.of(2023, 7, 16),
                LocalDate.of(2023, 7, 19),
                LocalDate.of(2023, 7, 22),
                LocalDate.of(2023, 7, 25)
        );

    }

    @Test
    @DisplayName("정상 기간 수정 - repOption = 3 - oldStart < newStart < newEnd < oldEnd의 경우 " +
            "newStart ~ newEnd 사이의 날짜 중 월, 수, 금요일에 할 일이 있어야 하며 oldStart ~ newStart-1, newEnd+1 ~ oldEnd에 할 일이 없어야 한다.")
    void updateDateRangeTestNormal_repOption3_allDatesUnique_1() {

        // given - when
        List<LocalDate> dateResult = makeDateRangeTest(3, List.of("월", "수", "금"), LocalDate.of(2023, 7, 20), LocalDate.of(2023, 7, 23));

        // then
        assertThat(dateResult).containsOnly(
                LocalDate.of(2023, 7, 21)
        );

    }

    @Test
    @DisplayName("정상 기간 수정 - repOption = 3 - newStart < oldStart < newEnd < oldEnd의 경우 " +
            "newStart ~ newEnd 사이의 날짜 중 월, 수, 금요일에 할 일이 있어야 하며 newEnd+1 ~ oldEnd 사이에 할 일이 없어야 한다.")
    void updateDateRangeTestNormal_repOption3_allDatesUnique_2() {

        // given - when
        List<LocalDate> dateResult = makeDateRangeTest(3, List.of("월", "수", "금"), LocalDate.of(2023, 7, 16), LocalDate.of(2023, 7, 23));

        // then
        assertThat(dateResult).containsOnly(
                LocalDate.of(2023, 7, 17),
                LocalDate.of(2023, 7, 19),
                LocalDate.of(2023, 7, 21)
        );

    }

    @Test
    @DisplayName("정상 기간 수정 - repOption = 3 - newStart < oldStart < oldEnd < newEnd의 경우 " +
            "newStart ~ newEnd 사이의 날짜 중 월, 수, 금요일에 할 일이 있어야 한다.")
    void updateDateRangeTestNormal_repOption3_allDatesUnique_3() {

        // given - when
        List<LocalDate> dateResult = makeDateRangeTest(3, List.of("월", "수", "금"), LocalDate.of(2023, 7, 16), LocalDate.of(2023, 7, 29));

        // then
        assertThat(dateResult).containsOnly(
                LocalDate.of(2023, 7, 17),
                LocalDate.of(2023, 7, 19),
                LocalDate.of(2023, 7, 21),
                LocalDate.of(2023, 7, 24),
                LocalDate.of(2023, 7, 26),
                LocalDate.of(2023, 7, 28)
        );

    }

    @Test
    @DisplayName("정상 기간 수정 - repOption = 3 - oldStart < newStart < oldEnd < newEnd의 경우 " +
            "newStart ~ newEnd 사이의 날짜 중 월, 수, 금요일에 할 일이 있어야 하며 oldStart ~ newStart-1 사이에는 할 일이 없어야 한다.")
    void updateDateRangeTestNormal_repOption3_allDatesUnique_4() {

        // given - when
        List<LocalDate> dateResult = makeDateRangeTest(3, List.of("월", "수", "금"), LocalDate.of(2023, 7, 23), LocalDate.of(2023, 7, 29));

        // then
        assertThat(dateResult).containsOnly(
                LocalDate.of(2023, 7, 24),
                LocalDate.of(2023, 7, 26),
                LocalDate.of(2023, 7, 28)
        );
    }

    @Test
    @DisplayName("정상 기간 수정 - repOption = 3 - newStart < newEnd < oldStart < oldEnd의 경우 " +
            "newStart ~ newEnd 사이의 날짜 중 월, 수, 금요일에 할 일이 있어야 하고 oldStart ~ oldEnd 사이에는 할 일이 없어야 한다.")
    void updateDateRangeTestNormal_repOption3_allDatesUnique_5() {

        // given - when
        List<LocalDate> dateResult = makeDateRangeTest(3, List.of("월", "수", "금"), LocalDate.of(2023, 7, 13), LocalDate.of(2023, 7, 16));

        // then
        assertThat(dateResult).containsOnly(
                LocalDate.of(2023, 7, 14)
        );

    }

    @Test
    @DisplayName("정상 기간 수정 - repOption = 3 - oldStart < oldEnd < newStart < newEnd의 경우 " +
            "newStart ~ newEnd 사이의 날짜 중 월, 수, 금요일에 할 일이 있어야 하고 oldStart ~ oldEnd 사이에는 할 일이 없어야 한다.")
    void updateDateRangeTestNormal_repOption3_allDatesUnique_6() {

        // given - when
        List<LocalDate> dateResult = makeDateRangeTest(3, List.of("월", "수", "금"), LocalDate.of(2023, 7, 29), LocalDate.of(2023, 7, 31));

        // then
        assertThat(dateResult).containsOnly(
                LocalDate.of(2023, 7, 31)
        );

    }

    @Test
    @DisplayName("정상 기간 수정 - repOption = 3 - oldStart == newStart && oldEnd == newEnd의 경우 " +
            "newStart ~ newEnd 사이의 날짜 중 월, 수, 금요일에 할 일이 있어야 한다.")
    void updateDateRangeTestNormal_repOption3_datesArentChanged() {

        // given - when
        List<LocalDate> dateResult = makeDateRangeTest(3, List.of("월", "수", "금"), LocalDate.of(2023, 7, 18), LocalDate.of(2023, 7, 25));

        // then
        assertThat(dateResult).containsOnly(
                LocalDate.of(2023, 7, 19),
                LocalDate.of(2023, 7, 21),
                LocalDate.of(2023, 7, 24)
        );

    }

    @Test
    @DisplayName("정상 기간 수정 - repOption = 3 - oldStart = newStart && oldEnd < newEnd의 경우 " +
            "newStart ~ newEnd 사이의 날짜 중 월, 수, 금요일에 할 일이 있어야 한다.")
    void updateDateRangeTestNormal_repOption3_newEndGreaterThanOldEnd() {

        // given - when
        List<LocalDate> dateResult = makeDateRangeTest(3, List.of("월", "수", "금"), LocalDate.of(2023, 7, 18), LocalDate.of(2023, 7, 29));

        // then
        assertThat(dateResult).containsOnly(
                LocalDate.of(2023, 7, 19),
                LocalDate.of(2023, 7, 21),
                LocalDate.of(2023, 7, 24),
                LocalDate.of(2023, 7, 26),
                LocalDate.of(2023, 7, 28)
        );

    }

    @Test
    @DisplayName("정상 기간 수정 - repOption = 3 - oldStart = newStart, oldEnd > newEnd의 경우 " +
            "newStart ~ newEnd 사이의 날짜 중 월, 수, 금요일에 할 일이 있어야 하고 newEnd+1 ~ oldEnd 사이에는 할 일이 없어야 한다.")
    void updateDateRangeTestNormal_repOption3_oldEndGreaterThanNewEnd() {

        // given - when
        List<LocalDate> dateResult = makeDateRangeTest(3, List.of("월", "수", "금"), LocalDate.of(2023, 7, 18), LocalDate.of(2023, 7, 23));

        // then
        assertThat(dateResult).containsOnly(
                LocalDate.of(2023, 7, 19),
                LocalDate.of(2023, 7, 21)
        );

    }

    @Test
    @DisplayName("정상 기간 수정 - repOption = 3L - oldStart < newStart, oldEnd = newEnd의 경우 " +
            "newStart ~ newEnd 사이의 날짜 중 월, 수, 금요일에 할 일이 있어야 하고 oldStart ~ newStart-1 사이에는 할 일이 없어야 한다.")
    void updateDateRangeTestNormal_repOption3_newStartGreaterThanOldStart() {

        // given - when
        List<LocalDate> dateResult = makeDateRangeTest(3, List.of("월", "수", "금"), LocalDate.of(2023, 7, 20), LocalDate.of(2023, 7, 25));

        // then
        assertThat(dateResult).containsOnly(
                LocalDate.of(2023, 7, 21),
                LocalDate.of(2023, 7, 24)
        );

    }

    @Test
    @DisplayName("정상 기간 수정 - repOption = 3L - oldStart > newStart, oldEnd = newEnd의 경우 " +
            "newStart ~ newEnd 사이의 날짜 중 월, 수, 금요일에 할 일이 있어야 한다.")
    void updateDateRangeTestNormal_repOption3_oldStartGreaterThanNewStart() {

        // given - when
        List<LocalDate> dateResult = makeDateRangeTest(3, List.of("월", "수", "금"), LocalDate.of(2023, 7, 16), LocalDate.of(2023, 7, 25));

        // then
        assertThat(dateResult).containsOnly(
                LocalDate.of(2023, 7, 17),
                LocalDate.of(2023, 7, 19),
                LocalDate.of(2023, 7, 21),
                LocalDate.of(2023, 7, 24)
        );

    }


    /**
     * 삭제 메소드 관련 테스트
     * @see PlanServiceTestImpl#delete(Long)
     *
     * 일정 삭제를 할 때
     * - 할일 그룹만 있는 경우
     * - 일일 할일만 있는 경우
     * - 할일 그룹과 일일 할일이 같이 있는 경우
     * - 할일 그룹과 일일 할일이 모두 없는 경우
     *
     * 네 가지 경우 다 일정을 삭제하고 연관된 할일을 조회했을 때 개수가 0이어야 한다.
     */
    @Test
    @DisplayName("정상 삭제 - 할일 그룹과 일일 할일 모두 없는 경우 일정을 삭제하고 연관된 할일을 조회했을 때 개수가 0이어야 한다.")
    void deleteTestNormal_noClientGroup_noDailyCheckboxes() {

        // given
        MemberResDto memberResDto = memberService.save(new MemberReqDto("test@abc.co.kr", "3s1@adf2"));
        PlanResDto planResDto = planService.save(new PlanReqDto("title", LocalDate.of(2023, 7, 18), LocalDate.of(2023, 7, 25), memberResDto.getId()));
        Long planId = planResDto.getId();

        // when
        planService.delete(planId);

        // then
        List<Group> groups = groupRepository.findByPlanId(planId);
        assertThat(groups).isEmpty();

    }

    @Test
    @DisplayName("정상 삭제 - 할일 그룹은 없고 일일 할일만 있는 경우 일정을 삭제하고 연관된 할일을 조회했을 때 개수가 0이어야 한다.")
    void deleteTestNormal_noClientGroup_dailyCheckboxes() {

        // given
        System.out.println(memberRepository.findByEmail("test@abc.co.kr").size());
        MemberResDto memberResDto = memberService.save(new MemberReqDto("test@abc.co.kr", "3s1@adf2"));
        PlanResDto planResDto = planService.save(new PlanReqDto("title", LocalDate.of(2023, 7, 18), LocalDate.of(2023, 7, 25), memberResDto.getId()));
        Long planId = planResDto.getId();

        CheckboxResDto checkboxResDto1 = checkboxService.save(new CheckboxReqDto("title1", planResDto.getId(), LocalDate.of(2023, 7, 18)));
        CheckboxResDto checkboxResDto2 = checkboxService.save(new CheckboxReqDto("title2", planResDto.getId(), LocalDate.of(2023, 7, 19)));
        CheckboxResDto checkboxResDto3 = checkboxService.save(new CheckboxReqDto("title3", planResDto.getId(), LocalDate.of(2023, 7, 20)));

        // when
        planService.delete(planId);

        // then
        Optional<Plan> findPlan = planRepository.findById(planId);
        assertThat(findPlan).isEmpty();

        List<Group> findGroup = groupRepository.findByPlanId(planId);
        assertThat(findGroup).isEmpty();

        Optional<Checkbox> findCheckbox1 = checkboxRepository.findById(checkboxResDto1.getId());
        Optional<Checkbox> findCheckbox2 = checkboxRepository.findById(checkboxResDto2.getId());
        Optional<Checkbox> findCheckbox3 = checkboxRepository.findById(checkboxResDto3.getId());

        assertThat(findCheckbox1).isEmpty();
        assertThat(findCheckbox2).isEmpty();
        assertThat(findCheckbox3).isEmpty();

    }

    @Test
    @DisplayName("정상 삭제 - 할일 그룹은 있고 일일 할일은 없는 경우 일정을 삭제하고 연관된 할일을 조회했을 때 개수가 0이어야 한다.")
    void deleteTestNormal_haveClientGroup_noDailyCheckboxes() {

        // given
        MemberResDto memberResDto = memberService.save(new MemberReqDto("test@abc.co.kr", "3s1@adf2"));
        PlanResDto planResDto = planService.save(new PlanReqDto("title", LocalDate.of(2023, 7, 18), LocalDate.of(2023, 7, 25), memberResDto.getId()));
        GroupResDto groupResDto = groupService.save(new GroupReqDto("title", 1, List.of(), planResDto.getId()));
        Long planId = planResDto.getId();
        Long groupId = groupResDto.getId();

        // when
        planService.delete(planId);

        // then
        Optional<Plan> findPlan = planRepository.findById(planId);
        assertThat(findPlan).isEmpty();

        List<Group> findGroup = groupRepository.findByPlanId(planId);
        assertThat(findGroup).isEmpty();

        List<Checkbox> checkboxes = checkboxRepository.findByGroupId(groupId);
        assertThat(checkboxes).isEmpty();

    }

    @Test
    @DisplayName("정상 삭제 - 할일 그룹과 일일 할일 모두 있는 경우 일정을 삭제하고 연관된 할일을 조회했을 때 개수가 0이어야 한다.")
    void deleteTestNormal_haveClientGroup_haveDailyCheckboxes() {

        // given
        MemberResDto memberResDto = memberService.save(new MemberReqDto("test@abc.co.kr", "3s1@adf2"));
        PlanResDto planResDto = planService.save(new PlanReqDto("title", LocalDate.of(2023, 7, 18), LocalDate.of(2023, 7, 25), memberResDto.getId()));
        GroupResDto groupResDto = groupService.save(new GroupReqDto("title", 1, List.of(), planResDto.getId()));
        Long planId = planResDto.getId();
        Long groupId = groupResDto.getId();

        CheckboxResDto checkboxResDto1 = checkboxService.save(new CheckboxReqDto("title1", planResDto.getId(), LocalDate.of(2023, 7, 18)));
        CheckboxResDto checkboxResDto2 = checkboxService.save(new CheckboxReqDto("title2", planResDto.getId(), LocalDate.of(2023, 7, 19)));
        CheckboxResDto checkboxResDto3 = checkboxService.save(new CheckboxReqDto("title3", planResDto.getId(), LocalDate.of(2023, 7, 20)));

        // when
        planService.delete(planId);

        // then
        Optional<Plan> findPlan = planRepository.findById(planId);
        assertThat(findPlan).isEmpty();

        List<Group> findGroup = groupRepository.findByPlanId(planId);
        assertThat(findGroup).isEmpty();

        Optional<Checkbox> findCheckbox1 = checkboxRepository.findById(checkboxResDto1.getId());
        Optional<Checkbox> findCheckbox2 = checkboxRepository.findById(checkboxResDto2.getId());
        Optional<Checkbox> findCheckbox3 = checkboxRepository.findById(checkboxResDto3.getId());

        assertThat(findCheckbox1).isEmpty();
        assertThat(findCheckbox2).isEmpty();
        assertThat(findCheckbox3).isEmpty();

        List<Checkbox> checkboxes = checkboxRepository.findByGroupId(groupId);
        assertThat(checkboxes).isEmpty();

    }

    @Test
    @DisplayName("비정상 삭제 - 대상이 없는 경우 ResourceNotFoundException을 던지는지 확인한다.")
    void deleteTestAbnormal_resourceNotFound() {

        // given - when - then
        assertThrows(ResourceNotFoundException.class, () -> planService.delete(Long.MAX_VALUE));

    }

    /**
     * 상태 변경 메소드 관련 테스트
     * @see PlanServiceTestImpl#change(Long)
     * 일정의 상태를 NOW에서 COMPLETED로, COMPLETED에서 NOW로만 변경할 수 있다.
     */
    @Test
    @DisplayName("정상 상태 변경 - 일정의 상태를 NOW에서 COMPLETED로 변경하고 일정을 다시 조회했을 때 일정의 상태가 COMPLETED여야 한다.")
    void changeTestNormal_nowToCompleted() {

        // given
        Member member = memberRepository.save(new Member("test@abc.co.kr", "a63d@$ga"));
        Plan plan = planRepository.save(new Plan("plan", LocalDate.of(2023, 7, 19), LocalDate.of(2023, 7, 31), member, PlanStatus.NOW));
        Long planId = plan.getId();

        // when
        planService.change(planId);

        // then
        Plan findPlan = planRepository.findById(planId).orElseThrow(ResourceNotFoundException::new);
        assertThat(findPlan.getStatus()).isEqualTo(PlanStatus.COMPLETED);

    }

    @Test
    @DisplayName("정상 상태 변경 - 일정의 상태를 COMPLETED에서 NOW로 변경하고 일정을 다시 조회했을 때 일정의 상태가 NOW여야 한다.")
    void changeTestNormal_completedToNow() {

        // given
        Member member = memberRepository.save(new Member("test@abc.co.kr", "a63d@$ga"));
        Plan plan = planRepository.save(new Plan("plan", LocalDate.of(2023, 7, 19), LocalDate.of(2023, 7, 31), member, PlanStatus.COMPLETED));
        Long planId = plan.getId();

        // when
        planService.change(planId);

        // then
        Plan findPlan = planRepository.findById(planId).orElseThrow(ResourceNotFoundException::new);
        assertThat(findPlan.getStatus()).isEqualTo(PlanStatus.NOW);

    }

    @Test
    @DisplayName("비정상 상태 변경 - 대상이 없는 경우 ResourceNotFoundException을 던지는지 확인해야 한다.")
    void changeTestAbnormal_resourceNotFound() {

        // given - when - then
        assertThrows(ResourceNotFoundException.class, () -> planService.change(Long.MAX_VALUE));

    }

    /**
     * 순수 컬렉션 조회 메소드 관련 테스트
     * 순수 컬렉션이란 필터링이 걸려 있지 않은 컬렉션 조회 API를 의미함
     * @see PlanServiceTestImpl#all(Long)
     * 저장한 개수와 조회한 개수가 같은지 확인한다.
     */
    @Test
    @DisplayName("정상 순수 컬렉션 조회 - 저장한 개수와 조회한 개수가 같은지 확인한다.")
    void allTestNormal() {

        // given
        Member member = memberRepository.save(new Member("test@abc.co.kr", "a63d@$ga"));
        planRepository.save(new Plan("plan1", LocalDate.of(2023, 7, 19), LocalDate.of(2023, 7, 31), member, PlanStatus.NOW));
        planRepository.save(new Plan("plan2", LocalDate.of(2023, 7, 19), LocalDate.of(2023, 7, 31), member, PlanStatus.NOW));
        planRepository.save(new Plan("plan3", LocalDate.of(2023, 7, 19), LocalDate.of(2023, 7, 31), member, PlanStatus.NOW));
        Long memberId = member.getId();

        // when
        List<PlanResDto> all = planService.all(memberId).getWrap();

        // then
        assertThat(all.size()).isEqualTo(3);

    }

    @Test
    @DisplayName("비정상 순수 컬렉션 조회 - 대상이 없는 경우 ResourceNotFoundException을 던지는지 확인한다.")
    void allTestAbnormal() {

        // given - when - then
        assertThrows(ResourceNotFoundException.class, () -> planService.all(Long.MAX_VALUE));

    }

    /**
     * 일별 컬렉션 조회 메소드 관련 테스트
     * 일별 컬렉션이란 일정의 기간이 검색일에 걸쳐 있는 컬렉션을 의미한다.
     *
     * @see PlanServiceTestImpl#all(Long, LocalDate)
     */
    @Test
    @DisplayName("일별 컬렉션 정상 조회 - 등록한 세 개의 일정이 모두 검색일에 해당되는 경우 일별 일정 조회 결과 개수가 3인지 확인한다.")
    void collectionFilteredByDateTestNormal_allMatchedWithKey() {

        // given
        Member member = memberRepository.save(new Member("test@abc.co.kr", "a63d@$ga"));
        planRepository.save(new Plan("plan1", LocalDate.of(2023, 7, 19), LocalDate.of(2023, 7, 31), member, PlanStatus.NOW));
        planRepository.save(new Plan("plan2", LocalDate.of(2023, 7, 19), LocalDate.of(2023, 7, 31), member, PlanStatus.NOW));
        planRepository.save(new Plan("plan3", LocalDate.of(2023, 7, 19), LocalDate.of(2023, 7, 31), member, PlanStatus.NOW));
        Long memberId = member.getId();
        LocalDate dateKey = LocalDate.of(2023, 7, 19);

        // when
        List<PlanResDto> filteredAll = planService.all(memberId, dateKey).getWrap();

        // then
        assertThat(filteredAll.size()).isEqualTo(3);

    }

    @Test
    @DisplayName("일별 컬렉션 정상 조회 - 등록한 일정 중 두 개만 검색일에 해당하는 경우 일별 일정 조회 결과 개수가 2인지 확인한다.")
    void collectionFilteredByDateTestNormal_partOfThemMatchedWithKey() {

        // given
        Member member = memberRepository.save(new Member("test@abc.co.kr", "a63d@$ga"));
        planRepository.save(new Plan("plan1", LocalDate.of(2023, 7, 19), LocalDate.of(2023, 7, 31), member, PlanStatus.NOW));
        planRepository.save(new Plan("plan2", LocalDate.of(2023, 7, 20), LocalDate.of(2023, 7, 31), member, PlanStatus.NOW));
        planRepository.save(new Plan("plan3", LocalDate.of(2023, 7, 19), LocalDate.of(2023, 7, 31), member, PlanStatus.NOW));
        Long memberId = member.getId();
        LocalDate dateKey = LocalDate.of(2023, 7, 19);

        // when
        List<PlanResDto> filteredAll = planService.all(memberId, dateKey).getWrap();

        // then
        assertThat(filteredAll.size()).isEqualTo(2);

    }

    @Test
    @DisplayName("일별 컬렉션 정상 조회 - 등록한 일정 중 검색일에 해당하는 경우가 없는 경우 일별 일정 조회 결과 개수가 0인지 확인한다.")
    void collectionFilteredByDateTestNormal_emptyResult() {

        // given
        Member member = memberRepository.save(new Member("test@abc.co.kr", "a63d@$ga"));
        planRepository.save(new Plan("plan1", LocalDate.of(2023, 7, 20), LocalDate.of(2023, 7, 31), member, PlanStatus.NOW));
        planRepository.save(new Plan("plan2", LocalDate.of(2023, 7, 20), LocalDate.of(2023, 7, 31), member, PlanStatus.NOW));
        planRepository.save(new Plan("plan3", LocalDate.of(2023, 7, 20), LocalDate.of(2023, 7, 31), member, PlanStatus.NOW));
        Long memberId = member.getId();
        LocalDate dateKey = LocalDate.of(2023, 7, 19);

        // when
        List<PlanResDto> filteredAll = planService.all(memberId, dateKey).getWrap();

        // then
        assertThat(filteredAll).isEmpty();

    }

    @Test
    @DisplayName("일별 컬렉션 비정상 조회 - 대상이 없는 경우 ResourceNotFoundException을 던지는지 확인한다.")
    void collectionFilteredByDateTestNormal_resourceNotFound() {

        // given - when - then
        assertThrows(ResourceNotFoundException.class, () -> planService.all(Long.MAX_VALUE, LocalDate.of(2023, 7, 19)));
    }

    /**
     * 기간 컬렉션 정상 조회
     * 기간 컬렉션이란 검색 시작일 ~ 검색 종료일 사이에 일정의 범위가 걸쳐 있는 컬렉션을 의미한다.
     * @see PlanServiceTestImpl#all(Long, LocalDate, LocalDate)
     *
     * (1) 용어
     * 검색 시작일 = ss, 검색 종료일 = se
     * 일정 시작일 = os, 일정 종료일 = oe
     *
     * (2) 주의사항
     * 5, 6번을 제외하고 모든 조합이 조회 대상
     *
     * (3) 테스트 종류
     * 생성한 일정이 모두 조회되는 경우 (7가지)
     * 생성한 일정의 일부분만 조회되는 경우 (2가지)
     * 생성한 일정이 모두 조회되지 않는 경우 (1가지)
     *
     * (4) 기간 조합 리스트
     * 1. os ss se oe
     * 2. ss os oe se
     * 3. ss os se oe
     * 4. os ss oe se
     * 5. ss se os oe (X)
     * 6. os oe ss se (X)
     *
     * 7. (os = ss) se oe
     * 8. (os = ss) oe se
     * 9. os ss (oe = se)
     * 10. ss os (oe = se)
     * 11. os (oe = ss) se
     * 12. os (oe = ss = se)
     * 13. ss (os = se) oe
     * 14. (ss = os = se) oe
     * 15. (ss = se = os = oe)
     */
    @Test
    @DisplayName("기간 컬렉션 정상 조회 - 일정을 세 개 생성해서 기간 컬렉션 조회를 한 결과 세 개 모두 조회됨")
    void collectionFilteredByDateRangeTestNormal_allPlanSucceeded_1() {

        // given
        Member member = memberRepository.save(new Member("test@abc.co.kr", "a63d@$ga"));
        /**
         * case 15, 2, 3
         * plan1 (ss = se = os = oe)
         * plan2 (ss os oe se)
         * plan3 (ss os se oe)
         */
        planRepository.save(new Plan("plan1", LocalDate.of(2023, 7, 20), LocalDate.of(2023, 7, 31), member, PlanStatus.NOW));
        planRepository.save(new Plan("plan2", LocalDate.of(2023, 7, 23), LocalDate.of(2023, 7, 29), member, PlanStatus.NOW));
        planRepository.save(new Plan("plan3", LocalDate.of(2023, 7, 25), LocalDate.of(2023, 8, 3), member, PlanStatus.NOW));
        Long memberId = member.getId();
        LocalDate searchStart = LocalDate.of(2023, 7, 20);
        LocalDate searchEnd = LocalDate.of(2023, 7, 31);

        // when
        List<PlanResDto> filteredAll = planService.all(memberId, searchStart, searchEnd).getWrap();

        // then
        assertThat(filteredAll.size()).isEqualTo(3);

    }

    @Test
    @DisplayName("기간 컬렉션 정상 조회 - 일정을 세 개 생성해서 기간 컬렉션 조회를 한 결과 세 개 모두 조회됨")
    void collectionFilteredByDateRangeTestNormal_allPlanSucceeded_2() {

        // given
        Member member = memberRepository.save(new Member("test@abc.co.kr", "a63d@$ga"));
        /**
         * case 7, 1, 4
         * plan1 ((os = ss) se oe)
         * plan2 (os ss se oe)
         * plan3 (os ss oe se)
         */
        planRepository.save(new Plan("plan1", LocalDate.of(2023, 7, 20), LocalDate.of(2023, 8, 3), member, PlanStatus.NOW));
        planRepository.save(new Plan("plan2", LocalDate.of(2023, 7, 19), LocalDate.of(2023, 8, 3), member, PlanStatus.NOW));
        planRepository.save(new Plan("plan3", LocalDate.of(2023, 7, 19), LocalDate.of(2023, 7, 29), member, PlanStatus.NOW));
        Long memberId = member.getId();
        LocalDate searchStart = LocalDate.of(2023, 7, 20);
        LocalDate searchEnd = LocalDate.of(2023, 7, 31);

        // when
        List<PlanResDto> filteredAll = planService.all(memberId, searchStart, searchEnd).getWrap();

        // then
        assertThat(filteredAll.size()).isEqualTo(3);

    }

    @Test
    @DisplayName("기간 컬렉션 정상 조회 - 일정을 세 개 생성해서 기간 컬렉션 조회를 한 결과 세 개 모두 조회됨")
    void collectionFilteredByDateRangeTestNormal_allPlanSucceeded_3() {

        // given
        Member member = memberRepository.save(new Member("test@abc.co.kr", "a63d@$ga"));
        /**
         * case 10, 8, 9
         * plan1 (ss os (oe = se))
         * plan2 ((os = ss) oe se)
         * plan3 9. os ss (oe = se)
         */
        planRepository.save(new Plan("plan1", LocalDate.of(2023, 7, 23), LocalDate.of(2023, 7, 31), member, PlanStatus.NOW));
        planRepository.save(new Plan("plan2", LocalDate.of(2023, 7, 20), LocalDate.of(2023, 7, 29), member, PlanStatus.NOW));
        planRepository.save(new Plan("plan3", LocalDate.of(2023, 7, 19), LocalDate.of(2023, 7, 31), member, PlanStatus.NOW));
        Long memberId = member.getId();
        LocalDate searchStart = LocalDate.of(2023, 7, 20);
        LocalDate searchEnd = LocalDate.of(2023, 7, 31);

        // when
        List<PlanResDto> filteredAll = planService.all(memberId, searchStart, searchEnd).getWrap();

        // then
        assertThat(filteredAll.size()).isEqualTo(3);

    }

    @Test
    @DisplayName("기간 컬렉션 정상 조회 - 일정을 한 개 생성해서 기간 컬렉션 조회를 한 결과 한 개 모두 조회됨")
    void collectionFilteredByDateRangeTestNormal_allPlanSucceeded_4() {

        // given
        Member member = memberRepository.save(new Member("test@abc.co.kr", "a63d@$ga"));
        /**
         * case 11
         * plan1 (os (oe = ss) se)
         */
        planRepository.save(new Plan("plan1", LocalDate.of(2023, 7, 18), LocalDate.of(2023, 7, 20), member, PlanStatus.NOW));
        Long memberId = member.getId();
        LocalDate searchStart = LocalDate.of(2023, 7, 20);
        LocalDate searchEnd = LocalDate.of(2023, 7, 31);

        // when
        List<PlanResDto> filteredAll = planService.all(memberId, searchStart, searchEnd).getWrap();

        // then
        assertThat(filteredAll.size()).isEqualTo(1);

    }

    @Test
    @DisplayName("기간 컬렉션 정상 조회 - 일정을 한 개 생성해서 기간 컬렉션 조회를 한 결과 한 개 모두 조회됨")
    void collectionFilteredByDateRangeTestNormal_allPlanSucceeded_5() {

        // given
        Member member = memberRepository.save(new Member("test@abc.co.kr", "a63d@$ga"));
        /**
         * case 12
         * plan1 (os (oe = ss = se))
         */
        planRepository.save(new Plan("plan1", LocalDate.of(2023, 7, 18), LocalDate.of(2023, 7, 20), member, PlanStatus.NOW));
        Long memberId = member.getId();
        LocalDate searchStart = LocalDate.of(2023, 7, 20);
        LocalDate searchEnd = LocalDate.of(2023, 7, 20);

        // when
        List<PlanResDto> filteredAll = planService.all(memberId, searchStart, searchEnd).getWrap();

        // then
        assertThat(filteredAll.size()).isEqualTo(1);

    }

    @Test
    @DisplayName("기간 컬렉션 정상 조회 - 일정을 한 개 생성해서 기간 컬렉션 조회를 한 결과 한 개 모두 조회됨")
    void collectionFilteredByDateRangeTestNormal_allPlanSucceeded_6() {

        // given
        Member member = memberRepository.save(new Member("test@abc.co.kr", "a63d@$ga"));
        /**
         * case 13
         * plan1 (ss (os = se) oe)
         */
        planRepository.save(new Plan("plan1", LocalDate.of(2023, 7, 23), LocalDate.of(2023, 7, 31), member, PlanStatus.NOW));
        Long memberId = member.getId();
        LocalDate searchStart = LocalDate.of(2023, 7, 19);
        LocalDate searchEnd = LocalDate.of(2023, 7, 23);

        // when
        List<PlanResDto> filteredAll = planService.all(memberId, searchStart, searchEnd).getWrap();

        // then
        assertThat(filteredAll.size()).isEqualTo(1);

    }

    @Test
    @DisplayName("기간 컬렉션 정상 조회 - 일정을 한 개 생성해서 기간 컬렉션 조회를 한 결과 한 개 모두 조회됨")
    void collectionFilteredByDateRangeTestNormal_allPlanSucceeded_7() {

        // given
        Member member = memberRepository.save(new Member("test@abc.co.kr", "a63d@$ga"));
        /**
         * case 14
         * plan1 ((ss = os = se) oe)
         */
        planRepository.save(new Plan("plan1", LocalDate.of(2023, 7, 23), LocalDate.of(2023, 7, 31), member, PlanStatus.NOW));
        Long memberId = member.getId();
        LocalDate searchStart = LocalDate.of(2023, 7, 23);
        LocalDate searchEnd = LocalDate.of(2023, 7, 23);

        // when
        List<PlanResDto> filteredAll = planService.all(memberId, searchStart, searchEnd).getWrap();

        // then
        assertThat(filteredAll.size()).isEqualTo(1);

    }

    @Test
    @DisplayName("기간 컬렉션 정상 조회 - 일정을 세 개 생성해서 기간 컬렉션 조회를 한 결과 두 개가 조회됨")
    void collectionFilteredByDateRangeTestNormal_partOfThemSucceeded_1() {

        // given
        Member member = memberRepository.save(new Member("test@abc.co.kr", "a63d@$ga"));
        /**
         * case 3, 4, 5
         * plan1 (ss os se oe)
         * plan2 (os ss oe se)
         * plan3 (ss se os oe) -> 조회되면 안 됨
         */
        planRepository.save(new Plan("plan1", LocalDate.of(2023, 7, 23), LocalDate.of(2023, 8, 3), member, PlanStatus.NOW));
        planRepository.save(new Plan("plan2", LocalDate.of(2023, 7, 18), LocalDate.of(2023, 7, 25), member, PlanStatus.NOW));
        planRepository.save(new Plan("plan3", LocalDate.of(2023, 8, 3), LocalDate.of(2023, 8, 5), member, PlanStatus.NOW));
        Long memberId = member.getId();
        LocalDate searchStart = LocalDate.of(2023, 7, 20);
        LocalDate searchEnd = LocalDate.of(2023, 7, 31);

        // when
        List<PlanResDto> filteredAll = planService.all(memberId, searchStart, searchEnd).getWrap();

        // then
        assertThat(filteredAll.size()).isEqualTo(2);
        assertThat(filteredAll.stream().map(PlanResDto::getTitle).toList()).doesNotContain("plan3");

    }

    @Test
    @DisplayName("기간 컬렉션 정상 조회 - 일정을 세 개 생성해서 기간 컬렉션 조회를 한 결과 두 개가 조회됨")
    void collectionFilteredByDateRangeTestNormal_partOfThemSucceeded_2() {

        // given
        Member member = memberRepository.save(new Member("test@abc.co.kr", "a63d@$ga"));
        /**
         * case 1, 2, 6
         * plan1 (os ss se oe)
         * plan2 (ss os oe se)
         * plan3 (os oe ss se) -> 조회되면 안 됨
         */
        planRepository.save(new Plan("plan1", LocalDate.of(2023, 7, 18), LocalDate.of(2023, 8, 3), member, PlanStatus.NOW));
        planRepository.save(new Plan("plan2", LocalDate.of(2023, 7, 23), LocalDate.of(2023, 7, 25), member, PlanStatus.NOW));
        planRepository.save(new Plan("plan3", LocalDate.of(2023, 7, 16), LocalDate.of(2023, 7, 18), member, PlanStatus.NOW));
        Long memberId = member.getId();
        LocalDate searchStart = LocalDate.of(2023, 7, 20);
        LocalDate searchEnd = LocalDate.of(2023, 7, 31);

        // when
        List<PlanResDto> filteredAll = planService.all(memberId, searchStart, searchEnd).getWrap();

        // then
        assertThat(filteredAll.size()).isEqualTo(2);

    }

    @Test
    @DisplayName("기간 컬렉션 정상 조회 - 일정을 두 개 생성해서 기간 컬렉션 조회를 한 결과 하나도 조회되지 않음")
    void collectionFilteredByDateRangeTestNormal_emptyResult() {

        // given
        Member member = memberRepository.save(new Member("test@abc.co.kr", "a63d@$ga"));
        /**
         * case 5, 6
         * plan1 (ss se os oe) -> 조회되면 안 됨
         * plan2 (os oe ss se) -> 조회되면 안 됨
         */
        planRepository.save(new Plan("plan1", LocalDate.of(2023, 8, 3), LocalDate.of(2023, 8, 5), member, PlanStatus.NOW));
        planRepository.save(new Plan("plan2", LocalDate.of(2023, 7, 16), LocalDate.of(2023, 7, 18), member, PlanStatus.NOW));
        Long memberId = member.getId();
        LocalDate searchStart = LocalDate.of(2023, 7, 20);
        LocalDate searchEnd = LocalDate.of(2023, 7, 31);

        // when
        List<PlanResDto> filteredAll = planService.all(memberId, searchStart, searchEnd).getWrap();

        // then
        assertThat(filteredAll.size()).isEqualTo(0);

    }

}
