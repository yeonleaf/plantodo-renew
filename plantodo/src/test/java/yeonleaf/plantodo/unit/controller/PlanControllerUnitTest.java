package yeonleaf.plantodo.unit.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.persistence.PersistenceException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.hateoas.MediaTypes;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;
import yeonleaf.plantodo.TestConfig;
import yeonleaf.plantodo.controller.PlanController;
import yeonleaf.plantodo.domain.Member;
import yeonleaf.plantodo.dto.PlanReqDto;
import yeonleaf.plantodo.dto.PlanResDto;
import yeonleaf.plantodo.provider.JwtBasicProvider;
import yeonleaf.plantodo.provider.JwtTestProvider;
import yeonleaf.plantodo.service.MemberService;
import yeonleaf.plantodo.service.PlanService;

import java.time.LocalDate;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@Import({TestConfig.class})
@WebMvcTest(PlanController.class)
public class PlanControllerUnitTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private JwtTestProvider jwtTestProvider;

    @MockBean
    private PlanService planService;

    @MockBean
    private MemberService memberService;

    @Test
    @DisplayName("정상 등록")
    void saveTestNormal() throws Exception {
        LocalDate start = LocalDate.now();
        LocalDate end = start.plusDays(3);

        PlanReqDto planReqDto = new PlanReqDto("title", start, end);
        String requestData = objectMapper.writeValueAsString(planReqDto);

        when(jwtTestProvider.extractToken(any())).thenReturn("token");
        when(jwtTestProvider.getIdFromToken(any())).thenReturn(1L);

        Member member = new Member("test@abc.co.kr", "d2A%2d56A");
        member.setId(1L);
        when(memberService.findById(any())).thenReturn(Optional.of(member));

        when(planService.save(any(), any())).thenReturn(new PlanResDto(1L, "title", start, end));

        MockHttpServletRequestBuilder request = post("/plan")
                .header("Authorization", "")
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestData);

        mockMvc.perform(request)
                .andExpect(status().isCreated())
                .andExpect(content().contentType(MediaTypes.HAL_JSON))
                .andExpect(jsonPath("_links").exists())
                .andDo(print());
    }

    @Test
    @DisplayName("비정상 등록 - 파라미터 null")
    void saveTestAbnormalNullParameters() throws Exception {
        PlanReqDto planReqDto = new PlanReqDto(null, null, null);
        String requestData = objectMapper.writeValueAsString(planReqDto);

        MockHttpServletRequestBuilder request = post("/plan")
                .header("Authorization", "")
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestData);

        mockMvc.perform(request)
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("message").value("입력값 타입/내용 오류"));
    }

    @Test
    @DisplayName("비정상 등록 - format validation - start가 오늘 이전")
    void saveTestAbnormalFormatValidationStart() throws Exception {
        LocalDate start = LocalDate.now().minusDays(3);
        LocalDate end = start.plusDays(4);

        PlanReqDto planReqDto = new PlanReqDto("title", start, end);
        String requestData = objectMapper.writeValueAsString(planReqDto);

        MockHttpServletRequestBuilder request = post("/plan")
                .header("Authorization", "")
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestData);

        mockMvc.perform(request)
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("message").value("입력값 타입/내용 오류"))
                .andExpect(jsonPath("errors.start[0]").value("must be a date in the present or in the future"))
                .andDo(print());
    }


    @Test
    @DisplayName("비정상 등록 - format validation - end가 start 이전")
    void saveTestAbnormalFormatValidationEnd() throws Exception {
        LocalDate start = LocalDate.now().plusDays(3);
        LocalDate end = start.minusDays(2);

        PlanReqDto planReqDto = new PlanReqDto("title", start, end);
        String requestData = objectMapper.writeValueAsString(planReqDto);

        MockHttpServletRequestBuilder request = post("/plan")
                .header("Authorization", "")
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestData);

        mockMvc.perform(request)
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("message").value("입력값 형식 오류"))
                .andExpect(jsonPath("errors.end[0]").value("end는 start 이전일 수 없습니다."))
                .andDo(print());
    }

    @Test
    @DisplayName("비정상 등록 - resource not found (member)")
    void saveTestAbnormalMemberResourceNotFound() throws Exception {
        LocalDate start = LocalDate.now();
        LocalDate end = start.plusDays(3);

        PlanReqDto planReqDto = new PlanReqDto("title", start, end);
        String requestData = objectMapper.writeValueAsString(planReqDto);

        when(jwtTestProvider.extractToken(any())).thenReturn("token");
        when(jwtTestProvider.getIdFromToken(any())).thenReturn(1L);
        when(memberService.findById(any())).thenReturn(Optional.empty());

        MockHttpServletRequestBuilder request = post("/plan")
                .header("Authorization", "")
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestData);

        mockMvc.perform(request)
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("message").value("Resource not found"));
    }

    @Test
    @DisplayName("비정상 등록 - persistence problem")
    void saveAbnormalPersistenceProblem() throws Exception {
        LocalDate start = LocalDate.now();
        LocalDate end = start.plusDays(3);

        PlanReqDto planReqDto = new PlanReqDto("title", start, end);
        String requestData = objectMapper.writeValueAsString(planReqDto);

        when(jwtTestProvider.extractToken(any())).thenReturn("token");
        when(jwtTestProvider.getIdFromToken(any())).thenReturn(1L);

        Member member = new Member("test@abc.co.kr", "d2A%2d56A");
        member.setId(1L);
        when(memberService.findById(any())).thenReturn(Optional.of(member));

        when(planService.save(any(), any())).thenThrow(PersistenceException.class);

        MockHttpServletRequestBuilder request = post("/plan")
                .header("Authorization", "")
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestData);

        mockMvc.perform(request)
                .andExpect(status().is5xxServerError())
                .andExpect(jsonPath("message").value("Possible server error"))
                .andDo(print());
    }


    @Test
    @DisplayName("plan 한 개 정상 조회")
    void oneTestNormal() throws Exception {
        MockHttpServletRequestBuilder request = get("/plan/1")
                .header("Authorization", "");
        mockMvc.perform(request)
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaTypes.HAL_JSON));
    }

}
