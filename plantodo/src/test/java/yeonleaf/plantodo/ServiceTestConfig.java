package yeonleaf.plantodo;

import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import yeonleaf.plantodo.converter.RepInToOutConverter;
import yeonleaf.plantodo.converter.RepOutToInConverter;
import yeonleaf.plantodo.domain.Member;
import yeonleaf.plantodo.repository.*;
import yeonleaf.plantodo.service.*;

@TestConfiguration
public class ServiceTestConfig {

    @Bean
    public MemoryRepository<Member> memberRepository() {
        return new MemoryMemberRepository();
    }

    @Bean
    public MemoryGroupRepository groupRepository() {
        return new MemoryGroupRepository();
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
        return new CheckboxServiceTestImpl(checkboxRepository());
    }

    @Bean
    public GroupServiceTestImpl groupService() {
        return new GroupServiceTestImpl(planRepository(), groupRepository(), checkboxService(), repInToOutConverter(), repOutToInConverter());
    }

    @Bean
    public PlanServiceTestImpl planService() {
        return new PlanServiceTestImpl(memberRepository(), planRepository(), groupService());
    }

    @Bean
    public MemberServiceTestImpl memberService() {
        return new MemberServiceTestImpl((MemoryMemberRepository) memberRepository());
    }

}