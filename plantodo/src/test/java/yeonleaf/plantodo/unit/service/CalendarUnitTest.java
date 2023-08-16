package yeonleaf.plantodo.unit.service;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import yeonleaf.plantodo.ServiceTestConfig;
import yeonleaf.plantodo.dto.*;
import yeonleaf.plantodo.service.CheckboxServiceTestImpl;
import yeonleaf.plantodo.service.GroupServiceTestImpl;
import yeonleaf.plantodo.service.MemberServiceTestImpl;
import yeonleaf.plantodo.service.PlanServiceTestImpl;
import yeonleaf.plantodo.util.DateRange;

import java.time.LocalDate;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@ExtendWith(SpringExtension.class)
@ContextConfiguration(classes = ServiceTestConfig.class)
public class CalendarUnitTest {

    @Autowired
    private MemberServiceTestImpl memberService;

    @Autowired
    private PlanServiceTestImpl planService;

    @Autowired
    private GroupServiceTestImpl groupService;

    @Autowired
    private CheckboxServiceTestImpl checkboxService;

    DateRange dateRange = new DateRange();

    @Test
    void calendarBasicTest() {
        List<LocalDate> dates = dateRange.between(LocalDate.of(2023, 8, 15), LocalDate.of(2023, 8, 30));
        dates.forEach(System.out::println);
    }
    

    @Test
    void calendarBasicTest2() {

        MemberResDto member = memberService.save(new MemberReqDto("test@abc.co.kr", "3df#$sa2"));
        PlanResDto plan1 = planService.save(new PlanReqDto("planTitle1", LocalDate.of(2023, 8, 16), LocalDate.of(2023, 8, 25), member.getId()));
        GroupResDto group1 = groupService.save(new GroupReqDto("groupTitle1", 1, List.of(), plan1.getId()));
        CheckboxResDto checkbox1 = checkboxService.save(new CheckboxReqDto("checkboxTitle1", plan1.getId(), LocalDate.of(2023, 8, 18)));

        PlanResDto plan2 = planService.save(new PlanReqDto("planTitle1", LocalDate.of(2023, 8, 20), LocalDate.of(2023, 8, 23), member.getId()));
        GroupResDto group2 = groupService.save(new GroupReqDto("groupTitle1", 2, List.of("2"), plan2.getId()));
        CheckboxResDto checkbox2 = checkboxService.save(new CheckboxReqDto("checkboxTitle1", plan2.getId(), LocalDate.of(2023, 8, 22)));

        LinkedHashMap<LocalDate, LinkedHashMap<PlanResDto, List<CheckboxResDto>>> result = new LinkedHashMap<>();
        List<LocalDate> dates = dateRange.between(LocalDate.of(2023, 8, 15), LocalDate.of(2023, 8, 30));
        dates.forEach(date -> {
            LinkedHashMap<PlanResDto, List<CheckboxResDto>> tmp = new LinkedHashMap<>();
            List<PlanResDto> allPlans = planService.all(member.getId(), date);
            allPlans.forEach(plan -> {
                List<CheckboxResDto> allCheckboxes = checkboxService.allByPlan(plan.getId(), date);
                tmp.put(plan, allCheckboxes);
            });
            result.put(date, tmp);
        });

        System.out.println(result.get(LocalDate.of(2023, 8, 22)).toString());
        assertThat(result.get(LocalDate.of(2023, 8, 22)).keySet().size()).isEqualTo(2);
        assertThat(result.get(LocalDate.of(2023, 8, 27))).isEmpty();
        assertThat(result.get(LocalDate.of(2023, 8, 16)).keySet().size()).isEqualTo(1);
    }

}
