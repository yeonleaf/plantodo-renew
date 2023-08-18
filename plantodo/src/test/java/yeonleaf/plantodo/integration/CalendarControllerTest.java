package yeonleaf.plantodo.integration;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.transaction.Transactional;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;
import org.springframework.validation.BindingResult;
import yeonleaf.plantodo.dto.*;
import yeonleaf.plantodo.provider.JwtBasicProvider;
import yeonleaf.plantodo.service.CheckboxService;
import yeonleaf.plantodo.service.GroupService;
import yeonleaf.plantodo.service.MemberService;
import yeonleaf.plantodo.service.PlanService;
import yeonleaf.plantodo.controller.CalendarController;

import java.time.LocalDate;
import java.util.List;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * target : {@link CalendarController}에 있는 모든 메소드
 * target description : 날짜별로 여러 종류의 엔티티를 조회하는 API
 * test description : 인터셉터를 포함한 통합 테스트
 *                    모든 요청의 Authorization 헤더에 Jwt 토큰을 포함시켜야 한다.
 *                    메소드별로 고정된 시나리오로 여러 가지 경우의 수 (조회할 원소가 없는 경우, 있는 경우, 있다면 몇 개의 원소가 있는지)를 테스트한다.
 */
@Transactional
@SpringBootTest
@AutoConfigureMockMvc
public class CalendarControllerTest {

    @Autowired
    private MemberService memberService;

    @Autowired
    private PlanService planService;

    @Autowired
    private CheckboxService checkboxService;

    @Autowired
    private GroupService groupService;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private JwtBasicProvider jwtProvider;

    /**
     * 기간 캘린더 조회 API 보조 메소드
     * plan id 세 개를 받아서 그룹 두 개와 일일 할일 세 개를 생성하는 메소드
     */
    private void scenario1_createGroupsAndCheckboxes(Long plan1_id, Long plan2_id, Long plan3_id) {

        groupService.save(new GroupReqDto("group 1", 2, List.of("2"), plan1_id));
        groupService.save(new GroupReqDto("group 2", 3, List.of("월", "수", "금"), plan2_id));

        checkboxService.save(new CheckboxReqDto("checkbox 1", plan3_id, LocalDate.of(2023, 8, 13)));
        checkboxService.save(new CheckboxReqDto("checkbox 1", plan3_id, LocalDate.of(2023, 8, 15)));
        checkboxService.save(new CheckboxReqDto("checkbox 1", plan3_id, LocalDate.of(2023, 8, 20)));

    }

    /**
     * 기간 캘린더 조회 API 관련 테스트
     * 검색 시작일부터 종료일까지의 날짜별로 일정과 일정과 연관된 할일 (그룹 할일, 일일 할일)을 함께 조회하는 API
     * @see CalendarController#getByRange(CalendarRangeReqDto, BindingResult)
     * @throws Exception mockMvc.perform();
     *
     * 고정 시나리오 :일정 1 2023-08-15 ~ 2023-08-30
     *              일정 2 2023-08-20 ~ 2023-08-25
     *              일정 3 2023-08-10 ~ 2023-08-22
     *
     *              그룹 1 (for 일정 1) repOption 2 repValue 2
     *              그룹 2 (for 일정 2) repOption 3 repValue "월", "수", "금"
     *
     *              @param repOption 할 일이 반복되는 양상을 설정하는 옵션
     *                               1(매일 반복), 2(기간 반복), 3(요일 반복)
     *              @param repValue 할 일이 반복되는 주기를 설정하는 옵션
     *
     *              할일 1 (for 일정 3) 2023-08-13
     *              할일 2 (for 일정 3) 2023-08-15
     *              할일 3 (for 일정 3) 2023-08-20
     */
    @Test
    @DisplayName("검색 시작일과 종료일이 같고 검색 시작일에 일정1 (그룹 할일 한 개 보유), 일정3 (일일 할일 한 개 보유)을 가지고 있는 경우 " +
            "결과 맵에 일정 두개와 할 일이 각각 하나씩 있는지 확인한다.")
    void searchStartEqualsSearchEnd_hasResult() throws Exception {

        // given
        MemberResDto member = memberService.save(new MemberReqDto("test@abc.co.kr", "dr3$@sdf"));

        PlanResDto plan1 = planService.save(new PlanReqDto("plan 1", LocalDate.of(2023, 8, 15), LocalDate.of(2023, 8, 30), member.getId()));
        PlanResDto plan2 = planService.save(new PlanReqDto("plan 2", LocalDate.of(2023, 8, 20), LocalDate.of(2023, 8, 25), member.getId()));
        PlanResDto plan3 = planService.save(new PlanReqDto("plan 3", LocalDate.of(2023, 8, 10), LocalDate.of(2023, 8, 22), member.getId()));

        scenario1_createGroupsAndCheckboxes(plan1.getId(), plan2.getId(), plan3.getId());

        MockHttpServletRequestBuilder request = get("/calendar/range")
                .header("Authorization", "Bearer " + jwtProvider.generateToken(member.getId()))
                .param("memberId", member.getId().toString())
                .param("searchStart", LocalDate.of(2023, 8, 15).toString())
                .param("searchEnd", LocalDate.of(2023, 8, 15).toString());

        // when - then
        mockMvc.perform(request)
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.['2023-08-15']", Matchers.aMapWithSize(2)))
                .andExpect(jsonPath("$.['2023-08-15']", Matchers.hasKey(plan1.toString())))
                .andExpect(jsonPath("$.['2023-08-15']", Matchers.hasKey(plan3.toString())))
                .andExpect(jsonPath("$.['2023-08-15'].['" + plan1 + "'].length()").value(1))
                .andExpect(jsonPath("$.['2023-08-15'].['" + plan3 + "'].length()").value(1))
                .andDo(print());

    }

    @Test
    @DisplayName("검색 시작일과 종료일이 같고, 검색 시작일에 걸쳐 있는 일정이 없는 경우 결과 맵에 일정과 할일이 없는지 확인한다.")
    void searchStartEqualsSearchEnd_emptyPlan() throws Exception {

        // given
        MemberResDto member = memberService.save(new MemberReqDto("test@abc.co.kr", "dr3$@sdf"));

        PlanResDto plan1 = planService.save(new PlanReqDto("plan 1", LocalDate.of(2023, 8, 15), LocalDate.of(2023, 8, 30), member.getId()));
        PlanResDto plan2 = planService.save(new PlanReqDto("plan 2", LocalDate.of(2023, 8, 20), LocalDate.of(2023, 8, 25), member.getId()));
        PlanResDto plan3 = planService.save(new PlanReqDto("plan 3", LocalDate.of(2023, 8, 10), LocalDate.of(2023, 8, 22), member.getId()));

        scenario1_createGroupsAndCheckboxes(plan1.getId(), plan2.getId(), plan3.getId());

        MockHttpServletRequestBuilder request = get("/calendar/range")
                .header("Authorization", "Bearer " + jwtProvider.generateToken(member.getId()))
                .param("memberId", member.getId().toString())
                .param("searchStart", LocalDate.of(2023, 8, 9).toString())
                .param("searchEnd", LocalDate.of(2023, 8, 9).toString());

        // when - then
        mockMvc.perform(request)
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.['2023-08-09']").exists())
                .andExpect(jsonPath("$.['2023-08-09']", Matchers.aMapWithSize(0)))
                .andDo(print());

    }

    @Test
    @DisplayName("검색 시작일과 종료일이 같고, 검색 시작일에 걸쳐 있는 일정이 하나 있고, 일정이 할일을 가지고 있지 않은 경우 " +
            "결과 맵에 일정 하나가 있고 일정의 할일 리스트가 비어 있는지 확인한다.")
    void searchStartEqualsSearchEnd_onePlanEmptyCheckboxes() throws Exception {

        // given
        MemberResDto member = memberService.save(new MemberReqDto("test@abc.co.kr", "dr3$@sdf"));

        PlanResDto plan1 = planService.save(new PlanReqDto("plan 1", LocalDate.of(2023, 8, 15), LocalDate.of(2023, 8, 30), member.getId()));
        PlanResDto plan2 = planService.save(new PlanReqDto("plan 2", LocalDate.of(2023, 8, 20), LocalDate.of(2023, 8, 25), member.getId()));
        PlanResDto plan3 = planService.save(new PlanReqDto("plan 3", LocalDate.of(2023, 8, 10), LocalDate.of(2023, 8, 22), member.getId()));

        scenario1_createGroupsAndCheckboxes(plan1.getId(), plan2.getId(), plan3.getId());

        MockHttpServletRequestBuilder request = get("/calendar/range")
                .header("Authorization", "Bearer " + jwtProvider.generateToken(member.getId()))
                .param("memberId", member.getId().toString())
                .param("searchStart", LocalDate.of(2023, 8, 14).toString())
                .param("searchEnd", LocalDate.of(2023, 8, 14).toString());

        // when - then
        mockMvc.perform(request)
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.['2023-08-14']").exists())
                .andExpect(jsonPath("$.['2023-08-14'].['" + plan3 + "']", Matchers.empty()));

    }

    @Test
    @DisplayName("검색 시작일과 종료일이 다르고, 기간 내에 일정이 하나도 걸쳐 있지 않은 경우 검색 시작일과 종료일 사이의 모든 날짜에 일정과 할일이 없는지 확인한다.")
    void searchStartDiffersSearchEnd_emptyResult() throws Exception {

        // given
        MemberResDto member = memberService.save(new MemberReqDto("test@abc.co.kr", "dr3$@sdf"));

        PlanResDto plan1 = planService.save(new PlanReqDto("plan 1", LocalDate.of(2023, 8, 15), LocalDate.of(2023, 8, 30), member.getId()));
        PlanResDto plan2 = planService.save(new PlanReqDto("plan 2", LocalDate.of(2023, 8, 20), LocalDate.of(2023, 8, 25), member.getId()));
        PlanResDto plan3 = planService.save(new PlanReqDto("plan 3", LocalDate.of(2023, 8, 10), LocalDate.of(2023, 8, 22), member.getId()));

        scenario1_createGroupsAndCheckboxes(plan1.getId(), plan2.getId(), plan3.getId());

        MockHttpServletRequestBuilder request = get("/calendar/range")
                .header("Authorization", "Bearer " + jwtProvider.generateToken(member.getId()))
                .param("memberId", member.getId().toString())
                .param("searchStart", LocalDate.of(2023, 8, 5).toString())
                .param("searchEnd", LocalDate.of(2023, 8, 9).toString());

        mockMvc.perform(request)
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.['2023-08-05']").isEmpty())
                .andExpect(jsonPath("$.['2023-08-06']").isEmpty())
                .andExpect(jsonPath("$.['2023-08-07']").isEmpty())
                .andExpect(jsonPath("$.['2023-08-08']").isEmpty())
                .andExpect(jsonPath("$.['2023-08-09']").isEmpty());

    }

    @Test
    @DisplayName("검색 시작일과 종료일이 다르고, 기간 내에 걸쳐 있는 일정이 한 개 있고 일정에 할일이 없는 경우 일정이 있고 할일이 없는지 확인한다.")
    void searchStartDiffersSearchEnd_onePlanEmptyCheckboxes() throws Exception {

        // given
        MemberResDto member = memberService.save(new MemberReqDto("test@abc.co.kr", "dr3$@sdf"));

        PlanResDto plan1 = planService.save(new PlanReqDto("plan 1", LocalDate.of(2023, 8, 15), LocalDate.of(2023, 8, 30), member.getId()));
        PlanResDto plan2 = planService.save(new PlanReqDto("plan 2", LocalDate.of(2023, 8, 20), LocalDate.of(2023, 8, 25), member.getId()));
        PlanResDto plan3 = planService.save(new PlanReqDto("plan 3", LocalDate.of(2023, 8, 10), LocalDate.of(2023, 8, 22), member.getId()));

        scenario1_createGroupsAndCheckboxes(plan1.getId(), plan2.getId(), plan3.getId());

        MockHttpServletRequestBuilder request = get("/calendar/range")
                .header("Authorization", "Bearer " + jwtProvider.generateToken(member.getId()))
                .param("memberId", member.getId().toString())
                .param("searchStart", LocalDate.of(2023, 8, 10).toString())
                .param("searchEnd", LocalDate.of(2023, 8, 12).toString());

        // when - then
        mockMvc.perform(request)
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.['2023-08-10'].['" + plan3 + "']").exists())
                .andExpect(jsonPath("$.['2023-08-10'].['" + plan3 + "']", Matchers.empty()))
                .andExpect(jsonPath("$.['2023-08-11'].['" + plan3 + "']").exists())
                .andExpect(jsonPath("$.['2023-08-11'].['" + plan3 + "']", Matchers.empty()))
                .andExpect(jsonPath("$.['2023-08-12'].['" + plan3 + "']").exists())
                .andExpect(jsonPath("$.['2023-08-12'].['" + plan3 + "']", Matchers.empty()));

    }

    @Test
    @DisplayName("검색 시작일과 종료일이 다르고, 기간 내에 걸쳐 있는 일정이 두 개 있고 일정 중 하나에 할일이 있는 경우 일정과 할일이 둘 다 있는지 확인한다.")
    void searchStartDiffersSearchEnd_onePlanWithCheckboxes() throws Exception {

        // given
        MemberResDto member = memberService.save(new MemberReqDto("test@abc.co.kr", "dr3$@sdf"));

        PlanResDto plan1 = planService.save(new PlanReqDto("plan 1", LocalDate.of(2023, 8, 15), LocalDate.of(2023, 8, 30), member.getId()));
        PlanResDto plan2 = planService.save(new PlanReqDto("plan 2", LocalDate.of(2023, 8, 20), LocalDate.of(2023, 8, 25), member.getId()));
        PlanResDto plan3 = planService.save(new PlanReqDto("plan 3", LocalDate.of(2023, 8, 10), LocalDate.of(2023, 8, 22), member.getId()));

        scenario1_createGroupsAndCheckboxes(plan1.getId(), plan2.getId(), plan3.getId());

        MockHttpServletRequestBuilder request = get("/calendar/range")
                .header("Authorization", "Bearer " + jwtProvider.generateToken(member.getId()))
                .param("memberId", member.getId().toString())
                .param("searchStart", LocalDate.of(2023, 8, 16).toString())
                .param("searchEnd", LocalDate.of(2023, 8, 18).toString());

        // when - then
        mockMvc.perform(request)
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.['2023-08-16'].['" + plan1 + "']").exists())
                .andExpect(jsonPath("$.['2023-08-16'].['" + plan1 + "']").isEmpty())
                .andExpect(jsonPath("$.['2023-08-16'].['" + plan3 + "']").exists())
                .andExpect(jsonPath("$.['2023-08-16'].['" + plan3 + "']").isEmpty())
                .andExpect(jsonPath("$.['2023-08-17'].['" + plan1 + "']").exists())
                .andExpect(jsonPath("$.['2023-08-17'].['" + plan1 + "'].length()").value(1))
                .andExpect(jsonPath("$.['2023-08-18'].['" + plan1 + "']").exists())
                .andExpect(jsonPath("$.['2023-08-18'].['" + plan1 + "']").isEmpty())
                .andExpect(jsonPath("$.['2023-08-18'].['" + plan3 + "']").exists())
                .andExpect(jsonPath("$.['2023-08-18'].['" + plan3 + "']").isEmpty());

    }


}
