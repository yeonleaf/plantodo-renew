package yeonleaf.plantodo.unit.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import yeonleaf.plantodo.domain.Member;
import yeonleaf.plantodo.dto.PlanReqDto;
import yeonleaf.plantodo.dto.PlanResDto;
import yeonleaf.plantodo.repository.MemoryPlanRepository;
import yeonleaf.plantodo.service.PlanService;
import yeonleaf.plantodo.service.PlanServiceTestImpl;

import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThat;

public class PlanServiceUnitTest {

    private PlanService planService;
    private MemoryPlanRepository planRepository;

    @BeforeEach
    void setUp() {
        planRepository = new MemoryPlanRepository();
        planService = new PlanServiceTestImpl(planRepository);
    }

    @Test
    @DisplayName("정상 저장")
    void saveTestNormal() {
        LocalDate start = LocalDate.now();
        LocalDate end = start.plusDays(3);
        PlanReqDto planReqDto = new PlanReqDto("title", start, end);

        Member member = new Member("test@abc.co.kr", "1d$%2av3");
        member.setId(1L);

        PlanResDto planResDto = planService.save(member, planReqDto);
        assertThat(planResDto.getId()).isNotNull();
        assertThat(planResDto.getTitle()).isEqualTo(planReqDto.getTitle());
        assertThat(planResDto.getStart()).isEqualTo(planReqDto.getStart());
        assertThat(planResDto.getEnd()).isEqualTo(planReqDto.getEnd());
    }

}
