package yeonleaf.plantodo.integration;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.transaction.Transactional;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;
import yeonleaf.plantodo.domain.Member;
import yeonleaf.plantodo.dto.MemberReqDto;
import yeonleaf.plantodo.dto.PlanReqDto;
import yeonleaf.plantodo.provider.JwtBasicProvider;
import yeonleaf.plantodo.service.MemberService;

import java.time.LocalDate;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
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

    @Test
    @DisplayName("정상 등록")
    void saveTestNormal() throws Exception {
        MemberReqDto memberReqDto = new MemberReqDto("test@abc.co.kr", "a3df!#sac");
        Member member = memberService.save(memberReqDto);

        LocalDate start = LocalDate.now();
        LocalDate end = start.plusDays(3);
        PlanReqDto planReqDto = new PlanReqDto("title", start, end);
        String requestData = objectMapper.writeValueAsString(planReqDto);

        MockHttpServletRequestBuilder request = post("/plan")
                .header("Authorization", "Bearer " + jwtProvider.generateToken(member.getId()))
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestData);

        mockMvc.perform(request)
                .andExpect(status().isCreated())
                .andDo(print());
    }

    @Test
    @DisplayName("비정상 등록 (예시 : end가 start보다 이전일 수 없음)")
    void saveTestAbnormal() throws Exception {
        MemberReqDto memberReqDto = new MemberReqDto("test@abc.co.kr", "a3df!#sac");
        Member member = memberService.save(memberReqDto);

        LocalDate start = LocalDate.now().plusDays(3);
        LocalDate end = start.minusDays(2);
        PlanReqDto planReqDto = new PlanReqDto("title", start, end);
        String requestData = objectMapper.writeValueAsString(planReqDto);

        MockHttpServletRequestBuilder request = post("/plan")
                .header("Authorization", "Bearer " + jwtProvider.generateToken(member.getId()))
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestData);

        mockMvc.perform(request)
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("message").value("입력값 형식 오류"))
                .andExpect(jsonPath("errors.end[0]").value("end는 start 이전일 수 없습니다."));
    }

}
