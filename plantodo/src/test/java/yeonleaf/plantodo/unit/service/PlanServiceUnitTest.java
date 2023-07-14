package yeonleaf.plantodo.unit.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import yeonleaf.plantodo.domain.Group;
import yeonleaf.plantodo.domain.Member;
import yeonleaf.plantodo.dto.PlanReqDto;
import yeonleaf.plantodo.dto.PlanResDto;
import yeonleaf.plantodo.repository.MemoryGroupRepository;
import yeonleaf.plantodo.repository.MemoryPlanRepository;
import yeonleaf.plantodo.service.PlanService;
import yeonleaf.plantodo.service.PlanServiceTestImpl;

import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThat;

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

    @Test
    @DisplayName("정상 저장 - repOption이 0인 group 한 개를 생성")
    void saveTestNormal() {

        LocalDate start = LocalDate.now();
        LocalDate end = start.plusDays(3);
        PlanReqDto planReqDto = new PlanReqDto("title", start, end);

        Member member = new Member("test@abc.co.kr", "1d$%2av3");
        member.setId(1L);

        PlanResDto planResDto = planService.save(member, planReqDto);
        Group group = groupRepository.findByPlanId(planResDto.getId()).get();

        assertThat(planResDto.getId()).isNotNull();
        assertThat(group.getRepetition().getId()).isNotNull();
        assertThat(group.getRepetition().getRepOption()).isEqualTo(0);

    }

}
