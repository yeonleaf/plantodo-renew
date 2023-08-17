package yeonleaf.plantodo.unit.service;

import org.junit.jupiter.api.AfterEach;
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
import yeonleaf.plantodo.service.GroupService;

import java.time.LocalDate;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

/**
 * target : {@link GroupServiceTestImpl}에 있는 모든 메소드
 * target description : {@link GroupService}의 테스트용 구현체
 *                      {@link MemoryRepository}를 상속받은 서브클래스들을 Repository로 주입받음 (DB를 메모리로 대신)
 */
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
     * 회원 등록 보조 메소드
     */
    private Member makeMember(String email, String password) {
        Member member = new Member(email, password);
        memberRepository.save(member);
        return member;
    }
    
    /**
     * 테스트 메이커
     * @param repOption (int) 할 일이 반복되는 양상을 설정하는 옵션
     *                   1(매일 반복), 2(기간 반복), 3(요일 반복)
     * @param repValue (List<String>) 할 일이 반복되는 주기를 설정하는 옵션
     * @param start 일정 시작일
     * @param end 일정 종료일
     * @param expectedCnt 생성된 할일 개수의 예상값
     */
    private void makeSaveRepOptionTest(int repOption, List<String> repValue, LocalDate start, LocalDate end, int expectedCnt) {

        // given
        Member member = makeMember("test@abc.co.kr", "3d^$a2df");
        Plan plan = planRepository.save(new Plan("plan", start, end, member));

        // when
        GroupResDto group = groupService.save(new GroupReqDto("group", repOption, repValue, plan.getId()));

        // then
        List<Checkbox> checkboxes = checkboxRepository.findByGroupId(group.getId());
        assertThat(checkboxes.size()).isEqualTo(expectedCnt);

    }

    /**
     * 할일 그룹 등록 메소드 관련 테스트
     * @see GroupServiceTestImpl#save(GroupReqDto)
     * 할일 그룹 등록 후 생성된 할일의 개수가 예상치와 같은지 확인한다.
     */

    @Test
    @DisplayName("정상 등록 - repOption = 1, start < end으로 할일 그룹을 생성할 경우 (end - start + 1)을 리턴한다.")
    void saveTestRepOption1_EndGreaterThanStart() {

        makeSaveRepOptionTest(1, List.of(), LocalDate.of(2023, 7, 18), LocalDate.of(2023, 7, 31), 14);

    }

    @Test
    @DisplayName("정상 등록 - repOption = 1, start = end으로 할일 그룹을 생성할 경우 1을 리턴한다.")
    void saveTestRepOption1_EndEqualToStart() {

        makeSaveRepOptionTest(1, List.of(), LocalDate.of(2023, 7, 18), LocalDate.of(2023, 7, 18), 1);

    }

    @Test
    @DisplayName("정상 등록 - repOption = 2, start < end으로 할일 그룹을 생성할 경우 (end - start + 1) / repValue을 리턴한다.")
    void saveTestRepOption2_EndGreaterThanStart() {

        makeSaveRepOptionTest(2, List.of("2"), LocalDate.of(2023, 7, 18), LocalDate.of(2023, 7, 31), 7);

    }

    @Test
    @DisplayName("정상 등록 - repOption = 2, start = end으로 할일 그룹을 생성할 경우 1을 리턴한다.")
    void saveTestRepOption2_EndEqualToStart() {

        makeSaveRepOptionTest(2, List.of("2"), LocalDate.of(2023, 7, 18), LocalDate.of(2023, 7, 18), 1);

    }

    @Test
    @DisplayName("정상 등록 - repOption = 3L, start < end으로 할일 그룹을 생성할 경우 일정 시작일부터 종료일까지의 날짜 중에서 요일이 repValue에 있는 날짜에 할일을 생성해야 한다.")
    void saveTestRepOption3L_EndGreaterThanStart() {

        makeSaveRepOptionTest(3, List.of("월", "수", "금"), LocalDate.of(2023, 7, 18), LocalDate.of(2023, 7, 31), 6);

    }

    @Test
    @DisplayName("정상 등록 - repOption = 3L, start = end으로 할일 그룹을 생성한 경우 일정 시작일의 요일이 repValue에 있는 경우 1, 아닌 경우 0을 리턴한다.")
    void saveTestRepOption3_EndEqualToStart() {

        makeSaveRepOptionTest(3, List.of("월", "수", "금"), LocalDate.of(2023, 7, 18), LocalDate.of(2023, 7, 18), 0);

    }


    /**
     * 할일 그룹 단건 조회 메소드 관련 테스트
     * @see GroupServiceTestImpl#one(Long)
     * 조회한 할일 그룹의 내용이 저장한 할일 그룹의 내용과 같은지 확인한다.
     */
    @Test
    @DisplayName("단건 정상 조회 - 조회한 할일 그룹의 내용이 저장한 할일 그룹의 내용과 같은지 확인한다.")
    void oneTestNormal() {

        // given
        Member member = makeMember("test@abc.co.kr", "3d^$a2df");
        Plan plan = planRepository.save(new Plan("plan", LocalDate.of(2023, 7, 18), LocalDate.of(2023, 7, 31), member));
        GroupResDto savedGroup = groupService.save(new GroupReqDto("group", 3, List.of("화", "목"), plan.getId()));

        // when
        GroupResDto groupResDto = groupService.one(savedGroup.getId());

        // then
        assertThat(groupResDto.equals(savedGroup)).isTrue();
        assertThat(groupResDto.getRepValue()).isEqualTo(List.of("화", "목"));

    }

    @Test
    @DisplayName("단건 비정상 조회 - 대상이 없는 경우 ResourceNotFoundException을 던지는지 확인한다.")
    void oneTestAbnormal() {

        // given - when - then
        assertThrows(ResourceNotFoundException.class, () -> groupService.one(Long.MAX_VALUE));

    }


    /**
     * 할일 그룹 수정 관련 테스트
     * @see GroupServiceTestImpl#update(GroupUpdateReqDto)
     *
     * title 할일 그룹의 타이틀
     * repOption (int) 할 일이 반복되는 양상을 설정하는 옵션
     *           1(매일 반복), 2(기간 반복), 3(요일 반복)
     * repValue  (List<String>) 할 일이 반복되는 주기를 설정하는 옵션
     *
     * 1. 타이틀, repOption, repValue에 변경사항이 없는 경우 변경 전 할일 그룹과 변경 후 할일 그룹의 내용이 일치하는지 확인한다.
     * 2. 타이틀에만 변경사항이 있는 경우 타이틀이 정상적으로 변경되었는지 확인하고 변경 전 할일 그룹과 변경 후 할일 그룹의 내용이 타이틀을 제외하고 모두 일치하는지 확인한다.
     * 3. repOption과 repValue에 변경사항이 있고 타이틀에는 변경사항이 없는 경우
     * 4. repValue에만 변경사항이 있고 타이틀, repOption에는 변경사항이 없는 경우
     * 5.
     */
    @Test
    @DisplayName("정상 수정 - 타이틀, repOption, repValue에 변경사항이 없는 경우 변경 전 할일 그룹과 변경 후 할일 그룹의 내용이 일치하는지 확인한다.")
    void updateTestNormal_notChanged() {

        // given
        Member member = makeMember("test@abc.co.kr", "3d^$a2df");
        Plan plan = planRepository.save(new Plan("plan", LocalDate.of(2023, 7, 18), LocalDate.of(2023, 7, 31), member));
        GroupResDto savedGroup = groupService.save(new GroupReqDto("group", 3, List.of("화", "목"), plan.getId()));
        GroupUpdateReqDto groupUpdateReqDto = new GroupUpdateReqDto(savedGroup.getId(), "group", 3, List.of("화", "목"));

        // when
        GroupResDto updatedGroup = groupService.update(groupUpdateReqDto);

        // then
        assertThat(updatedGroup.getTitle()).isEqualTo(savedGroup.getTitle());
        assertThat(updatedGroup.getRepOption()).isEqualTo(savedGroup.getRepOption());
        assertThat(updatedGroup.getRepValue()).isEqualTo(savedGroup.getRepValue());

    }

    @Test
    @DisplayName("정상 수정 - 타이틀에만 변경사항이 있는 경우 타이틀이 정상적으로 변경되었는지 확인하고 " +
            "변경 전 할일 그룹과 변경 후 할일 그룹의 내용이 타이틀을 제외하고 모두 일치하는지 확인한다.")
    void updateTestNormal_changedOnlyTitle() {

        // given
        Member member = makeMember("test@abc.co.kr", "3d^$a2df");
        Plan plan = planRepository.save(new Plan("plan", LocalDate.of(2023, 7, 18), LocalDate.of(2023, 7, 31), member));
        GroupResDto savedGroup = groupService.save(new GroupReqDto("group", 3, List.of("화", "목"), plan.getId()));
        GroupUpdateReqDto groupUpdateReqDto = new GroupUpdateReqDto(savedGroup.getId(), "updatedGroup", 3, List.of("화", "목"));

        // when
        groupService.update(groupUpdateReqDto);

        // then
        GroupResDto groupResDto = groupService.one(savedGroup.getId());
        assertThat(groupResDto.getTitle()).isEqualTo("updatedGroup");

    }

    @Test
    @DisplayName("정상 수정 - repOption, repValue에 변경사항이 있고 타이틀에는 변경사항이 없는 경우 " +
            "할일 그룹 id로 모든 할일을 조회했을 때 변경된 repOption, repValue에 맞는 할일만 생성되어 있는지 확인한다.")
    void updateTestNormal_changedRepOptionAndRepValue() {

        // given
        Member member = makeMember("test@abc.co.kr", "3d^$a2df");
        Plan plan = planRepository.save(new Plan("plan", LocalDate.of(2023, 7, 18), LocalDate.of(2023, 7, 25), member));
        GroupResDto savedGroup = groupService.save(new GroupReqDto("group", 3, List.of("화", "목"), plan.getId()));
        GroupUpdateReqDto groupUpdateReqDto = new GroupUpdateReqDto(savedGroup.getId(), "group", 2, List.of("3"));

        // when
        GroupResDto updatedGroup = groupService.update(groupUpdateReqDto);

        // then
        Stream<LocalDate> dateResult = checkboxRepository.findByGroupId(updatedGroup.getId()).stream().map(Checkbox::getDate);
        assertThat(dateResult).containsOnly(
                LocalDate.of(2023, 7, 18),
                LocalDate.of(2023, 7, 21),
                LocalDate.of(2023, 7, 24)
        );

    }

    @Test
    @DisplayName("정상 수정 - repValue에만 변경사항이 있는 경우 할일 그룹 id로 모든 할일을 조회했을 때 변경된 repValue에 맞는 할일만 생성되어 있는지 확인한다.")
    void updateTestNormal_changedOnlyRepValue() {

        // given
        Member member = makeMember("test@abc.co.kr", "3d^$a2df");
        Plan plan = planRepository.save(new Plan("plan", LocalDate.of(2023, 7, 18), LocalDate.of(2023, 7, 25), member));
        GroupResDto savedGroup = groupService.save(new GroupReqDto("group", 3, List.of("화", "목"), plan.getId()));
        GroupUpdateReqDto groupUpdateReqDto = new GroupUpdateReqDto(savedGroup.getId(), "group", 3, List.of("월", "수", "금"));

        // when
        GroupResDto updatedGroup = groupService.update(groupUpdateReqDto);

        // then
        Stream<LocalDate> dateResult = checkboxRepository.findByGroupId(updatedGroup.getId()).stream().map(Checkbox::getDate);
        assertThat(dateResult).containsOnly(
                LocalDate.of(2023, 7, 19),
                LocalDate.of(2023, 7, 21),
                LocalDate.of(2023, 7, 24)
        );

    }

    @Test
    @DisplayName("정상 수정 - 타이틀, repOption, repValue 모두 변경사항이 있는 경우 " +
            "타이틀 변경이 정상적으로 이루어졌는지 확인하고 할일 그룹 id로 모든 할일을 조회했을 때 repOption, repValue에 맞는 할일만 생성되어 있는지 확인한다.")
    void updateTestNormal_changedAll() {

        // given
        Member member = makeMember("test@abc.co.kr", "3d^$a2df");
        Plan plan = planRepository.save(new Plan("plan", LocalDate.of(2023, 7, 18), LocalDate.of(2023, 7, 25), member));
        GroupResDto savedGroup = groupService.save(new GroupReqDto("title", 3, List.of("화", "목"), plan.getId()));
        GroupUpdateReqDto groupUpdateReqDto = new GroupUpdateReqDto(savedGroup.getId(), "updatedTitle", 2, List.of("3"));

        // when
        GroupResDto updatedGroup = groupService.update(groupUpdateReqDto);

        // then
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
    @DisplayName("정상 수정 - 타이틀, repValue에만 변경사항이 있는 경우 타이틀 변경이 정상적으로 이루어졌는지 확인하고 " +
            "할일 그룹 id로 모든 할일을 조회했을 때 변경된 repValue에 맞는 할일만 생성되어 있는지 확인한다.")
    void updateTestNormal_changedTitleAndRepValue() {

        // given
        Member member = makeMember("test@abc.co.kr", "3d^$a2df");
        Plan plan = planRepository.save(new Plan("plan", LocalDate.of(2023, 7, 18), LocalDate.of(2023, 7, 25), member));
        GroupResDto savedGroup = groupService.save(new GroupReqDto("title", 3, List.of("화", "목"), plan.getId()));
        GroupUpdateReqDto groupUpdateReqDto = new GroupUpdateReqDto(savedGroup.getId(), "updatedTitle", 3, List.of("월", "일"));

        // when
        GroupResDto updatedGroup = groupService.update(groupUpdateReqDto);

        // then
        GroupResDto groupResDto = groupService.one(updatedGroup.getId());
        assertThat(groupResDto.getTitle()).isEqualTo("updatedTitle");

        Stream<LocalDate> dateResult = checkboxRepository.findByGroupId(updatedGroup.getId()).stream().map(Checkbox::getDate);
        assertThat(dateResult).containsOnly(
                LocalDate.of(2023, 7, 23),
                LocalDate.of(2023, 7, 24)
        );

    }

    @Test
    @DisplayName("비정상 수정 - 수정할 할일 그룹이 없는 경우 ResourceNotFoundException을 던지는지 확인한다.")
    void updateTestAbnormal_resourceNotFound() {

        // given
        GroupUpdateReqDto groupUpdateReqDto = new GroupUpdateReqDto(Long.MAX_VALUE, "updatedTitle", 3, List.of("월", "일"));

        // when - then
        assertThrows(ResourceNotFoundException.class, () -> groupService.update(groupUpdateReqDto));

    }


    /**
     * 할일 그룹 삭제 메소드 관련 테스트
     * @see GroupServiceTestImpl#delete(Long)
     * 할일 그룹이 삭제되었을 때 연관된 Repetition과 Checkbox가 모두 삭제되었는지 확인한다.
     */
    @Test
    @DisplayName("정상 삭제 - 할일 그룹을 삭제하고 할일 그룹을 기준으로 Repetition과 Checkbox를 조회했을 때 빈 결과값을 리턴한다.")
    void deleteTestNormal() {

        // given
        Member member = makeMember("test@abc.co.kr", "3d^$a2df");
        Plan plan = planRepository.save(new Plan("plan", LocalDate.of(2023, 7, 18), LocalDate.of(2023, 7, 25), member));
        GroupResDto savedGroup = groupService.save(new GroupReqDto("title", 3, List.of("화", "목"), plan.getId()));
        Group findGroup = groupRepository.findById(savedGroup.getId()).orElseThrow(ResourceNotFoundException::new);
        Long groupId = findGroup.getId();
        Long repetitionId = findGroup.getRepetition().getId();

        // when
        groupService.delete(groupId);

        // then
        List<Checkbox> findCheckboxes = checkboxRepository.findByGroupId(groupId);
        Optional<Repetition> findRepetition = repetitionRepository.findById(repetitionId);
        assertThat(findCheckboxes).isEmpty();
        assertThat(findRepetition).isEmpty();

    }

    @Test
    @DisplayName("비정상 삭제 - 삭제할 할일 그룹이 없으면 ResourceNotFoundException을 던지는지 확인한다.")
    void deleteTestAbnormal_resourceNotFound() {

        // given - when - then
        assertThrows(ResourceNotFoundException.class, () -> groupService.delete(Long.MAX_VALUE));

    }


    /**
     * 할일 그룹 순수 컬렉션 조회 메소드 관련 테스트
     * 순수 컬렉션 조회 메소드란 필터링이 걸려 있지 않은 컬렉션 조회 메소드를 의미함
     * @see GroupServiceTestImpl#all(Long)
     * 할일 그룹을 저장한 개수와 조회한 개수가 같은지 확인한다.
     */
    @Test
    @DisplayName("정상 순수 컬렉션 조회 - 저장한 할일 그룹의 개수와 조회한 할일 그룹의 개수가 같은지 확인한다.")
    void allTestNormal() {

        // given
        Member member = makeMember("test@abc.co.kr", "3d^$a2df");
        PlanResDto planResDto = planService.save(new PlanReqDto("title", LocalDate.of(2023, 7, 18), LocalDate.of(2023, 7, 25), member.getId()));
        Long planId = planResDto.getId();
        groupService.save(new GroupReqDto("title1", 3, List.of("화", "목"), planId));
        groupService.save(new GroupReqDto("title2", 3, List.of("화", "목"), planId));
        groupService.save(new GroupReqDto("title3", 3, List.of("화", "목"), planId));

        // when
        List<GroupResDto> all = groupService.all(planId);

        // then
        assertThat(all.size()).isEqualTo(3);

    }

    @Test
    @DisplayName("비정상 순수 컬렉션 조회 - 조회 기준으로 삼을 일정이 없는 경우 ResourceNotFoundException을 던지는지 확인한다.")
    void allTestAbnormal() {

        // given - when - then
        assertThrows(ResourceNotFoundException.class, () -> groupService.all(Long.MAX_VALUE));

    }


    /**
     * 할일 그룹 일별 컬렉션 조회 메소드 관련 테스트
     * 일별 컬렉션은 검색일에 하나의 할일이라도 가지고 있는 할일 그룹의 컬렉션을 의미한다.
     * @see GroupServiceTestImpl#all(Long, LocalDate)
     */
    @Test
    @DisplayName("일별 컬렉션 정상 조회 - 저장한 세 개의 그룹이 검색일에 할일을 생성하는 경우 조회 결과 리스트의 사이즈는 3이다.")
    void collectionFilteredByDateTestNormal_allMatchedWithKey() {

        // given
        Member member = makeMember("test@abc.co.kr", "3d^$a2df");
        PlanResDto planResDto = planService.save(new PlanReqDto("title", LocalDate.of(2023, 7, 19), LocalDate.of(2023, 7, 31), member.getId()));
        Long planId = planResDto.getId();
        groupService.save(new GroupReqDto("title1", 3, List.of("화", "목"), planId));
        groupService.save(new GroupReqDto("title2", 1, List.of(), planId));
        groupService.save(new GroupReqDto("title3", 2, List.of("2"), planId));
        LocalDate dateKey = LocalDate.of(2023, 7, 25);

        // when
        List<GroupResDto> filteredAll = groupService.all(planId, dateKey);

        // then
        assertThat(filteredAll.size()).isEqualTo(3);

    }

    @Test
    @DisplayName("일별 컬렉션 정상 조회 - 저장한 세 개의 그룹 중 두 개의 그룹만 검색일에 할일을 생성하는 경우 조회 결과 리스트의 사이즈는 2이다.")
    void collectionFilteredByDateTestNormal_partOfThemMatchedWithKey() {

        // given
        Member member = makeMember("test@abc.co.kr", "3d^$a2df");
        PlanResDto planResDto = planService.save(new PlanReqDto("title", LocalDate.of(2023, 7, 19), LocalDate.of(2023, 7, 31), member.getId()));
        Long planId = planResDto.getId();
        groupService.save(new GroupReqDto("title1", 3, List.of("화", "목"), planId));
        groupService.save(new GroupReqDto("title2", 1, List.of(), planId));
        groupService.save(new GroupReqDto("title3", 2, List.of("2"), planId));
        LocalDate dateKey = LocalDate.of(2023, 7, 19);

        // when
        List<GroupResDto> filteredAll = groupService.all(planId, dateKey);

        // then
        assertThat(filteredAll.size()).isEqualTo(2);

    }

    @Test
    @DisplayName("일별 컬렉션 정상 조회 - 저장한 세 개의 그룹 중 어떤 그룹도 검색일에 할일을 생성하지 않는 경우 조회 결과 리스트의 사이즈는 0이다.")
    void collectionFilteredByDateTestNormal_emptyResult() {

        // given
        Member member = makeMember("test@abc.co.kr", "3d^$a2df");
        PlanResDto planResDto = planService.save(new PlanReqDto("title", LocalDate.of(2023, 7, 19), LocalDate.of(2023, 7, 31), member.getId()));
        Long planId = planResDto.getId();
        groupService.save(new GroupReqDto("title1", 3, List.of("화"), planId));
        groupService.save(new GroupReqDto("title2", 2, List.of("2"), planId));
        groupService.save(new GroupReqDto("title3", 2, List.of("3"), planId));
        LocalDate dateKey = LocalDate.of(2023, 7, 20);

        // when
        List<GroupResDto> filteredAll = groupService.all(planId, dateKey);

        // then
        assertThat(filteredAll).isEmpty();

    }

    @Test
    @DisplayName("일별 컬렉션 비정상 조회 - 기준으로 삼을 일정을 조회할 수 없는 경우 ResourceNotFoundException을 던지는지 확인한다.")
    void collectionFilteredByDateTestNormal_resourceNotFound() {

        // given - when - then
        assertThrows(ResourceNotFoundException.class, () -> groupService.all(Long.MAX_VALUE, LocalDate.of(2023, 7, 19)));

    }


    /**
     * 할일 그룹 기간 컬렉션 조회 메소드 관련 테스트
     * 기간 컬렉션이란 검색 시작일 ~ 검색 종료일 사이에 할일을 하나라도 가지고 있는 할일 그룹의 컬렉션을 의미한다.
     * @see GroupServiceTestImpl#all(Long, LocalDate, LocalDate)
     */
    @Test
    @DisplayName("기간 컬렉션 정상 조회 - 저장한 세 개의 할일 그룹이 모두 기간 안에 하나 이상의 할일을 생성하는 경우 조회 결과 리스트의 사이즈는 3이다.")
    void collectionFilteredByDateRangeTestNormal_allGroupSucceeded() {

        // given
        Member member = makeMember("test@abc.co.kr", "3d^$a2df");
        PlanResDto planResDto = planService.save(new PlanReqDto("title", LocalDate.of(2023, 7, 19), LocalDate.of(2023, 7, 31), member.getId()));
        Long planId = planResDto.getId();
        groupService.save(new GroupReqDto("title1", 3, List.of("화"), planId));
        groupService.save(new GroupReqDto("title2", 2, List.of("2"), planId));
        groupService.save(new GroupReqDto("title3", 2, List.of("3"), planId));

        LocalDate searchStart = LocalDate.of(2023, 7, 25);
        LocalDate searchEnd = LocalDate.of(2023, 7, 25);

        // when
        List<GroupResDto> filteredAll = groupService.all(planId, searchStart, searchEnd);

        // then
        assertThat(filteredAll.size()).isEqualTo(3);

    }

    @Test
    @DisplayName("기간 컬렉션 정상 조회 - 저장한 세 개의 할일 그룹 중 두 개가 기간 안에 하나 이상의 할일을 생성하는 경우 조회 결과 리스트의 사이즈는 2이다.")
    void collectionFilteredByDateRangeTestNormal_partOfThemSucceeded() {

        // given
        Member member = makeMember("test@abc.co.kr", "3d^$a2df");
        PlanResDto planResDto = planService.save(new PlanReqDto("title", LocalDate.of(2023, 7, 19), LocalDate.of(2023, 7, 31), member.getId()));
        Long planId = planResDto.getId();
        groupService.save(new GroupReqDto("title1", 3, List.of("화"), planId));
        groupService.save(new GroupReqDto("title2", 2, List.of("2"), planId));
        groupService.save(new GroupReqDto("title3", 2, List.of("3"), planId));

        LocalDate searchStart = LocalDate.of(2023, 7, 26);
        LocalDate searchEnd = LocalDate.of(2023, 7, 29);

        // when
        List<GroupResDto> filteredAll = groupService.all(planId, searchStart, searchEnd);

        // then
        assertThat(filteredAll.size()).isEqualTo(2);

    }

    @Test
    @DisplayName("기간 컬렉션 정상 조회 - 저장한 세 개의 할일 그룹 중에 어떠한 그룹도 기간 안에 하나 이상의 할일을 생성하지 못하는 경우 조회 결과 리스트의 사이즈는 0이다.")
    void collectionFilteredByDateRangeTestNormal_emptyResult() {

        // given
        Member member = makeMember("test@abc.co.kr", "3d^$a2df");
        PlanResDto planResDto = planService.save(new PlanReqDto("title", LocalDate.of(2023, 7, 19), LocalDate.of(2023, 7, 31), member.getId()));
        Long planId = planResDto.getId();
        groupService.save(new GroupReqDto("title1", 3, List.of("화"), planId));
        groupService.save(new GroupReqDto("title2", 2, List.of("2"), planId));
        groupService.save(new GroupReqDto("title3", 2, List.of("3"), planId));

        LocalDate searchStart = LocalDate.of(2023, 7, 17);
        LocalDate searchEnd = LocalDate.of(2023, 7, 18);

        // when
        List<GroupResDto> filteredAll = groupService.all(planId, searchStart, searchEnd);

        // then
        assertThat(filteredAll.size()).isEqualTo(0);

    }

    @Test
    @DisplayName("기간 컬렉션 비정상 조회 - 기준으로 삼을 일정을 조회할 수 없는 경우 ResourceNotFoundException을 던지는지 확인한다.")
    void collectionFilteredByDateRangeTestAbnormal_resourceNotFound() {

        // given - when - then
        assertThrows(ResourceNotFoundException.class,
                () -> groupService.all(Long.MAX_VALUE, LocalDate.of(2023, 7, 17), LocalDate.of(2023, 7, 18)));

    }

}
