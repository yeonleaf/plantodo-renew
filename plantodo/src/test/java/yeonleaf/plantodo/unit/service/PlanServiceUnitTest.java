package yeonleaf.plantodo.unit.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import yeonleaf.plantodo.domain.Group;
import yeonleaf.plantodo.domain.Member;
import yeonleaf.plantodo.dto.PlanReqDto;
import yeonleaf.plantodo.dto.PlanResDto;
import yeonleaf.plantodo.exceptions.ResourceNotFoundException;
import yeonleaf.plantodo.repository.MemoryGroupRepository;
import yeonleaf.plantodo.repository.MemoryPlanRepository;
import yeonleaf.plantodo.service.PlanService;
import yeonleaf.plantodo.service.PlanServiceTestImpl;

import java.time.LocalDate;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

public class PlanServiceUnitTest {

    private PlanService planService;
    private MemoryPlanRepository planRepository;
    private MemoryGroupRepository groupRepository;

    @BeforeEach
    void setUp() {

        planRepository = new MemoryPlanRepository();
        groupRepository = new MemoryGroupRepository();
        planService = new PlanServiceTestImpl(planRepository, groupRepository);

    }

    private Member makeMember() {
        Member member = new Member("test@abc.co.kr", "1d$%2av3");
        member.setId(1L);
        return member;
    }

    private PlanReqDto makePlanReq() {
        return new PlanReqDto("title", LocalDate.now(), LocalDate.now().plusDays(3));
    }

    @Test
    @DisplayName("정상 저장 - repOption이 0인 group 한 개를 생성")
    void saveTestNormal() {

        Member member = makeMember();
        PlanResDto planResDto = planService.save(member, makePlanReq());
        Group group = groupRepository.findByPlanId(planResDto.getId()).get();

        assertThat(planResDto.getId()).isNotNull();
        assertThat(group.getRepetition().getId()).isNotNull();
        assertThat(group.getRepetition().getRepOption()).isEqualTo(0);

    }

    @Test
    @DisplayName("정상 조회 - Plan 한 개 조회")
    void getOneTestNormal() {

        Member member = makeMember();
        PlanResDto planResDto = planService.save(member, makePlanReq());

        PlanResDto findOne = planService.one(planResDto.getId());

        assertThat(findOne.getId()).isEqualTo(planResDto.getId());

    }

    @Test
    @DisplayName("비정상 조회 - Plan 한 개 조회")
    void getOneTestAbnormal() {

        assertThrows(ResourceNotFoundException.class, () -> planService.one(1L));

    }

    @Test
    @DisplayName("정상 수정 - Plan 타이틀 수정")
    void updateOneTestNormal() {

        Member member = makeMember();
        PlanResDto plan = planService.save(member, makePlanReq());

        PlanReqDto updateDto = new PlanReqDto("revisedTitle", plan.getStart(), plan.getEnd());
        planService.update(plan.getId(), updateDto);

        PlanResDto findPlan = planService.one(plan.getId());
        assertThat(findPlan.getTitle()).isEqualTo("revisedTitle");
        assertThat(findPlan.getId()).isEqualTo(plan.getId());

    }

    @Test
    @DisplayName("비정상 수정 - Plan 타이틀 수정")
    void updateOneTestAbnormal() {

        PlanReqDto updateDto = new PlanReqDto("revisedTitle", LocalDate.now(), LocalDate.now().plusDays(3));
        assertThrows(ResourceNotFoundException.class, () -> planService.update(1L, updateDto));

    }

    @Test
    @DisplayName("정상 삭제")
    void deleteOneTestNormal() {

        Member member = makeMember();
        PlanResDto plan = planService.save(member, makePlanReq());

        planService.delete(plan.getId());

        assertThrows(ResourceNotFoundException.class, () -> planService.one(plan.getId()));

    }

    @Test
    @DisplayName("비정상 삭제")
    void deleteOneTestAbnormal() {

        assertThrows(ResourceNotFoundException.class, () -> planService.delete(1L));

    }

}
