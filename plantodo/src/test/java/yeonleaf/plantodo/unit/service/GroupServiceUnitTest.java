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
import yeonleaf.plantodo.exceptions.ResourceNotFoundException;
import yeonleaf.plantodo.repository.*;
import yeonleaf.plantodo.service.GroupServiceTestImpl;
import yeonleaf.plantodo.service.PlanServiceTestImpl;

import java.time.LocalDate;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

@ExtendWith(SpringExtension.class)
@ContextConfiguration(classes = ServiceTestConfig.class)
public class GroupServiceUnitTest {

    @Autowired
    private GroupServiceTestImpl groupService;

    @Autowired
    private MemoryCheckboxRepository checkboxRepository;

    @Autowired
    private MemoryMemberRepository memberRepository;

    @Autowired
    private MemoryPlanRepository planRepository;

    @Autowired
    private PlanServiceTestImpl planService;

    @Autowired
    private MemoryGroupRepository groupRepository;

    @Autowired
    private MemoryRepetitionRepository repetitionRepository;

    private Member makeMember(String email, String password) {
        Member member = new Member(email, password);
        memberRepository.save(member);
        return member;
    }

    private List<String> makeArrToList(String... target) {
        return Arrays.asList(target);
    }

    private void makeSaveRepOptionTest(int repOption, List<String> repValue, LocalDate start, LocalDate end, int expectedCnt) {

        Member member = makeMember("test@abc.co.kr", "3d^$a2df");
        Plan plan = planRepository.save(new Plan("plan", start, end, member));
        GroupResDto group = groupService.save(new GroupReqDto("group", repOption, repValue, plan.getId()));

        List<Checkbox> checkboxes = checkboxRepository.findByGroupId(group.getId());
        assertThat(checkboxes.size()).isEqualTo(expectedCnt);

    }

    @Test
    @DisplayName("정상 등록 - repOption = 1, start < end")
    void saveTestRepOption1_EndGreaterThanStart() {

        makeSaveRepOptionTest(1, makeArrToList(), LocalDate.of(2023, 7, 18), LocalDate.of(2023, 7, 31), 14);

    }

    @Test
    @DisplayName("정상 등록 - repOption = 1, start = end")
    void saveTestRepOption1_EndEqualToStart() {

        makeSaveRepOptionTest(1, makeArrToList(), LocalDate.of(2023, 7, 18), LocalDate.of(2023, 7, 18), 1);

    }

    @Test
    @DisplayName("정상 등록 - repOption = 2, start < end")
    void saveTestRepOption2_EndGreaterThanStart() {

        makeSaveRepOptionTest(2, makeArrToList("2"), LocalDate.of(2023, 7, 18), LocalDate.of(2023, 7, 31), 7);

    }

    @Test
    @DisplayName("정상 등록 - repOption = 2, start = end")
    void saveTestRepOption2_EndEqualToStart() {

        makeSaveRepOptionTest(2, makeArrToList("2"), LocalDate.of(2023, 7, 18), LocalDate.of(2023, 7, 18), 1);

    }

    @Test
    @DisplayName("정상 등록 - repOption = 3L, start < end")
    void saveTestRepOption3L_EndGreaterThanStart() {

        makeSaveRepOptionTest(3, makeArrToList("월", "수", "금"), LocalDate.of(2023, 7, 18), LocalDate.of(2023, 7, 31), 6);

    }

    @Test
    @DisplayName("정상 등록 - repOption = 3L, start = end")
    void saveTestRepOption3_EndEqualToStart() {

        makeSaveRepOptionTest(3, makeArrToList("월", "수", "금"), LocalDate.of(2023, 7, 18), LocalDate.of(2023, 7, 18), 0);

    }

    @Test
    @DisplayName("단건 정상 조회")
    void oneTestNormal() {

        Member member = makeMember("test@abc.co.kr", "3d^$a2df");
        Plan plan = planRepository.save(new Plan("plan", LocalDate.of(2023, 7, 18), LocalDate.of(2023, 7, 31), member));
        GroupResDto savedGroup = groupService.save(new GroupReqDto("group", 3, makeArrToList("화", "목"), plan.getId()));

        GroupResDto groupResDto = groupService.one(savedGroup.getId());

        assertThat(groupResDto.equals(savedGroup)).isTrue();
        assertThat(groupResDto.getRepValue()).isEqualTo(makeArrToList("화", "목"));

    }

    @Test
    @DisplayName("단건 비정상 조회")
    void oneTestAbnormal() {

        assertThrows(ResourceNotFoundException.class, () -> groupService.one(9999L));

    }

    @Test
    @DisplayName("정상 수정 - 타이틀 X, repOption X, repValue X (변화 없음)")
    void updateTestNormal_notChanged() {

        Member member = makeMember("test@abc.co.kr", "3d^$a2df");
        Plan plan = planRepository.save(new Plan("plan", LocalDate.of(2023, 7, 18), LocalDate.of(2023, 7, 31), member));
        GroupResDto savedGroup = groupService.save(new GroupReqDto("group", 3, makeArrToList("화", "목"), plan.getId()));

        GroupUpdateReqDto groupUpdateReqDto = new GroupUpdateReqDto(savedGroup.getId(), "group", 3, makeArrToList("화", "목"));
        GroupResDto updatedGroup = groupService.update(groupUpdateReqDto);

        assertThat(updatedGroup.getTitle()).isEqualTo(savedGroup.getTitle());
        assertThat(updatedGroup.getRepOption()).isEqualTo(savedGroup.getRepOption());
        assertThat(updatedGroup.getRepValue()).isEqualTo(savedGroup.getRepValue());

    }

    @Test
    @DisplayName("정상 수정 - 타이틀 O, repOption X, repValue X")
    void updateTestNormal_changedOnlyTitle() {

        Member member = makeMember("test@abc.co.kr", "3d^$a2df");
        Plan plan = planRepository.save(new Plan("plan", LocalDate.of(2023, 7, 18), LocalDate.of(2023, 7, 31), member));
        GroupResDto savedGroup = groupService.save(new GroupReqDto("group", 3, makeArrToList("화", "목"), plan.getId()));

        GroupUpdateReqDto groupUpdateReqDto = new GroupUpdateReqDto(savedGroup.getId(), "updatedGroup", 3, makeArrToList("화", "목"));
        groupService.update(groupUpdateReqDto);

        GroupResDto groupResDto = groupService.one(savedGroup.getId());
        assertThat(groupResDto.getTitle()).isEqualTo("updatedGroup");

    }

    @Test
    @DisplayName("정상 수정 - 타이틀 X, repOption O, repValue O")
    void updateTestNormal_changedRepOptionAndRepValue() {

        Member member = makeMember("test@abc.co.kr", "3d^$a2df");
        Plan plan = planRepository.save(new Plan("plan", LocalDate.of(2023, 7, 18), LocalDate.of(2023, 7, 25), member));
        GroupResDto savedGroup = groupService.save(new GroupReqDto("group", 3, makeArrToList("화", "목"), plan.getId()));

        GroupUpdateReqDto groupUpdateReqDto = new GroupUpdateReqDto(savedGroup.getId(), "group", 2, makeArrToList("3"));
        GroupResDto updatedGroup = groupService.update(groupUpdateReqDto);

        Stream<LocalDate> dateResult = checkboxRepository.findByGroupId(updatedGroup.getId()).stream().map(Checkbox::getDate);
        assertThat(dateResult).containsOnly(
                LocalDate.of(2023, 7, 18),
                LocalDate.of(2023, 7, 21),
                LocalDate.of(2023, 7, 24)
        );

    }

    @Test
    @DisplayName("정상 수정 - 타이틀 X, repOption X, repValue O")
    void updateTestNormal_changedOnlyRepValue() {

        Member member = makeMember("test@abc.co.kr", "3d^$a2df");
        Plan plan = planRepository.save(new Plan("plan", LocalDate.of(2023, 7, 18), LocalDate.of(2023, 7, 25), member));
        GroupResDto savedGroup = groupService.save(new GroupReqDto("group", 3, makeArrToList("화", "목"), plan.getId()));

        GroupUpdateReqDto groupUpdateReqDto = new GroupUpdateReqDto(savedGroup.getId(), "group", 3, makeArrToList("월", "수", "금"));
        GroupResDto updatedGroup = groupService.update(groupUpdateReqDto);

        Stream<LocalDate> dateResult = checkboxRepository.findByGroupId(updatedGroup.getId()).stream().map(Checkbox::getDate);
        assertThat(dateResult).containsOnly(
                LocalDate.of(2023, 7, 19),
                LocalDate.of(2023, 7, 21),
                LocalDate.of(2023, 7, 24)
        );

    }

    @Test
    @DisplayName("정상 수정 - 타이틀 O, repOption O, repValue O")
    void updateTestNormal_changedAll() {

        Member member = makeMember("test@abc.co.kr", "3d^$a2df");
        Plan plan = planRepository.save(new Plan("plan", LocalDate.of(2023, 7, 18), LocalDate.of(2023, 7, 25), member));
        GroupResDto savedGroup = groupService.save(new GroupReqDto("title", 3, makeArrToList("화", "목"), plan.getId()));

        GroupUpdateReqDto groupUpdateReqDto = new GroupUpdateReqDto(savedGroup.getId(), "updatedTitle", 2, makeArrToList("3"));
        GroupResDto updatedGroup = groupService.update(groupUpdateReqDto);

        GroupResDto groupResDto = groupService.one(updatedGroup.getId());
        assertThat(groupResDto.getTitle()).isEqualTo("updatedTitle");

        Stream<LocalDate> dateResult = checkboxRepository.findByGroupId(updatedGroup.getId()).stream().map(Checkbox::getDate);
        assertThat(dateResult).containsOnly(
                LocalDate.of(2023, 7, 18),
                LocalDate.of(2023, 7, 21),
                LocalDate.of(2023, 7, 24)
        );

    }

    @Test
    @DisplayName("정상 수정 - 타이틀 O, repOption X, repValue O")
    void updateTestNormal_changedTitleAndRepValue() {

        Member member = makeMember("test@abc.co.kr", "3d^$a2df");
        Plan plan = planRepository.save(new Plan("plan", LocalDate.of(2023, 7, 18), LocalDate.of(2023, 7, 25), member));
        GroupResDto savedGroup = groupService.save(new GroupReqDto("title", 3, makeArrToList("화", "목"), plan.getId()));

        GroupUpdateReqDto groupUpdateReqDto = new GroupUpdateReqDto(savedGroup.getId(), "updatedTitle", 3, makeArrToList("월", "일"));
        GroupResDto updatedGroup = groupService.update(groupUpdateReqDto);

        GroupResDto groupResDto = groupService.one(updatedGroup.getId());
        assertThat(groupResDto.getTitle()).isEqualTo("updatedTitle");

        Stream<LocalDate> dateResult = checkboxRepository.findByGroupId(updatedGroup.getId()).stream().map(Checkbox::getDate);
        assertThat(dateResult).containsOnly(
                LocalDate.of(2023, 7, 23),
                LocalDate.of(2023, 7, 24)
        );

    }

    @Test
    @DisplayName("비정상 수정 - Resource not found")
    void updateTestAbnormal_resourceNotFound() {

        GroupUpdateReqDto groupUpdateReqDto = new GroupUpdateReqDto(Long.MAX_VALUE, "updatedTitle", 3, makeArrToList("월", "일"));
        assertThrows(ResourceNotFoundException.class, () -> groupService.update(groupUpdateReqDto));

    }

    @Test
    @DisplayName("정상 삭제 - Repetition, Checkbox 삭제 확인")
    void deleteTestNormal() {

        Member member = makeMember("test@abc.co.kr", "3d^$a2df");
        Plan plan = planRepository.save(new Plan("plan", LocalDate.of(2023, 7, 18), LocalDate.of(2023, 7, 25), member));
        GroupResDto savedGroup = groupService.save(new GroupReqDto("title", 3, makeArrToList("화", "목"), plan.getId()));
        Group findGroup = groupRepository.findById(savedGroup.getId()).orElseThrow(ResourceNotFoundException::new);

        Long groupId = findGroup.getId();
        Long repetitionId = findGroup.getRepetition().getId();

        groupService.delete(groupId);

        List<Checkbox> findCheckboxes = checkboxRepository.findByGroupId(groupId);
        Optional<Repetition> findRepetition = repetitionRepository.findById(repetitionId);
        assertThat(findCheckboxes).isEmpty();
        assertThat(findRepetition).isEmpty();

    }

    @Test
    @DisplayName("비정상 삭제 - Resource not found")
    void deleteTestAbnormal_resourceNotFound() {

        assertThrows(ResourceNotFoundException.class, () -> groupService.delete(Long.MAX_VALUE));

    }

    @Test
    @DisplayName("정상 순수 컬렉션 조회")
    void allTestNormal() {

        Member member = makeMember("test@abc.co.kr", "3d^$a2df");
        PlanResDto planResDto = planService.save(new PlanReqDto("title", LocalDate.of(2023, 7, 18), LocalDate.of(2023, 7, 25), member.getId()));
        Long planId = planResDto.getId();
        groupService.save(new GroupReqDto("title1", 3, makeArrToList("화", "목"), planId));
        groupService.save(new GroupReqDto("title2", 3, makeArrToList("화", "목"), planId));
        groupService.save(new GroupReqDto("title3", 3, makeArrToList("화", "목"), planId));

        List<GroupResDto> all = groupService.all(planId);

        assertThat(all.size()).isEqualTo(3);

    }

    @Test
    @DisplayName("비정상 순수 컬렉션 조회 - Resource not found")
    void allTestAbnormal() {

        assertThrows(ResourceNotFoundException.class, () -> groupService.all(Long.MAX_VALUE));

    }

    @Test
    @DisplayName("일별 컬렉션 정상 조회 - all matched with key")
    void collectionFilteredByDateTestNormal_allMatchedWithKey() {

        Member member = makeMember("test@abc.co.kr", "3d^$a2df");
        PlanResDto planResDto = planService.save(new PlanReqDto("title", LocalDate.of(2023, 7, 19), LocalDate.of(2023, 7, 31), member.getId()));
        Long planId = planResDto.getId();
        groupService.save(new GroupReqDto("title1", 3, makeArrToList("화", "목"), planId));
        groupService.save(new GroupReqDto("title2", 1, makeArrToList(), planId));
        groupService.save(new GroupReqDto("title3", 2, makeArrToList("2"), planId));
        LocalDate dateKey = LocalDate.of(2023, 7, 25);

        List<GroupResDto> filteredAll = groupService.all(planId, dateKey);

        assertThat(filteredAll.size()).isEqualTo(3);

    }

    @Test
    @DisplayName("일별 컬렉션 정상 조회 - part of them matched with key")
    void collectionFilteredByDateTestNormal_partOfThemMatchedWithKey() {

        Member member = makeMember("test@abc.co.kr", "3d^$a2df");
        PlanResDto planResDto = planService.save(new PlanReqDto("title", LocalDate.of(2023, 7, 19), LocalDate.of(2023, 7, 31), member.getId()));
        Long planId = planResDto.getId();
        groupService.save(new GroupReqDto("title1", 3, makeArrToList("화", "목"), planId));
        groupService.save(new GroupReqDto("title2", 1, makeArrToList(), planId));
        groupService.save(new GroupReqDto("title3", 2, makeArrToList("2"), planId));
        LocalDate dateKey = LocalDate.of(2023, 7, 19);

        List<GroupResDto> filteredAll = groupService.all(planId, dateKey);

        assertThat(filteredAll.size()).isEqualTo(2);

    }

    @Test
    @DisplayName("일별 컬렉션 정상 조회 - empty result")
    void collectionFilteredByDateTestNormal_emptyResult() {

        Member member = makeMember("test@abc.co.kr", "3d^$a2df");
        PlanResDto planResDto = planService.save(new PlanReqDto("title", LocalDate.of(2023, 7, 19), LocalDate.of(2023, 7, 31), member.getId()));
        Long planId = planResDto.getId();
        groupService.save(new GroupReqDto("title1", 3, makeArrToList("화"), planId));
        groupService.save(new GroupReqDto("title2", 2, makeArrToList("2"), planId));
        groupService.save(new GroupReqDto("title3", 2, makeArrToList("3"), planId));
        LocalDate dateKey = LocalDate.of(2023, 7, 20);

        List<GroupResDto> filteredAll = groupService.all(planId, dateKey);

        assertThat(filteredAll).isEmpty();

    }

    @Test
    @DisplayName("일별 컬렉션 비정상 조회 - Resource not found")
    void collectionFilteredByDateTestNormal_resourceNotFound() {

        assertThrows(ResourceNotFoundException.class, () -> groupService.all(Long.MAX_VALUE, LocalDate.of(2023, 7, 19)));
    }

    @Test
    @DisplayName("기간 컬렉션 정상 조회 - 모든 group이 조회됨")
    void collectionFilteredByDateRangeTestNormal_allGroupSucceeded() {

        Member member = makeMember("test@abc.co.kr", "3d^$a2df");
        PlanResDto planResDto = planService.save(new PlanReqDto("title", LocalDate.of(2023, 7, 19), LocalDate.of(2023, 7, 31), member.getId()));
        Long planId = planResDto.getId();
        groupService.save(new GroupReqDto("title1", 3, makeArrToList("화"), planId));
        groupService.save(new GroupReqDto("title2", 2, makeArrToList("2"), planId));
        groupService.save(new GroupReqDto("title3", 2, makeArrToList("3"), planId));

        LocalDate searchStart = LocalDate.of(2023, 7, 25);
        LocalDate searchEnd = LocalDate.of(2023, 7, 25);

        List<GroupResDto> filteredAll = groupService.all(planId, searchStart, searchEnd);

        assertThat(filteredAll.size()).isEqualTo(3);

    }

    @Test
    @DisplayName("기간 컬렉션 정상 조회 - 등록한 Group의 일부만 조회됨")
    void collectionFilteredByDateRangeTestNormal_partOfThemSucceeded() {

        Member member = makeMember("test@abc.co.kr", "3d^$a2df");
        PlanResDto planResDto = planService.save(new PlanReqDto("title", LocalDate.of(2023, 7, 19), LocalDate.of(2023, 7, 31), member.getId()));
        Long planId = planResDto.getId();
        groupService.save(new GroupReqDto("title1", 3, makeArrToList("화"), planId));
        groupService.save(new GroupReqDto("title2", 2, makeArrToList("2"), planId));
        groupService.save(new GroupReqDto("title3", 2, makeArrToList("3"), planId));

        LocalDate searchStart = LocalDate.of(2023, 7, 26);
        LocalDate searchEnd = LocalDate.of(2023, 7, 29);

        List<GroupResDto> filteredAll = groupService.all(planId, searchStart, searchEnd);

        assertThat(filteredAll.size()).isEqualTo(2);

    }

    @Test
    @DisplayName("기간 컬렉션 정상 조회 - 등록한 Group이 모두 조회되지 않음")
    void collectionFilteredByDateRangeTestNormal_emptyResult() {

        Member member = makeMember("test@abc.co.kr", "3d^$a2df");
        PlanResDto planResDto = planService.save(new PlanReqDto("title", LocalDate.of(2023, 7, 19), LocalDate.of(2023, 7, 31), member.getId()));
        Long planId = planResDto.getId();
        groupService.save(new GroupReqDto("title1", 3, makeArrToList("화"), planId));
        groupService.save(new GroupReqDto("title2", 2, makeArrToList("2"), planId));
        groupService.save(new GroupReqDto("title3", 2, makeArrToList("3"), planId));

        LocalDate searchStart = LocalDate.of(2023, 7, 17);
        LocalDate searchEnd = LocalDate.of(2023, 7, 18);

        List<GroupResDto> filteredAll = groupService.all(planId, searchStart, searchEnd);

        assertThat(filteredAll.size()).isEqualTo(0);

    }

    @Test
    @DisplayName("기간 컬렉션 비정상 조회 - Resource not found")
    void collectionFilteredByDateRangeTestAbnormal_resourceNotFound() {

        assertThrows(ResourceNotFoundException.class,
                () -> groupService.all(Long.MAX_VALUE, LocalDate.of(2023, 7, 17), LocalDate.of(2023, 7, 18)));

    }

}
