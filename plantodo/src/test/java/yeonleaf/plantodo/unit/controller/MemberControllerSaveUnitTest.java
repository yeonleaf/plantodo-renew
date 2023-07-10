package yeonleaf.plantodo.unit.controller;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.jsonwebtoken.JwtBuilder;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;
import yeonleaf.plantodo.controller.MemberController;
import yeonleaf.plantodo.domain.Member;
import yeonleaf.plantodo.dto.MemberReqDto;
import yeonleaf.plantodo.exceptions.ApiBindingError;
import yeonleaf.plantodo.service.MemberServiceImpl;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(MemberController.class)
public class MemberControllerSaveUnitTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private MemberServiceImpl memberService;

    @MockBean
    private JwtBuilder jwtBuilder;

    private final ObjectMapper objectMapper = new ObjectMapper();

    MockHttpServletRequestBuilder makeSaveRequest(String email, String password) throws JsonProcessingException {
        MemberReqDto memberReqDto = new MemberReqDto(email, password);
        String requestData = objectMapper.writeValueAsString(memberReqDto);
        when(memberService.save(any())).thenReturn(new Member(memberReqDto));
        return post("/member")
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestData);
    }

    MvcResult badRequestMvcTest(String email, String password) throws Exception {
        MockHttpServletRequestBuilder request = makeSaveRequest(email, password);
        return mockMvc.perform(request)
                .andExpect(status().isBadRequest())
                .andReturn();
    }

    ApiBindingError parseResultToApiError(String target) throws JsonProcessingException {
        return objectMapper.readValue(target, ApiBindingError.class);
    }

    @Test
    @DisplayName("멤버 등록 - 정상 케이스 - Http 상태코드 확인")
    void saveTestNormal() throws Exception {
        MockHttpServletRequestBuilder request = makeSaveRequest("test@abc.co.kr", "sz81@Za3");
        mockMvc.perform(request)
                .andExpect(status().isCreated());
    }

    @Test
    @DisplayName("멤버 등록 - 비정상 케이스 (파라미터 빈 문자열) - Http 상태코드 확인")
    void saveTestAbnormalEmptyString() throws Exception {
        MockHttpServletRequestBuilder request = makeSaveRequest("", "");
        mockMvc.perform(request)
                .andExpect(status().isBadRequest())
                .andDo(print());
    }

    @Test
    @DisplayName("멤버 등록 - 비정상 케이스 (파라미터 빈 문자열) - Http 상태코드 확인")
    void saveTestAbnormalSpace() throws Exception {
        MockHttpServletRequestBuilder request = makeSaveRequest(" ", " ");
        mockMvc.perform(request)
                .andExpect(status().isBadRequest())
                .andDo(print());
    }

    @Test
    @DisplayName("멤버 등록 - 비정상 케이스 (이메일 정상, 비밀번호 형식 오류 1개) - 결과 검증")
    void saveTestInvalidPasswordFormatOne() throws Exception {
        MvcResult mvcResult = badRequestMvcTest("test@abc.co.kr", "41ab#");
        ApiBindingError apiError = parseResultToApiError(mvcResult.getResponse().getContentAsString());
        assertThat(apiError.getErrors().get("password").size()).isEqualTo(1);
        assertThat(apiError.getErrors().containsKey("email")).isFalse();
    }

    @Test
    @DisplayName("멤버 등록 - 비정상 케이스 (이메일 정상, 비밀번호 형식 오류 2개) - 결과 검증")
    void saveTestInvalidPasswordFormatTwo() throws Exception {
        MvcResult mvcResult = badRequestMvcTest("test@abc.co.kr", "41ab");
        ApiBindingError apiError = parseResultToApiError(mvcResult.getResponse().getContentAsString());
        assertThat(apiError.getErrors().get("password").size()).isEqualTo(2);
        assertThat(apiError.getErrors().containsKey("email")).isFalse();
    }

    @Test
    @DisplayName("멤버 등록 - 비정상 케이스 (이메일 형식 오류 1개, 비밀번호 정상) - 결과 검증")
    void saveTestInvalidEmailFormatOne() throws Exception {
        MvcResult mvcResult = badRequestMvcTest("test@ab@c", "41ab$%za");
        ApiBindingError apiError = parseResultToApiError(mvcResult.getResponse().getContentAsString());
        assertThat(apiError.getErrors().get("email").size()).isEqualTo(1);
        assertThat(apiError.getErrors().containsKey("password")).isFalse();
    }

    @Test
    @DisplayName("멤버 등록 - 비정상 케이스 (이메일 형식 오류 2개, 비밀번호 정상) - 결과 검증")
    void saveTestInvalidEmailFormatTwo() throws Exception {
        MvcResult mvcResult = badRequestMvcTest("te..st@ab한c", "41ab$%za");
        ApiBindingError apiError = parseResultToApiError(mvcResult.getResponse().getContentAsString());
        assertThat(apiError.getErrors().get("email").size()).isEqualTo(2);
        assertThat(apiError.getErrors().containsKey("password")).isFalse();
    }

    @Test
    @DisplayName("멤버 등록 - 비정상 케이스 (이메일 형식 오류 1개, 비밀번호 형식 오류 1개) - 결과 검증")
    void saveTestInvalidFormatBothOne() throws Exception {
        MvcResult mvcResult = badRequestMvcTest("test@ab한c", "41a$%za");
        ApiBindingError apiError = parseResultToApiError(mvcResult.getResponse().getContentAsString());
        assertThat(apiError.getErrors().get("email").size()).isEqualTo(1);
        assertThat(apiError.getErrors().get("password").size()).isEqualTo(1);
    }

    @Test
    @DisplayName("멤버 등록 - 비정상 케이스 (이메일 형식 오류 2개, 비밀번호 형식 오류 2개) - 결과 검증")
    void saveTestInvalidFormatBothTwo() throws Exception {
        MvcResult mvcResult = badRequestMvcTest("tes..t@ab한c", "41a$/za");
        ApiBindingError apiError = parseResultToApiError(mvcResult.getResponse().getContentAsString());
        assertThat(apiError.getErrors().get("email").size()).isEqualTo(2);
        assertThat(apiError.getErrors().get("password").size()).isEqualTo(2);
    }
}
