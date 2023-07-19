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
import yeonleaf.plantodo.domain.Group;
import yeonleaf.plantodo.domain.Member;
import yeonleaf.plantodo.dto.MemberReqDto;
import yeonleaf.plantodo.dto.MemberResDto;
import yeonleaf.plantodo.dto.PlanReqDto;
import yeonleaf.plantodo.provider.JwtBasicProvider;
import yeonleaf.plantodo.repository.GroupRepository;
import yeonleaf.plantodo.service.MemberService;

import java.time.LocalDate;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
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
    private GroupRepository groupRepository;

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

        MemberReqDto memberReqDto = new MemberReqDto("test@abc.co.kr", "a3df!#sac");
        MemberResDto memberResDto = memberService.save(memberReqDto);
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

}
