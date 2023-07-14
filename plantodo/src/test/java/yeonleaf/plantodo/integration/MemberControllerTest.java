package yeonleaf.plantodo.integration;

import com.fasterxml.jackson.core.JsonProcessingException;
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
import yeonleaf.plantodo.dto.JwtTokenDto;
import yeonleaf.plantodo.dto.MemberReqDto;
import yeonleaf.plantodo.dto.MemberResDto;
import yeonleaf.plantodo.exceptions.ApiBindingError;
import yeonleaf.plantodo.exceptions.ApiSimpleError;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@Transactional
@AutoConfigureMockMvc
public class MemberControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    private MockHttpServletRequestBuilder makePostRequest(String email, String password, String url) throws JsonProcessingException {

        MemberReqDto memberReqDto = new MemberReqDto(email, password);
        String requestData = objectMapper.writeValueAsString(memberReqDto);
        return post(url)
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestData);

    }

    @Test
    @DisplayName("회원가입 정상")
    void joinTestNormal() throws Exception {

        MockHttpServletRequestBuilder request = makePostRequest("test@abc.co.kr", "3zDF!43A", "/member");

        mockMvc.perform(request)
                .andExpect(status().isCreated())
                .andExpect(jsonPath("id").isNumber());

    }

    @Test
    @DisplayName("회원가입 비정상 - invalid argument")
    void joinTestAbnormalArguments() throws Exception {

        MockHttpServletRequestBuilder request = makePostRequest("test@abc.co.kr", "1z4F#", "/member");

        mockMvc.perform(request)
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("message").value("입력값 형식 오류"))
                .andExpect(jsonPath("errors.password").isNotEmpty());

    }

    @Test
    @DisplayName("회원가입 비정상 - duplicated member")
    void joinTestAbnormalDuplicatedMember() throws Exception {

        MockHttpServletRequestBuilder request = makePostRequest("test@abc.co.kr", "3zDF!43A", "/member");
        mockMvc.perform(request);

        mockMvc.perform(request)
                .andExpect(status().isConflict())
                .andExpect(jsonPath("message").value("Duplicated Member"));

    }

    @Test
    @DisplayName("로그인 정상")
    void loginTestNormal() throws Exception {

        MockHttpServletRequestBuilder joinRequest = makePostRequest("test@abc.co.kr", "3zDF!43A", "/member");
        mockMvc.perform(joinRequest);

        MockHttpServletRequestBuilder loginRequest = makePostRequest("test@abc.co.kr", "3zDF!43A", "/member/login");
        mockMvc.perform(loginRequest)
                .andExpect(status().isOk())
                .andExpect(jsonPath("token").isString())
                .andDo(print());

    }

    @Test
    @DisplayName("로그인 비정상 - invalid argument")
    void loginTestAbnormalInvalidArguments() throws Exception {

        MockHttpServletRequestBuilder request = makePostRequest("", "", "/member/login");
        mockMvc.perform(request)
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("message").value("입력값 타입/내용 오류"))
                .andExpect(jsonPath("errors.password").isNotEmpty())
                .andExpect(jsonPath("errors.email").isNotEmpty());

    }

    @Test
    @DisplayName("로그인 비정상 - resource not found")
    void loginTestAbnormalResourceNotFound() throws Exception {

        MockHttpServletRequestBuilder request = makePostRequest("test@abc.co.kr", "du12%1aC", "/member/login");
        mockMvc.perform(request)
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("message").value("Resource not found"));

    }

    @Test
    @DisplayName("로그인 비정상 - wrong password")
    void loginTestAbnormalWrongPassword() throws Exception {

        MockHttpServletRequestBuilder joinRequest = makePostRequest("test@abc.co.kr", "du12%1aC", "/member");
        mockMvc.perform(joinRequest);

        MockHttpServletRequestBuilder loginRequest = makePostRequest("test@abc.co.kr", "Ca1%21ud", "/member/login");
        mockMvc.perform(loginRequest)
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("errors.password").isNotEmpty());

    }

    @Test
    @DisplayName("회원 삭제 정상")
    void deleteTestNormal() throws Exception {

        MockHttpServletRequestBuilder joinRequest = makePostRequest("test@abc.co.kr", "du12%1aC", "/member");

        MvcResult mvcResult = mockMvc.perform(joinRequest)
                .andReturn();

        long memberId = Long.parseLong(JsonPath.read(mvcResult.getResponse().getContentAsString(), "$.id").toString());

        MockHttpServletRequestBuilder deleteRequest = delete("/member/" + memberId);
        mockMvc.perform(deleteRequest)
                .andExpect(status().isNoContent());

    }

    @Test
    @DisplayName("회원 삭제 비정상 - resource not found")
    void deleteTestAbnormal() throws Exception {

        MockHttpServletRequestBuilder deleteRequest = delete("/member/1");
        mockMvc.perform(deleteRequest)
                .andExpect(status().isNotFound());

    }

}
