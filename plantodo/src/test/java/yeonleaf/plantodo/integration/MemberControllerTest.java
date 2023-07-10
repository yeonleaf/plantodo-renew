package yeonleaf.plantodo.integration;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.transaction.Transactional;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;
import yeonleaf.plantodo.dto.JwtTokenDto;
import yeonleaf.plantodo.dto.MemberReqDto;
import yeonleaf.plantodo.dto.MemberResDto;
import yeonleaf.plantodo.exceptions.ApiBindingError;
import yeonleaf.plantodo.exceptions.ApiSimpleError;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@Transactional
@AutoConfigureMockMvc
public class MemberControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    private MockHttpServletRequestBuilder makeRequest(String email, String password, String url) throws JsonProcessingException {
        MemberReqDto memberReqDto = new MemberReqDto(email, password);
        String requestData = objectMapper.writeValueAsString(memberReqDto);
        return post(url)
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestData);
    }

    @Test
    @DisplayName("회원가입 정상")
    void joinTestNormal() throws Exception {
        MockHttpServletRequestBuilder request = makeRequest("test@abc.co.kr", "3zDF!43A", "/member");

        MvcResult mvcResult = mockMvc.perform(request)
                .andExpect(status().isCreated())
                .andReturn();

        String resultString = mvcResult.getResponse().getContentAsString();
        MemberResDto memberResDto = objectMapper.readValue(resultString, MemberResDto.class);
        assertThat(memberResDto.getId()).isNotNull();
        assertThat(memberResDto.getEmail()).isEqualTo("test@abc.co.kr");
        assertThat(memberResDto.getPassword()).isEqualTo("3zDF!43A");
    }

    @Test
    @DisplayName("회원가입 비정상 - invalid argument")
    void joinTestAbnormalArguments() throws Exception {
        MockHttpServletRequestBuilder request = makeRequest("test@abc.co.kr", "1z4F#", "/member");

        MvcResult mvcResult = mockMvc.perform(request)
                .andExpect(status().isBadRequest())
                .andReturn();

        String resultString = mvcResult.getResponse().getContentAsString();
        ApiBindingError apiBindingError = objectMapper.readValue(resultString, ApiBindingError.class);
        assertThat(apiBindingError.getErrors().get("password").size()).isEqualTo(1);
    }

    @Test
    @DisplayName("회원가입 비정상 - duplicated member")
    void joinTestAbnormalDuplicatedMember() throws Exception {
        MockHttpServletRequestBuilder request = makeRequest("test@abc.co.kr", "3zDF!43A", "/member");
        mockMvc.perform(request);

        MvcResult mvcResult = mockMvc.perform(request)
                .andExpect(status().isBadRequest())
                .andReturn();

        String resultString = mvcResult.getResponse().getContentAsString();
        ApiSimpleError apiSimpleError = objectMapper.readValue(resultString, ApiSimpleError.class);
        assertThat(apiSimpleError.getMessage()).isEqualTo("Duplicated Member");
    }

    @Test
    @DisplayName("로그인 정상")
    void loginTestNormal() throws Exception {
        MockHttpServletRequestBuilder joinRequest = makeRequest("test@abc.co.kr", "3zDF!43A", "/member");
        mockMvc.perform(joinRequest);

        MockHttpServletRequestBuilder loginRequest = makeRequest("test@abc.co.kr", "3zDF!43A", "/member/login");
        MvcResult mvcResult = mockMvc.perform(loginRequest)
                .andExpect(status().isOk())
                .andReturn();

        String resultString = mvcResult.getResponse().getContentAsString();
        JwtTokenDto tokenDto = objectMapper.readValue(resultString, JwtTokenDto.class);
        assertThat(tokenDto.getToken()).isNotBlank();
    }

    @Test
    @DisplayName("로그인 비정상 - invalid argument")
    void loginTestAbnormalInvalidArguments() throws Exception {
        MockHttpServletRequestBuilder request = makeRequest("", "", "/member/login");
        mockMvc.perform(request)
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("로그인 비정상 - resource not found")
    void loginTestAbnormalResourceNotFound() throws Exception {
        MockHttpServletRequestBuilder request = makeRequest("test@abc.co.kr", "du12%1aC", "/member/login");
        MvcResult mvcResult = mockMvc.perform(request)
                .andExpect(status().isNotFound())
                .andReturn();
        String resultString = mvcResult.getResponse().getContentAsString();
        ApiSimpleError apiSimpleError = objectMapper.readValue(resultString, ApiSimpleError.class);
        assertThat(apiSimpleError.getMessage()).isEqualTo("Resource not found");
        System.out.println(apiSimpleError.getDetail());
    }

    @Test
    @DisplayName("로그인 비정상 - wrong password")
    void loginTestAbnormalWrongPassword() throws Exception {
        MockHttpServletRequestBuilder joinRequest = makeRequest("test@abc.co.kr", "du12%1aC", "/member");
        mockMvc.perform(joinRequest);

        MockHttpServletRequestBuilder loginRequest = makeRequest("test@abc.co.kr", "Ca1%21ud", "/member/login");
        MvcResult mvcResult = mockMvc.perform(loginRequest)
                .andExpect(status().isBadRequest())
                .andReturn();

        String resultString = mvcResult.getResponse().getContentAsString();
        ApiBindingError apiBindingError = objectMapper.readValue(resultString, ApiBindingError.class);
        assertThat(apiBindingError.getErrors().get("password").get(0)).isEqualTo("password가 일치하지 않습니다.");
    }

}
