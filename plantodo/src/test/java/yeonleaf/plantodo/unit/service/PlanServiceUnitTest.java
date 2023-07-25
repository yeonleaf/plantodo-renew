package yeonleaf.plantodo.unit.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import yeonleaf.plantodo.converter.RepInToOutConverter;
import yeonleaf.plantodo.converter.RepOutToInConverter;
import yeonleaf.plantodo.domain.*;
import yeonleaf.plantodo.dto.*;
import yeonleaf.plantodo.exceptions.ResourceNotFoundException;
import yeonleaf.plantodo.repository.*;
import yeonleaf.plantodo.service.*;
import yeonleaf.plantodo.validator.RepInputValidator;

import java.time.LocalDate;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.stream.IntStream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

public class PlanServiceUnitTest {

    private MemberServiceTestImpl memberService;

    private PlanServiceTestImpl planService;

    private GroupServiceTestImpl groupService;

    private MemoryMemberRepository memberRepository;

    private MemoryPlanRepository planRepository;

    private MemoryGroupRepository groupRepository;

    private MemoryRepetitionRepository repetitionRepository;

    private MemoryCheckboxRepository checkboxRepository;

    private CheckboxService checkboxService;

    @BeforeEach
    void setUp() {

        memberRepository = new MemoryMemberRepository();
        planRepository = new MemoryPlanRepository();
        repetitionRepository = new MemoryRepetitionRepository();
        groupRepository = new MemoryGroupRepository(repetitionRepository);
        checkboxRepository = new MemoryCheckboxRepository();

        memberService = new MemberServiceTestImpl(memberRepository);
        groupService = new GroupServiceTestImpl(planRepository, groupRepository, checkboxRepository, new RepInToOutConverter(), new RepOutToInConverter(), new RepInputValidator());
        planService = new PlanServiceTestImpl(memberRepository, planRepository, groupRepository, checkboxRepository, groupService);
        checkboxService = new CheckboxServiceTestImpl(planRepository, groupRepository, checkboxRepository);
    }

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
        assertThat(groups.get(0).getRepOption()).isEqualTo(0);

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
        assertThat(findPlan.getStatus()).isEqualTo(plan.getStatus());

    }

    @Test
    @DisplayName("정상 조회 - PAST")
    void oneTestNormal_planBecomesPast() {

        MemberResDto member = memberService.save(new MemberReqDto("test@abc.co.kr", "3s1@adf2"));
        PlanResDto plan = planService.save(new PlanReqDto("title", LocalDate.of(2023, 7, 18), LocalDate.of(2023, 7, 24), member.getId()));

        PlanResDto findPlan = planService.one(plan.getId());

        assertThat(findPlan.getId()).isEqualTo(plan.getId());
        assertThat(findPlan.getStart()).isEqualTo(plan.getStart());
        assertThat(findPlan.getEnd()).isEqualTo(plan.getEnd());
        assertThat(findPlan.getStatus()).isEqualTo(PlanStatus.PAST);

    }

    @Test
    @DisplayName("비정상 조회")
    void oneTestAbnormal() {

        assertThrows(ResourceNotFoundException.class, () -> planService.one(9999L));

    }

    @Test
    @DisplayName("정상 타이틀 수정")
    void updateTitleTestNormal() {

        MemberResDto member = memberService.save(new MemberReqDto("test@abc.co.kr", "3s1@adf2"));
        PlanResDto plan = planService.save(new PlanReqDto("title", LocalDate.now(), LocalDate.now().plusDays(3), member.getId()));
        groupService.save(new GroupReqDto("group", 1, makeArrToList(), plan.getId()));

        planService.update(new PlanUpdateReqDto(plan.getId(), "updatedTitle", plan.getStart(), plan.getEnd()));

        Plan findPlan = planRepository.findById(plan.getId()).orElseThrow(ResourceNotFoundException::new);
        assertThat(findPlan.getTitle()).isEqualTo("updatedTitle");

    }

    @Test
    @DisplayName("비정상 타이틀 수정")
    void updateTitleTestAbnormal() {

        assertThrows(ResourceNotFoundException.class, () -> planService.update(
                new PlanUpdateReqDto(Long.MAX_VALUE, "updatedTitle",
                        LocalDate.of(2023, 7, 18), LocalDate.of(2023, 7, 18))));

    }

    private PlanResDto makeOldPlan() {
        MemberResDto member = memberService.save(new MemberReqDto("test@abc.co.kr", "3s1@adf2"));
        return planService.save(new PlanReqDto("title", LocalDate.of(2023, 7, 18), LocalDate.of(2023, 7, 25), member.getId()));
    }

    private List<LocalDate> makeDateRangeTest(int repOption, List<String> repValue, LocalDate newStart, LocalDate newEnd) {

        PlanResDto oldPlan = makeOldPlan();
        GroupResDto group = groupService.save(new GroupReqDto("group", repOption, repValue, oldPlan.getId()));

        PlanUpdateReqDto planUpdateReqDto = new PlanUpdateReqDto(oldPlan.getId(), oldPlan.getTitle(), newStart, newEnd);
        planService.update(planUpdateReqDto);

        return checkboxRepository.findByGroupId(group.getId()).stream().map(Checkbox::getDate).toList();

    }

    @Test
    @DisplayName("정상 기간 수정 - repOption = 1 - oldStart < newStart < newEnd < oldEnd")
    void updateDateRangeTestNormal_repOption1_allDatesUnique_1() {

        List<LocalDate> dateResult = makeDateRangeTest(1, makeArrToList(), LocalDate.of(2023, 7, 20), LocalDate.of(2023, 7, 23));
        assertThat(dateResult).containsOnly(
                LocalDate.of(2023, 7, 20),
                LocalDate.of(2023, 7, 21),
                LocalDate.of(2023, 7, 22),
                LocalDate.of(2023, 7, 23)
        );

    }

    @Test
    @DisplayName("정상 기간 수정 - repOption = 1 - newStart < oldStart < newEnd < oldEnd")
    void updateDateRangeTestNormal_repOption1_allDatesUnique_2() {

        List<LocalDate> dateResult = makeDateRangeTest(1, makeArrToList(), LocalDate.of(2023, 7, 16), LocalDate.of(2023, 7, 23));
        IntStream.rangeClosed(16, 23).forEach(i -> assertThat(dateResult).contains(LocalDate.of(2023, 7, i)));
        IntStream.rangeClosed(24, 25).forEach(i -> assertThat(dateResult).doesNotContain(LocalDate.of(2023, 7, i)));

    }

    @Test
    @DisplayName("정상 기간 수정 - repOption = 1 - newStart < oldStart < oldEnd < newEnd")
    void updateDateRangeTestNormal_repOption1_allDatesUnique_3() {

        List<LocalDate> dateResult = makeDateRangeTest(1, makeArrToList(), LocalDate.of(2023, 7, 16), LocalDate.of(2023, 7, 29));
        IntStream.rangeClosed(16, 29).forEach(i -> assertThat(dateResult).contains(LocalDate.of(2023, 7, i)));

    }

    @Test
    @DisplayName("정상 기간 수정 - repOption = 1 - oldStart < newStart < oldEnd < newEnd")
    void updateDateRangeTestNormal_repOption1_allDatesUnique_4() {

        List<LocalDate> dateResult = makeDateRangeTest(1, makeArrToList(), LocalDate.of(2023, 7, 23), LocalDate.of(2023, 7, 29));
        IntStream.rangeClosed(23, 29).forEach(i -> assertThat(dateResult).contains(LocalDate.of(2023, 7, i)));
        IntStream.rangeClosed(18, 22).forEach(i -> assertThat(dateResult).doesNotContain(LocalDate.of(2023, 7, i)));

    }

    @Test
    @DisplayName("정상 기간 수정 - repOption = 1 - newStart < newEnd < oldStart < oldEnd")
    void updateDateRangeTestNormal_repOption1_allDatesUnique_5() {

        List<LocalDate> dateResult = makeDateRangeTest(1, makeArrToList(), LocalDate.of(2023, 7, 13), LocalDate.of(2023, 7, 16));
        IntStream.rangeClosed(13, 16).forEach(i -> assertThat(dateResult).contains(LocalDate.of(2023, 7, i)));
        IntStream.rangeClosed(17, 25).forEach(i -> assertThat(dateResult).doesNotContain(LocalDate.of(2023, 7, i)));

    }

    @Test
    @DisplayName("정상 기간 수정 - repOption = 1 - oldStart < oldEnd < newStart < newEnd")
    void updateDateRangeTestNormal_repOption1_allDatesUnique_6() {

        List<LocalDate> dateResult = makeDateRangeTest(1, makeArrToList(), LocalDate.of(2023, 7, 29), LocalDate.of(2023, 7, 31));
        IntStream.rangeClosed(29, 31).forEach(i -> assertThat(dateResult).contains(LocalDate.of(2023, 7, i)));
        IntStream.rangeClosed(18, 28).forEach(i -> assertThat(dateResult).doesNotContain(LocalDate.of(2023, 7, i)));

    }

    @Test
    @DisplayName("정상 기간 수정 - repOption = 1 - oldStart == newStart && oldEnd == newEnd")
    void updateDateRangeTestNormal_repOption1_datesArentChanged() {

        List<LocalDate> dateResult = makeDateRangeTest(1, makeArrToList(), LocalDate.of(2023, 7, 18), LocalDate.of(2023, 7, 25));
        IntStream.rangeClosed(18, 25).forEach(i -> assertThat(dateResult).contains(LocalDate.of(2023, 7, i)));

    }

    @Test
    @DisplayName("정상 기간 수정 - repOption = 1 - oldStart = newStart && oldEnd < newEnd")
    void updateDateRangeTestNormal_repOption1_newEndGreaterThanOldEnd() {

        List<LocalDate> dateResult = makeDateRangeTest(1, makeArrToList(), LocalDate.of(2023, 7, 18), LocalDate.of(2023, 7, 29));
        IntStream.rangeClosed(18, 29).forEach(i -> assertThat(dateResult).contains(LocalDate.of(2023, 7, i)));

    }

    @Test
    @DisplayName("정상 기간 수정 - repOption = 1 - oldStart = newStart, oldEnd > newEnd")
    void updateDateRangeTestNormal_repOption1_oldEndGreaterThanNewEnd() {

        List<LocalDate> dateResult = makeDateRangeTest(1, makeArrToList(), LocalDate.of(2023, 7, 18), LocalDate.of(2023, 7, 23));
        IntStream.rangeClosed(18, 23).forEach(i -> assertThat(dateResult).contains(LocalDate.of(2023, 7, i)));
        IntStream.rangeClosed(24, 25).forEach(i -> assertThat(dateResult).doesNotContain(LocalDate.of(2023, 7, i)));

    }

    @Test
    @DisplayName("정상 기간 수정 - repOption = 1 - oldStart < newStart, oldEnd = newEnd")
    void updateDateRangeTestNormal_repOption1_newStartGreaterThanOldStart() {

        List<LocalDate> dateResult = makeDateRangeTest(1, makeArrToList(), LocalDate.of(2023, 7, 20), LocalDate.of(2023, 7, 25));
        IntStream.rangeClosed(20, 25).forEach(i -> assertThat(dateResult).contains(LocalDate.of(2023, 7, i)));
        IntStream.rangeClosed(18, 19).forEach(i -> assertThat(dateResult).doesNotContain(LocalDate.of(2023, 7, i)));

    }

    @Test
    @DisplayName("정상 기간 수정 - repOption = 1 - oldStart > newStart, oldEnd = newEnd")
    void updateDateRangeTestNormal_repOption1_oldStartGreaterThanNewStart() {

        List<LocalDate> dateResult = makeDateRangeTest(1, makeArrToList(), LocalDate.of(2023, 7, 16), LocalDate.of(2023, 7, 25));
        IntStream.rangeClosed(16, 25).forEach(i -> assertThat(dateResult).contains(LocalDate.of(2023, 7, i)));

    }


    /*
    * repOption = 2
    * */

    @Test
    @DisplayName("정상 기간 수정 - repOption = 2 - oldStart < newStart < newEnd < oldEnd")
    void updateDateRangeTestNormal_repOption2_allDatesUnique_1() {

        List<LocalDate> dateResult = makeDateRangeTest(2, makeArrToList("3"), LocalDate.of(2023, 7, 20), LocalDate.of(2023, 7, 23));
        assertThat(dateResult).containsOnly(
                LocalDate.of(2023, 7, 20),
                LocalDate.of(2023, 7, 23)
        );

    }

    @Test
    @DisplayName("정상 기간 수정 - repOption = 2 - newStart < oldStart < newEnd < oldEnd")
    void updateDateRangeTestNormal_repOption2_allDatesUnique_2() {

        List<LocalDate> dateResult = makeDateRangeTest(2, makeArrToList("3"), LocalDate.of(2023, 7, 16), LocalDate.of(2023, 7, 23));
        IntStream.iterate(16, i -> i + 3).limit(3).forEach(i -> assertThat(dateResult).contains(LocalDate.of(2023, 7, i)));

    }

    @Test
    @DisplayName("정상 기간 수정 - repOption = 2 - newStart < oldStart < oldEnd < newEnd")
    void updateDateRangeTestNormal_repOption2_allDatesUnique_3() {

        List<LocalDate> dateResult = makeDateRangeTest(2, makeArrToList("3"), LocalDate.of(2023, 7, 16), LocalDate.of(2023, 7, 29));
        IntStream.iterate(16, i -> i + 3).limit(5).forEach(i -> assertThat(dateResult).contains(LocalDate.of(2023, 7, i)));

    }

    @Test
    @DisplayName("정상 기간 수정 - repOption = 2 - oldStart < newStart < oldEnd < newEnd")
    void updateDateRangeTestNormal_repOption2_allDatesUnique_4() {

        List<LocalDate> dateResult = makeDateRangeTest(2, makeArrToList("3"), LocalDate.of(2023, 7, 23), LocalDate.of(2023, 7, 29));
        IntStream.iterate(23, i -> i + 3).limit(3).forEach(i -> assertThat(dateResult).contains(LocalDate.of(2023, 7, i)));

    }

    @Test
    @DisplayName("정상 기간 수정 - repOption = 2 - newStart < newEnd < oldStart < oldEnd")
    void updateDateRangeTestNormal_repOption2_allDatesUnique_5() {

        List<LocalDate> dateResult = makeDateRangeTest(2, makeArrToList("3"), LocalDate.of(2023, 7, 13), LocalDate.of(2023, 7, 16));
        assertThat(dateResult).containsOnly(
                LocalDate.of(2023, 7, 13),
                LocalDate.of(2023, 7, 16)
        );

    }

    @Test
    @DisplayName("정상 기간 수정 - repOption = 2 - oldStart < oldEnd < newStart < newEnd")
    void updateDateRangeTestNormal_repOption2_allDatesUnique_6() {

        List<LocalDate> dateResult = makeDateRangeTest(2, makeArrToList("3"), LocalDate.of(2023, 7, 29), LocalDate.of(2023, 7, 31));
        assertThat(dateResult).containsOnly(
                LocalDate.of(2023, 7, 29)
        );

    }

    @Test
    @DisplayName("정상 기간 수정 - repOption = 2 - oldStart == newStart && oldEnd == newEnd")
    void updateDateRangeTestNormal_repOption2_datesArentChanged() {

        List<LocalDate> dateResult = makeDateRangeTest(2, makeArrToList("3"), LocalDate.of(2023, 7, 18), LocalDate.of(2023, 7, 25));
        assertThat(dateResult).containsOnly(
                LocalDate.of(2023, 7, 18),
                LocalDate.of(2023, 7, 21),
                LocalDate.of(2023, 7, 24)
        );

    }

    @Test
    @DisplayName("정상 기간 수정 - repOption = 2 - oldStart = newStart && oldEnd < newEnd")
    void updateDateRangeTestNormal_repOption2_newEndGreaterThanOldEnd() {

        List<LocalDate> dateResult = makeDateRangeTest(2, makeArrToList("3"), LocalDate.of(2023, 7, 18), LocalDate.of(2023, 7, 29));
        IntStream.iterate(18, i -> i + 3).limit(4).forEach(i -> assertThat(dateResult).contains(LocalDate.of(2023, 7, i)));

    }

    @Test
    @DisplayName("정상 기간 수정 - repOption = 2 - oldStart = newStart, oldEnd > newEnd")
    void updateDateRangeTestNormal_repOption2_oldEndGreaterThanNewEnd() {

        List<LocalDate> dateResult = makeDateRangeTest(2, makeArrToList("3"), LocalDate.of(2023, 7, 18), LocalDate.of(2023, 7, 23));
        assertThat(dateResult).containsOnly(
                LocalDate.of(2023, 7, 18),
                LocalDate.of(2023, 7, 21)
        );

    }

    @Test
    @DisplayName("정상 기간 수정 - repOption = 2 - oldStart < newStart, oldEnd = newEnd")
    void updateDateRangeTestNormal_repOption2_newStartGreaterThanOldStart() {

        List<LocalDate> dateResult = makeDateRangeTest(2, makeArrToList("3"), LocalDate.of(2023, 7, 20), LocalDate.of(2023, 7, 25));
        assertThat(dateResult).containsOnly(
                LocalDate.of(2023, 7, 20),
                LocalDate.of(2023, 7, 23)
        );

    }

    @Test
    @DisplayName("정상 기간 수정 - repOption = 2 - oldStart > newStart, oldEnd = newEnd")
    void updateDateRangeTestNormal_repOption2_oldStartGreaterThanNewStart() {

        List<LocalDate> dateResult = makeDateRangeTest(2, makeArrToList("3"), LocalDate.of(2023, 7, 16), LocalDate.of(2023, 7, 25));
        assertThat(dateResult).containsOnly(
                LocalDate.of(2023, 7, 16),
                LocalDate.of(2023, 7, 19),
                LocalDate.of(2023, 7, 22),
                LocalDate.of(2023, 7, 25)
        );

    }

    /*
    * repOption = 3L
    * */
    @Test
    @DisplayName("정상 기간 수정 - repOption = 3 - oldStart < newStart < newEnd < oldEnd")
    void updateDateRangeTestNormal_repOption3_allDatesUnique_1() {

        List<LocalDate> dateResult = makeDateRangeTest(3, makeArrToList("월", "수", "금"), LocalDate.of(2023, 7, 20), LocalDate.of(2023, 7, 23));
        assertThat(dateResult).containsOnly(
                LocalDate.of(2023, 7, 21)
        );

    }

    @Test
    @DisplayName("정상 기간 수정 - repOption = 3 - newStart < oldStart < newEnd < oldEnd")
    void updateDateRangeTestNormal_repOption3_allDatesUnique_2() {

        List<LocalDate> dateResult = makeDateRangeTest(3, makeArrToList("월", "수", "금"), LocalDate.of(2023, 7, 16), LocalDate.of(2023, 7, 23));
        assertThat(dateResult).containsOnly(
                LocalDate.of(2023, 7, 17),
                LocalDate.of(2023, 7, 19),
                LocalDate.of(2023, 7, 21)
        );

    }

    @Test
    @DisplayName("정상 기간 수정 - repOption = 3 - newStart < oldStart < oldEnd < newEnd")
    void updateDateRangeTestNormal_repOption3_allDatesUnique_3() {

        List<LocalDate> dateResult = makeDateRangeTest(3, makeArrToList("월", "수", "금"), LocalDate.of(2023, 7, 16), LocalDate.of(2023, 7, 29));
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
    @DisplayName("정상 기간 수정 - repOption = 3 - oldStart < newStart < oldEnd < newEnd")
    void updateDateRangeTestNormal_repOption3_allDatesUnique_4() {

        List<LocalDate> dateResult = makeDateRangeTest(3, makeArrToList("월", "수", "금"), LocalDate.of(2023, 7, 23), LocalDate.of(2023, 7, 29));
        assertThat(dateResult).containsOnly(
                LocalDate.of(2023, 7, 24),
                LocalDate.of(2023, 7, 26),
                LocalDate.of(2023, 7, 28)
        );
    }

    @Test
    @DisplayName("정상 기간 수정 - repOption = 3 - newStart < newEnd < oldStart < oldEnd")
    void updateDateRangeTestNormal_repOption3_allDatesUnique_5() {

        List<LocalDate> dateResult = makeDateRangeTest(3, makeArrToList("월", "수", "금"), LocalDate.of(2023, 7, 13), LocalDate.of(2023, 7, 16));
        assertThat(dateResult).containsOnly(
                LocalDate.of(2023, 7, 14)
        );

    }

    @Test
    @DisplayName("정상 기간 수정 - repOption = 3 - oldStart < oldEnd < newStart < newEnd")
    void updateDateRangeTestNormal_repOption3_allDatesUnique_6() {

        List<LocalDate> dateResult = makeDateRangeTest(3, makeArrToList("월", "수", "금"), LocalDate.of(2023, 7, 29), LocalDate.of(2023, 7, 31));
        assertThat(dateResult).containsOnly(
                LocalDate.of(2023, 7, 31)
        );

    }

    @Test
    @DisplayName("정상 기간 수정 - repOption = 3L - oldStart == newStart && oldEnd == newEnd")
    void updateDateRangeTestNormal_repOption3_datesArentChanged() {

        List<LocalDate> dateResult = makeDateRangeTest(3, makeArrToList("월", "수", "금"), LocalDate.of(2023, 7, 18), LocalDate.of(2023, 7, 25));
        assertThat(dateResult).containsOnly(
                LocalDate.of(2023, 7, 19),
                LocalDate.of(2023, 7, 21),
                LocalDate.of(2023, 7, 24)
        );

    }

    @Test
    @DisplayName("정상 기간 수정 - repOption = 3L - oldStart = newStart && oldEnd < newEnd")
    void updateDateRangeTestNormal_repOption3_newEndGreaterThanOldEnd() {

        List<LocalDate> dateResult = makeDateRangeTest(3, makeArrToList("월", "수", "금"), LocalDate.of(2023, 7, 18), LocalDate.of(2023, 7, 29));
        assertThat(dateResult).containsOnly(
                LocalDate.of(2023, 7, 19),
                LocalDate.of(2023, 7, 21),
                LocalDate.of(2023, 7, 24),
                LocalDate.of(2023, 7, 26),
                LocalDate.of(2023, 7, 28)
        );

    }

    @Test
    @DisplayName("정상 기간 수정 - repOption = 3L - oldStart = newStart, oldEnd > newEnd")
    void updateDateRangeTestNormal_repOption3_oldEndGreaterThanNewEnd() {

        List<LocalDate> dateResult = makeDateRangeTest(3, makeArrToList("월", "수", "금"), LocalDate.of(2023, 7, 18), LocalDate.of(2023, 7, 23));
        assertThat(dateResult).containsOnly(
                LocalDate.of(2023, 7, 19),
                LocalDate.of(2023, 7, 21)
        );

    }

    @Test
    @DisplayName("정상 기간 수정 - repOption = 3L - oldStart < newStart, oldEnd = newEnd")
    void updateDateRangeTestNormal_repOption3_newStartGreaterThanOldStart() {

        List<LocalDate> dateResult = makeDateRangeTest(3, makeArrToList("월", "수", "금"), LocalDate.of(2023, 7, 20), LocalDate.of(2023, 7, 25));
        assertThat(dateResult).containsOnly(
                LocalDate.of(2023, 7, 21),
                LocalDate.of(2023, 7, 24)
        );

    }

    @Test
    @DisplayName("정상 기간 수정 - repOption = 3L - oldStart > newStart, oldEnd = newEnd")
    void updateDateRangeTestNormal_repOption3_oldStartGreaterThanNewStart() {

        List<LocalDate> dateResult = makeDateRangeTest(3, makeArrToList("월", "수", "금"), LocalDate.of(2023, 7, 16), LocalDate.of(2023, 7, 25));
        assertThat(dateResult).containsOnly(
                LocalDate.of(2023, 7, 17),
                LocalDate.of(2023, 7, 19),
                LocalDate.of(2023, 7, 21),
                LocalDate.of(2023, 7, 24)
        );

    }

    @Test
    @DisplayName("정상 삭제 - 클라이언트 Group X, Daily Checkbox X")
    void deleteTestNormal_noClientGroup_noDailyCheckboxes() {

        MemberResDto memberResDto = memberService.save(new MemberReqDto("test@abc.co.kr", "3s1@adf2"));
        PlanResDto planResDto = planService.save(new PlanReqDto("title", LocalDate.of(2023, 7, 18), LocalDate.of(2023, 7, 25), memberResDto.getId()));

        Long planId = planResDto.getId();
        planService.delete(planId);

        List<GroupResDto> groups = groupService.findAllByPlanId(planId);
        assertThat(groups).isEmpty();

    }

    @Test
    @DisplayName("정상 삭제 - 클라이언트 Group X, Daily Checkbox O")
    void deleteTestNormal_noClientGroup_dailyCheckboxes() {

        MemberResDto memberResDto = memberService.save(new MemberReqDto("test@abc.co.kr", "3s1@adf2"));
        PlanResDto planResDto = planService.save(new PlanReqDto("title", LocalDate.of(2023, 7, 18), LocalDate.of(2023, 7, 25), memberResDto.getId()));
        Long planId = planResDto.getId();

        CheckboxResDto checkboxResDto1 = checkboxService.save(new CheckboxReqDto("title1", planResDto.getId(), LocalDate.of(2023, 7, 18)));
        CheckboxResDto checkboxResDto2 = checkboxService.save(new CheckboxReqDto("title2", planResDto.getId(), LocalDate.of(2023, 7, 19)));
        CheckboxResDto checkboxResDto3 = checkboxService.save(new CheckboxReqDto("title3", planResDto.getId(), LocalDate.of(2023, 7, 20)));

        planService.delete(planId);

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
    @DisplayName("정상 삭제 - 클라이언트 Group O, Daily Checkboxes X")
    void deleteTestNormal_haveClientGroup_noDailyCheckboxes() {

        MemberResDto memberResDto = memberService.save(new MemberReqDto("test@abc.co.kr", "3s1@adf2"));
        PlanResDto planResDto = planService.save(new PlanReqDto("title", LocalDate.of(2023, 7, 18), LocalDate.of(2023, 7, 25), memberResDto.getId()));
        GroupResDto groupResDto = groupService.save(new GroupReqDto("title", 1, makeArrToList(), planResDto.getId()));
        Long planId = planResDto.getId();
        Long groupId = groupResDto.getId();

        planService.delete(planId);

        Optional<Plan> findPlan = planRepository.findById(planId);
        assertThat(findPlan).isEmpty();

        List<Group> findGroup = groupRepository.findByPlanId(planId);
        assertThat(findGroup).isEmpty();

        List<Checkbox> checkboxes = checkboxRepository.findByGroupId(groupId);
        assertThat(checkboxes).isEmpty();

    }

    @Test
    @DisplayName("정상 삭제 - 클라이언트 Group O, Daily Checkboxes O")
    void deleteTestNormal_haveClientGroup_haveDailyCheckboxes() {

        MemberResDto memberResDto = memberService.save(new MemberReqDto("test@abc.co.kr", "3s1@adf2"));
        PlanResDto planResDto = planService.save(new PlanReqDto("title", LocalDate.of(2023, 7, 18), LocalDate.of(2023, 7, 25), memberResDto.getId()));
        GroupResDto groupResDto = groupService.save(new GroupReqDto("title", 1, makeArrToList(), planResDto.getId()));
        Long planId = planResDto.getId();
        Long groupId = groupResDto.getId();

        CheckboxResDto checkboxResDto1 = checkboxService.save(new CheckboxReqDto("title1", planResDto.getId(), LocalDate.of(2023, 7, 18)));
        CheckboxResDto checkboxResDto2 = checkboxService.save(new CheckboxReqDto("title2", planResDto.getId(), LocalDate.of(2023, 7, 19)));
        CheckboxResDto checkboxResDto3 = checkboxService.save(new CheckboxReqDto("title3", planResDto.getId(), LocalDate.of(2023, 7, 20)));

        planService.delete(planId);

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
    @DisplayName("비정상 삭제 - Resource not found")
    void deleteTestAbnormal_resourceNotFound() {

        assertThrows(ResourceNotFoundException.class, () -> planService.delete(Long.MAX_VALUE));

    }

    @Test
    @DisplayName("정상 상태 변경 - NOW TO COMPLETED")
    void changeTestNormal_nowToCompleted() {

        Member member = memberRepository.save(new Member("test@abc.co.kr", "a63d@$ga"));
        Plan plan = planRepository.save(new Plan("plan", LocalDate.of(2023, 7, 19), LocalDate.of(2023, 7, 31), member, PlanStatus.NOW));
        Long planId = plan.getId();

        planService.change(planId);

        Plan findPlan = planRepository.findById(planId).orElseThrow(ResourceNotFoundException::new);
        assertThat(findPlan.getStatus()).isEqualTo(PlanStatus.COMPLETED);

    }

    @Test
    @DisplayName("정상 상태 변경 - COMPLETED TO NOW")
    void changeTestNormal_completedToNow() {

        Member member = memberRepository.save(new Member("test@abc.co.kr", "a63d@$ga"));
        Plan plan = planRepository.save(new Plan("plan", LocalDate.of(2023, 7, 19), LocalDate.of(2023, 7, 31), member, PlanStatus.COMPLETED));
        Long planId = plan.getId();

        planService.change(planId);

        Plan findPlan = planRepository.findById(planId).orElseThrow(ResourceNotFoundException::new);
        assertThat(findPlan.getStatus()).isEqualTo(PlanStatus.NOW);

    }

    @Test
    @DisplayName("비정상 상태 변경 - Resource not found")
    void changeTestAbnormal_resourceNotFound() {

        assertThrows(ResourceNotFoundException.class, () -> planService.change(Long.MAX_VALUE));

    }

}
