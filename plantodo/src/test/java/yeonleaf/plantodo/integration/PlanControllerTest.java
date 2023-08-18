package yeonleaf.plantodo.integration;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.jayway.jsonpath.JsonPath;
import jakarta.transaction.Transactional;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;
import org.springframework.validation.BindingResult;
import yeonleaf.plantodo.domain.Checkbox;
import yeonleaf.plantodo.domain.Group;
import yeonleaf.plantodo.dto.*;
import yeonleaf.plantodo.provider.JwtBasicProvider;
import yeonleaf.plantodo.repository.CheckboxRepository;
import yeonleaf.plantodo.repository.GroupRepository;
import yeonleaf.plantodo.service.CheckboxService;
import yeonleaf.plantodo.service.GroupService;
import yeonleaf.plantodo.service.MemberService;
import yeonleaf.plantodo.service.PlanService;
import yeonleaf.plantodo.controller.PlanController;

import java.time.LocalDate;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * target : {@link PlanController}에 있는 모든 메소드
 * target description : 일정을 생성, 조회, 수정, 상태 변경, 삭제하는 API
 *
 * test description : 인터셉터를 포함한 통합 테스트
 *                    모든 요청의 Authorization 헤더에 Jwt 토큰을 포함시켜야 한다.
 */
@Transactional
@SpringBootTest
@AutoConfigureMockMvc
public class PlanControllerTest {

    @Autowired
    private JwtBasicProvider jwtProvider;

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private MemberService memberService;

    @Autowired
    private PlanService planService;

    @Autowired
    private GroupService groupService;

    @Autowired
    private CheckboxService checkboxService;

    @Autowired
    private GroupRepository groupRepository;

    @Autowired
    private CheckboxRepository checkboxRepository;


    /**
     * 일정 등록 API 관련 테스트
     * @see PlanController#save(PlanReqDto, BindingResult)
     * @throws Exception mockMvc.perform();
     */
    @Test
    @DisplayName("정상 등록 - plan 생성 - 일정 등록 후 생성된 일정을 기준으로 그룹을 조회하여 repOption이 0인 그룹이 같이 생성되었는지 확인한다. " +
            "repOption이 0인 그룹은 일일 할일을 관리하기 위해서 plan 등록과 함께 생성하는 그룹으로, 클라이언트는 접근할 수 없다.")
    void saveTestNormal() throws Exception {

        // given
        MemberReqDto memberReqDto = new MemberReqDto("test@abc.co.kr", "a3df!#sac");
        MemberResDto memberResDto = memberService.save(memberReqDto);
        Long memberId = memberResDto.getId();

        LocalDate start = LocalDate.now();
        LocalDate end = start.plusDays(3);
        PlanReqDto planReqDto = new PlanReqDto("title", start, end, memberId);
        String requestData = objectMapper.writeValueAsString(planReqDto);

        MockHttpServletRequestBuilder request = post("/plan")
                .header("Authorization", "Bearer " + jwtProvider.generateToken(memberId))
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestData);

        // when - then
        MvcResult mvcResult = mockMvc.perform(request)
                .andExpect(status().isCreated())
                .andExpect(jsonPath("id").exists())
                .andReturn();
        Long planId = Long.parseLong(JsonPath.read(mvcResult.getResponse().getContentAsString(), "$.id").toString());

        List<Group> groups = groupRepository.findByPlanId(planId);
        assertThat(groups.size()).isEqualTo(1);
        assertThat(groups.get(0).getRepetition().getRepOption()).isEqualTo(0);

    }

    @Test
    @DisplayName("비정상 등록 - 시작일이 종료일보다 늦으면 ApiBindingError를 리턴한다. errors 객체가 종료일의 필드 이름을 key로 가지고 있는지 화인한다.")
    void saveTestAbnormal() throws Exception {

        // given
        MemberResDto memberResDto = memberService.save(new MemberReqDto("test@abc.co.kr", "a3df!#sac"));
        Long memberId = memberResDto.getId();

        LocalDate start = LocalDate.now().plusDays(3);
        LocalDate end = start.minusDays(2);
        PlanReqDto planReqDto = new PlanReqDto("title", start, end, memberId);
        String requestData = objectMapper.writeValueAsString(planReqDto);

        MockHttpServletRequestBuilder request = post("/plan")
                .header("Authorization", "Bearer " + jwtProvider.generateToken(memberId))
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestData);

        // when - then
        mockMvc.perform(request)
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("message").value("입력값 형식 오류"))
                .andExpect(jsonPath("errors.end[0]").value("end는 start 이전일 수 없습니다."));

    }


    /**
     * 일정 단건 조회 API 관련 테스트
     * @see PlanController#one(Long)
     * @throws Exception mockMvc.perform();
     */
    @Test
    @DisplayName("단건 정상 조회 - 등록한 일정과 조회한 일정의 내용이 같은지 확인한다.")
    void oneTestNormal() throws Exception {

        // given
        MemberResDto memberResDto = memberService.save(new MemberReqDto("test@abc.co.kr", "a3df!#sac"));
        Long memberId = memberResDto.getId();
        PlanResDto planResDto = planService.save(new PlanReqDto("title", LocalDate.of(2023, 7, 19), LocalDate.of(2023, 8, 10), memberId));

        MockHttpServletRequestBuilder request = get("/plan/" + planResDto.getId())
                .header("Authorization", "Bearer " + jwtProvider.generateToken(memberResDto.getId()));

        // when - then
        mockMvc.perform(request)
                .andExpect(status().isOk())
                .andExpect(jsonPath("id").value(planResDto.getId()))
                .andExpect(jsonPath("title").value(planResDto.getTitle()));

    }

    @Test
    @DisplayName("단건 정상 조회 - 임의로 기간이 지난 일정을 등록한 후 단건 정상 조회를 하면 status가 PAST로 바뀌는지 확인한다.")
    void oneTestNormal_planBecomesPast() throws Exception {

        // given
        MemberResDto memberResDto = memberService.save(new MemberReqDto("test@abc.co.kr", "a3df!#sac"));
        Long memberId = memberResDto.getId();
        PlanResDto planResDto = planService.save(new PlanReqDto("title", LocalDate.of(2023, 7, 19), LocalDate.of(2023, 7, 24), memberId));

        MockHttpServletRequestBuilder request = get("/plan/" + planResDto.getId())
                .header("Authorization", "Bearer " + jwtProvider.generateToken(memberResDto.getId()));

        // when - then
        mockMvc.perform(request)
                .andExpect(status().isOk())
                .andExpect(jsonPath("id").value(planResDto.getId()))
                .andExpect(jsonPath("title").value(planResDto.getTitle()))
                .andExpect(jsonPath("status").value("PAST"));

    }

    @Test
    @DisplayName("단건 비정상 조회 - 조회할 일정이 없는 경우 ApiSimpleError를 리턴한다. message 필드의 내용을 확인한다.")
    void oneTestAbnormal() throws Exception {

        // given
        MockHttpServletRequestBuilder request = get("/plan/" + Long.MAX_VALUE)
                .header("Authorization", "Bearer " + jwtProvider.generateToken(Long.MAX_VALUE));

        // when - then
        mockMvc.perform(request)
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("message").value("Resource not found"));

    }


    /**
     * 일정 수정 API 관련 테스트
     * 시작일이 2023-07-18, 종료일이 2023-07-25인 일정을 수정하고, 변경된 범위에 따라 기존 일정 삭제 / 새 일정 생성이 정상적으로 이루어지는지 확인한다.
     *
     * @see PlanController#update(PlanUpdateReqDto, BindingResult)
     * @throws Exception mockMvc.perform();
     */
    @Test
    @DisplayName("정상 수정 - repOption = 1 - 수정한 일정을 기준으로 할일을 조회했을 때 예상한 날짜만 결과 리스트에 들어 있는지 확인한다.")
    void updateTestNormal_repOption1() throws Exception {

        // given
        MemberResDto memberResDto = memberService.save(new MemberReqDto("test@abc.co.kr", "a3df!#sac"));
        Long memberId = memberResDto.getId();
        PlanResDto planResDto = planService.save(new PlanReqDto("title", LocalDate.of(2023, 7, 18), LocalDate.of(2023, 7, 25), memberId));
        GroupResDto group = groupService.save(new GroupReqDto("group", 1, List.of(), planResDto.getId()));

        PlanUpdateReqDto planUpdateReqDto = new PlanUpdateReqDto(planResDto.getId(), "revisedTitle", LocalDate.of(2023, 7, 23), LocalDate.of(2023, 7, 29));
        MockHttpServletRequestBuilder request = put("/plan")
                .header("Authorization", "Bearer " + jwtProvider.generateToken(memberId))
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(planUpdateReqDto));

        // when - then
        mockMvc.perform(request)
                .andExpect(status().isOk());

        List<LocalDate> dateResult = checkboxRepository.findByGroupId(group.getId()).stream().map(Checkbox::getDate).toList();
        assertThat(dateResult).containsOnly(
                LocalDate.of(2023, 7, 23),
                LocalDate.of(2023, 7, 24),
                LocalDate.of(2023, 7, 25),
                LocalDate.of(2023, 7, 26),
                LocalDate.of(2023, 7, 27),
                LocalDate.of(2023, 7, 28),
                LocalDate.of(2023, 7, 29)
        );

    }

    @Test
    @DisplayName("정상 수정 - repOption = 2 - 수정한 일정을 기준으로 할일을 조회했을 때 예상한 날짜만 결과 리스트에 들어 있는지 확인한다.")
    void updateTestNormal_repOption2() throws Exception {

        // given
        MemberResDto memberResDto = memberService.save(new MemberReqDto("test@abc.co.kr", "a3df!#sac"));
        Long memberId = memberResDto.getId();
        PlanResDto planResDto = planService.save(new PlanReqDto("title", LocalDate.of(2023, 7, 18), LocalDate.of(2023, 7, 25), memberId));
        GroupResDto group = groupService.save(new GroupReqDto("group", 2, List.of("3"), planResDto.getId()));


        PlanUpdateReqDto planUpdateReqDto = new PlanUpdateReqDto(planResDto.getId(), "revisedTitle", LocalDate.of(2023, 7, 23), LocalDate.of(2023, 7, 29));
        MockHttpServletRequestBuilder request = put("/plan")
                .header("Authorization", "Bearer " + jwtProvider.generateToken(memberId))
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(planUpdateReqDto));

        // when - then
        mockMvc.perform(request)
                .andExpect(status().isOk());

        List<LocalDate> dateResult = checkboxRepository.findByGroupId(group.getId()).stream().map(Checkbox::getDate).toList();
        assertThat(dateResult).containsOnly(
                LocalDate.of(2023, 7, 23),
                LocalDate.of(2023, 7, 26),
                LocalDate.of(2023, 7, 29)
        );

    }

    @Test
    @DisplayName("정상 수정 - repOption = 3 - 수정한 일정을 기준으로 할일을 조회했을 때 예상한 날짜만 결과 리스트에 들어 있는지 확인한다.")
    void updateTestNormal_repOption3() throws Exception {

        // given
        MemberResDto memberResDto = memberService.save(new MemberReqDto("test@abc.co.kr", "a3df!#sac"));
        Long memberId = memberResDto.getId();
        PlanResDto planResDto = planService.save(new PlanReqDto("title", LocalDate.of(2023, 7, 18), LocalDate.of(2023, 7, 25), memberId));
        GroupResDto group = groupService.save(new GroupReqDto("group", 3, List.of("월", "수", "금"), planResDto.getId()));


        PlanUpdateReqDto planUpdateReqDto = new PlanUpdateReqDto(planResDto.getId(), "revisedTitle", LocalDate.of(2023, 7, 23), LocalDate.of(2023, 7, 29));
        MockHttpServletRequestBuilder request = put("/plan")
                .header("Authorization", "Bearer " + jwtProvider.generateToken(memberId))
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(planUpdateReqDto));

        // when - then
        mockMvc.perform(request)
                .andExpect(status().isOk());

        List<LocalDate> dateResult = checkboxRepository.findByGroupId(group.getId()).stream().map(Checkbox::getDate).toList();
        assertThat(dateResult).containsOnly(
                LocalDate.of(2023, 7, 24),
                LocalDate.of(2023, 7, 26),
                LocalDate.of(2023, 7, 28)
        );

    }

    @Test
    @DisplayName("비정상 수정 - 수정할 일정이 없는 경우 ApiSimpleError를 리턴한다. message 필드의 내용을 확인한다.")
    void updateTestAbnormal() throws Exception {

        // given
        PlanUpdateReqDto planUpdateReqDto = new PlanUpdateReqDto(Long.MAX_VALUE, "revisedTitle", LocalDate.of(2023, 7, 23), LocalDate.of(2023, 7, 29));
        MockHttpServletRequestBuilder request = put("/plan")
                .header("Authorization", "Bearer " + jwtProvider.generateToken(Long.MAX_VALUE))
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(planUpdateReqDto));

        // when - then
        mockMvc.perform(request)
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("message").value("Resource not found"));

    }


    /**
     * 일정 삭제 API 관련 테스트
     * 일정 삭제 시 연관된 모든 그룹, 모든 그룹 할일, 모든 일일 할일이 삭제되었는지 확인한다.
     * @see PlanController#delete(Long)
     * @throws Exception mockMvc.perform();
     */
    @Test
    @DisplayName("정상 삭제 - 일정 삭제 시 연관된 모든 그룹, 모든 그룹 할일, 모든 일일 할일이 삭제되었는지 확인한다")
    void deleteTestNormal() throws Exception {

        // given
        MemberResDto memberResDto = memberService.save(new MemberReqDto("test@abc.co.kr", "a3df!#sac"));
        Long memberId = memberResDto.getId();
        PlanResDto planResDto = planService.save(new PlanReqDto("title", LocalDate.of(2023, 7, 18), LocalDate.of(2023, 7, 25), memberId));
        Long planId = planResDto.getId();
        GroupResDto groupResDto = groupService.save(new GroupReqDto("group", 3, List.of("월", "수", "금"), planResDto.getId()));
        CheckboxResDto checkboxResDto = checkboxService.save(new CheckboxReqDto("title", planId, LocalDate.of(2023, 7, 18)));
        Long groupId = groupResDto.getId();
        Long checkboxId = checkboxResDto.getId();

        MockHttpServletRequestBuilder request = delete("/plan/" + planId)
                .header("Authorization", "Bearer " + jwtProvider.generateToken(memberId));

        // when - then
        mockMvc.perform(request)
                .andExpect(status().isNoContent());

        List<Group> findGroups = groupRepository.findByPlanId(planId);
        List<Checkbox> findCheckboxes = checkboxRepository.findByGroupId(groupId);
        Optional<Checkbox> findCheckbox = checkboxRepository.findById(checkboxId);

        assertThat(findGroups).isEmpty();
        assertThat(findCheckboxes).isEmpty();
        assertThat(findCheckbox).isEmpty();

    }

    @Test
    @DisplayName("비정상 삭제 - 삭제할 일정이 없는 경우 ApiSimpleError를 리턴한다. message 필드의 내용을 확인한다.")
    void deleteTestAbnormal() throws Exception {

        // given
        MockHttpServletRequestBuilder request = delete("/plan/" + Long.MAX_VALUE)
                .header("Authorization", "Bearer " + jwtProvider.generateToken(Long.MAX_VALUE));

        // when - then
        mockMvc.perform(request)
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("message").value("Resource not found"));

    }


    /**
     * 일정 상태 변경 API 관련 테스트
     * NOW ↔ COMPLETED
     * @see PlanController#change(Long)
     * @throws Exception mockMvc.perform();
     */
    @Test
    @DisplayName("정상 상태 변경 - 상태 변경 후 일정을 다시 조회했을 때 상태가 COMPLETED인지 확인한다.")
    void changeStatusTestNormal() throws Exception {

        // given
        MemberResDto memberResDto = memberService.save(new MemberReqDto("test@abc.co.kr", "a3df!#sac"));
        PlanResDto planResDto = planService.save(new PlanReqDto("title", LocalDate.now(), LocalDate.now().plusDays(3), memberResDto.getId()));
        Long memberId = memberResDto.getId();
        Long planId = planResDto.getId();

        MockHttpServletRequestBuilder request = patch("/plan/" + planId)
                .header("Authorization", "Bearer " + jwtProvider.generateToken(memberId));

        // when - then
        mockMvc.perform(request)
                .andExpect(status().isOk())
                .andExpect(jsonPath("status").value("COMPLETED"));

    }

    @Test
    @DisplayName("비정상 상태 변경 - 상태를 변경할 일정이 없는 경우 ApiSimpleError를 리턴한다. message 필드의 내용을 확인한다.")
    void changeStatusTestAbnormal() throws Exception {

        // given
        MockHttpServletRequestBuilder request = patch("/plan/" + Long.MAX_VALUE)
                .header("Authorization", "Bearer " + jwtProvider.generateToken(Long.MAX_VALUE));

        // when - then
        mockMvc.perform(request)
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("message").value("Resource not found"));

    }


    /**
     * 순수 일정 컬렉션 조회 API 관련 테스트
     * 순수 일정 컬렉션 조회 API는 회원을 기준으로 모든 일정을 조회한다.
     * @see PlanController#all(Long)
     * @throws Exception mockMvc.perform();
     */
    @Test
    @DisplayName("정상 순수 컬렉션 조회 - 세 개의 일정을 등록한 후 순수 일정 컬렉션을 조회하면 세 개의 객체를 리턴해야 한다.")
    void allTestNormal() throws Exception {

        // given
        MemberResDto memberResDto = memberService.save(new MemberReqDto("test@abc.co.kr", "a3df!#sac"));
        Long memberId = memberResDto.getId();
        planService.save(new PlanReqDto("title", LocalDate.now(), LocalDate.now().plusDays(3), memberResDto.getId()));
        planService.save(new PlanReqDto("title", LocalDate.now(), LocalDate.now().plusDays(3), memberResDto.getId()));
        planService.save(new PlanReqDto("title", LocalDate.now(), LocalDate.now().plusDays(3), memberResDto.getId()));

        MockHttpServletRequestBuilder request = get("/plans")
                .header("Authorization", "Bearer " + jwtProvider.generateToken(memberId))
                .param("memberId", memberResDto.getId().toString());

        // when - then
        mockMvc.perform(request)
                .andExpect(status().isOk())
                .andExpect(jsonPath("_embedded.planResDtoList.length()").value(3));

    }

    @Test
    @DisplayName("비정상 순수 컬렉션 조회 - 기준으로 삼을 회원이 없는 경우 ApiSimpleError를 리턴한다. message 필드의 내용을 확인한다.")
    void allTestAbnormal() throws Exception {

        // given
        MockHttpServletRequestBuilder request = get("/plans")
                .header("Authorization", "Bearer " + jwtProvider.generateToken(1L))
                .param("memberId", String.valueOf(Long.MAX_VALUE));

        // when - then
        mockMvc.perform(request)
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("message").value("Resource not found"));

    }


    /**
     * 일별 일정 컬렉션 조회 API 관련 테스트
     * 일별 일정 컬렉션 조회 API는 회원을 기준으로 범위가 검색일에 걸쳐지는 일정을 모두 조회한다.
     * @see PlanController#all(Long, LocalDate)
     * @throws Exception mockMvc.perform();
     */
    @Test
    @DisplayName("정상 일별 컬렉션 조회 - 등록한 모든 일정의 범위가 검색일에 걸쳐지는 경우 조회 결과의 길이가 등록한 일정의 개수와 같은지 확인한다.")
    void collectionFilteredByDateTestNormal() throws Exception {

        // given
        MemberResDto memberResDto = memberService.save(new MemberReqDto("test@abc.co.kr", "a3df!#sac"));
        Long memberId = memberResDto.getId();
        planService.save(new PlanReqDto("title", LocalDate.now(), LocalDate.now().plusDays(3), memberResDto.getId()));
        planService.save(new PlanReqDto("title", LocalDate.now(), LocalDate.now().plusDays(3), memberResDto.getId()));
        planService.save(new PlanReqDto("title", LocalDate.now(), LocalDate.now().plusDays(3), memberResDto.getId()));

        MockHttpServletRequestBuilder request = get("/plans/date")
                .header("Authorization", "Bearer " + jwtProvider.generateToken(memberId))
                .param("memberId", String.valueOf(memberResDto.getId()))
                .param("dateKey", LocalDate.now().toString());

        // when - then
        mockMvc.perform(request)
                .andExpect(status().isOk())
                .andExpect(jsonPath("_embedded.planResDtoList.length()").value(3));

    }

    @Test
    @DisplayName("비정상 일별 컬렉션 조회 - 기준으로 삼을 회원이 없는 경우 ApiSimpleError를 리턴한다. message 필드의 내용을 확인한다.")
    void collectionFilteredByDateTestAbnormal() throws Exception {

        // given
        MockHttpServletRequestBuilder request = get("/plans/date")
                .header("Authorization", "Bearer " + jwtProvider.generateToken(1L))
                .param("memberId", String.valueOf(Long.MAX_VALUE))
                .param("dateKey", LocalDate.now().toString());

        // when - then
        mockMvc.perform(request)
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("message").value("Resource not found"));

    }


    /**
     * 기간 일정 컬렉션 조회 API 관련 테스트
     * 기간 일정 컬렉션 조회 API는 회원을 기준으로 검색 범위가 일정 범위에 걸쳐지는 일정을 모두 조회한다.
     * @see PlanController#all(Long, LocalDate, LocalDate)
     * @throws Exception mockMvc.perform();
     */
    @Test
    @DisplayName("정상 기간 컬렉션 조회 - 조회한 결과의 길이가 등록한 일정 중 검색 범위에 걸쳐지는 일정의 개수와 같은지 확인한다.")
    void collectionFilteredByDateRangeTestNormal() throws Exception {

        // given
        MemberResDto memberResDto = memberService.save(new MemberReqDto("test@abc.co.kr", "a3df!#sac"));
        Long memberId = memberResDto.getId();
        planService.save(new PlanReqDto("title", LocalDate.of(2023, 7, 23), LocalDate.of(2023, 8, 3), memberResDto.getId()));
        planService.save(new PlanReqDto("title", LocalDate.of(2023, 7, 18), LocalDate.of(2023, 7, 25), memberResDto.getId()));
        planService.save(new PlanReqDto("title", LocalDate.of(2023, 8, 3), LocalDate.of(2023, 8, 5), memberResDto.getId()));

        MockHttpServletRequestBuilder request = get("/plans/range")
                .header("Authorization", "Bearer " + jwtProvider.generateToken(memberId))
                .param("memberId", String.valueOf(memberResDto.getId()))
                .param("searchStart", LocalDate.of(2023, 7, 29).toString())
                .param("searchEnd", LocalDate.of(2023, 8, 3).toString());

        // when - then
        mockMvc.perform(request)
                .andExpect(status().isOk())
                .andExpect(jsonPath("_embedded.planResDtoList.length()").value(2));

    }

    @Test
    @DisplayName("비정상 기간 컬렉션 조회 - 기준으로 삼을 회원이 없는 경우 ApiSimpleError를 리턴한다. message 필드의 내용을 확인한다.")
    void collectionFilteredByDateRangeTestAbnormal() throws Exception {

        // given
        MockHttpServletRequestBuilder request = get("/plans/range")
                .header("Authorization", "Bearer " + jwtProvider.generateToken(1L))
                .param("memberId", "1")
                .param("searchStart", LocalDate.of(2023, 7, 29).toString())
                .param("searchEnd", LocalDate.of(2023, 7, 3).toString());

        // when - then
        mockMvc.perform(request)
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("errors.searchStart").exists())
                .andExpect(jsonPath("errors.searchEnd").exists());

    }

}
