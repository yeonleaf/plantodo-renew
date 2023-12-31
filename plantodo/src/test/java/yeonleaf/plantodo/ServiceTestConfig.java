package yeonleaf.plantodo;

import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import yeonleaf.plantodo.converter.RepInToOutConverter;
import yeonleaf.plantodo.converter.RepOutToInConverter;
import yeonleaf.plantodo.domain.Checkbox;
import yeonleaf.plantodo.domain.Group;
import yeonleaf.plantodo.domain.Member;
import yeonleaf.plantodo.domain.Plan;
import yeonleaf.plantodo.repository.*;
import yeonleaf.plantodo.service.*;
import yeonleaf.plantodo.util.DateRange;
import yeonleaf.plantodo.validator.RepInputValidator;

@TestConfiguration
public class ServiceTestConfig {

    @Bean
    public MemoryMemberRepository memberRepository() {
        return new MemoryMemberRepository();
    }

    @Bean
    public MemoryRepetitionRepository repetitionRepository() {
        return new MemoryRepetitionRepository();
    }

    @Bean
    public MemoryGroupRepository groupRepository() {
        return new MemoryGroupRepository(repetitionRepository());
    }

    @Bean
    public MemoryCheckboxRepository checkboxRepository() {
        return new MemoryCheckboxRepository();
    }

    @Bean
    public MemoryPlanRepository planRepository() {
        return new MemoryPlanRepository();
    }

    @Bean
    public RepInToOutConverter repInToOutConverter() {
        return new RepInToOutConverter();
    }

    @Bean
    public RepOutToInConverter repOutToInConverter() {
        return new RepOutToInConverter();
    }

    @Bean
    public CheckboxServiceTestImpl checkboxService() {
        return new CheckboxServiceTestImpl(planRepository(), groupRepository(), checkboxRepository());
    }

    @Bean
    public RepInputValidator repInputValidator() {
        return new RepInputValidator();
    }

    @Bean
    public DateRange dateRange() {
        return new DateRange();
    }

    @Bean
    public GroupServiceTestImpl groupService() {
        return new GroupServiceTestImpl(planRepository(), groupRepository(), checkboxRepository(),
                repInToOutConverter(), repOutToInConverter(), repInputValidator(), dateRange());
    }

    @Bean
    public PlanServiceTestImpl planService() {
        return new PlanServiceTestImpl(memberRepository(), planRepository(), groupRepository(), checkboxRepository(), groupService());
    }

    @Bean
    public MemberServiceTestImpl memberService() {
        return new MemberServiceTestImpl(memberRepository());
    }

}
