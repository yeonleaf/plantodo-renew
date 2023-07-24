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

import java.time.LocalDate;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

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

    @Test
    @DisplayName("정상 등록 - plan 생성 - group 생성 확인(repOption 0)")
    void saveTestNormal() throws Exception {

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
    @DisplayName("비정상 등록 (예시 : end가 start보다 이전일 수 없음)")
    void saveTestAbnormal() throws Exception {

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

        mockMvc.perform(request)
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("message").value("입력값 형식 오류"))
                .andExpect(jsonPath("errors.end[0]").value("end는 start 이전일 수 없습니다."));

    }

    @Test
    @DisplayName("단건 정상 조회")
    void oneTestNormal() throws Exception {

        MemberResDto memberResDto = memberService.save(new MemberReqDto("test@abc.co.kr", "a3df!#sac"));
        Long memberId = memberResDto.getId();
        PlanResDto planResDto = planService.save(new PlanReqDto("title", LocalDate.of(2023, 7, 19), LocalDate.of(2023, 8, 10), memberId));

        MockHttpServletRequestBuilder request = get("/plan/" + planResDto.getId());

        mockMvc.perform(request)
                .andExpect(status().isOk())
                .andExpect(jsonPath("id").value(planResDto.getId()))
                .andExpect(jsonPath("title").value(planResDto.getTitle()));

    }

    @Test
    @DisplayName("단건 비정상 조회")
    void oneTestAbnormal() throws Exception {

        MockHttpServletRequestBuilder request = get("/plan/" + Long.MAX_VALUE);

        mockMvc.perform(request)
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("message").value("Resource not found"));

    }

    List<String> makeArrToList(String... target) {
        return Arrays.asList(target);
    }

    @Test
    @DisplayName("정상 수정 - repOption = 1")
    void updateTestNormal_repOption1() throws Exception {

        MemberResDto memberResDto = memberService.save(new MemberReqDto("test@abc.co.kr", "a3df!#sac"));
        Long memberId = memberResDto.getId();
        PlanResDto planResDto = planService.save(new PlanReqDto("title", LocalDate.of(2023, 7, 18), LocalDate.of(2023, 7, 25), memberId));
        GroupResDto group = groupService.save(new GroupReqDto("group", 1, makeArrToList(), planResDto.getId()));


        PlanUpdateReqDto planUpdateReqDto = new PlanUpdateReqDto(planResDto.getId(), "revisedTitle", LocalDate.of(2023, 7, 23), LocalDate.of(2023, 7, 29));
        MockHttpServletRequestBuilder request = put("/plan")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(planUpdateReqDto));

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
    @DisplayName("정상 수정 - repOption = 2")
    void updateTestNormal_repOption2() throws Exception {

        MemberResDto memberResDto = memberService.save(new MemberReqDto("test@abc.co.kr", "a3df!#sac"));
        Long memberId = memberResDto.getId();
        PlanResDto planResDto = planService.save(new PlanReqDto("title", LocalDate.of(2023, 7, 18), LocalDate.of(2023, 7, 25), memberId));
        GroupResDto group = groupService.save(new GroupReqDto("group", 2, makeArrToList("3"), planResDto.getId()));


        PlanUpdateReqDto planUpdateReqDto = new PlanUpdateReqDto(planResDto.getId(), "revisedTitle", LocalDate.of(2023, 7, 23), LocalDate.of(2023, 7, 29));
        MockHttpServletRequestBuilder request = put("/plan")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(planUpdateReqDto));

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
    @DisplayName("정상 수정 - repOption = 3")
    void updateTestNormal_repOption3() throws Exception {

        MemberResDto memberResDto = memberService.save(new MemberReqDto("test@abc.co.kr", "a3df!#sac"));
        Long memberId = memberResDto.getId();
        PlanResDto planResDto = planService.save(new PlanReqDto("title", LocalDate.of(2023, 7, 18), LocalDate.of(2023, 7, 25), memberId));
        GroupResDto group = groupService.save(new GroupReqDto("group", 3, makeArrToList("월", "수", "금"), planResDto.getId()));


        PlanUpdateReqDto planUpdateReqDto = new PlanUpdateReqDto(planResDto.getId(), "revisedTitle", LocalDate.of(2023, 7, 23), LocalDate.of(2023, 7, 29));
        MockHttpServletRequestBuilder request = put("/plan")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(planUpdateReqDto));

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
    @DisplayName("비정상 수정")
    void updateTestAbnormal() throws Exception {

        PlanUpdateReqDto planUpdateReqDto = new PlanUpdateReqDto(Long.MAX_VALUE, "revisedTitle", LocalDate.of(2023, 7, 23), LocalDate.of(2023, 7, 29));
        MockHttpServletRequestBuilder request = put("/plan")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(planUpdateReqDto));

        mockMvc.perform(request)
                .andExpect(status().isNotFound());

    }

    @Test
    @DisplayName("정상 삭제")
    void deleteTestNormal() throws Exception {

        MemberResDto memberResDto = memberService.save(new MemberReqDto("test@abc.co.kr", "a3df!#sac"));
        Long memberId = memberResDto.getId();
        PlanResDto planResDto = planService.save(new PlanReqDto("title", LocalDate.of(2023, 7, 18), LocalDate.of(2023, 7, 25), memberId));
        Long planId = planResDto.getId();
        GroupResDto groupResDto = groupService.save(new GroupReqDto("group", 3, makeArrToList("월", "수", "금"), planResDto.getId()));
        CheckboxResDto checkboxResDto = checkboxService.save(new CheckboxReqDto("title", planId, LocalDate.of(2023, 7, 18)));
        Long groupId = groupResDto.getId();
        Long checkboxId = checkboxResDto.getId();

        MockHttpServletRequestBuilder request = delete("/plan/" + planId);

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
    @DisplayName("비정상 삭제 - Resource not found")
    void deleteTestAbnormal() throws Exception {

        MockHttpServletRequestBuilder request = delete("/group/" + Long.MAX_VALUE);
        mockMvc.perform(request)
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("message").value("Resource not found"));

    }

}
