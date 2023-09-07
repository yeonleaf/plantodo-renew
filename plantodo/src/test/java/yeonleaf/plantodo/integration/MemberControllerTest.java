package yeonleaf.plantodo.integration;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.transaction.Transactional;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;
import org.springframework.validation.BindingResult;
import yeonleaf.plantodo.dto.MemberReqDto;
import yeonleaf.plantodo.controller.MemberController;
import yeonleaf.plantodo.dto.MemberResDto;
import yeonleaf.plantodo.exceptions.ApiBindingError;
import yeonleaf.plantodo.exceptions.ApiSimpleError;
import yeonleaf.plantodo.dto.JwtTokenDto;
import yeonleaf.plantodo.interceptor.LoginCheckInterceptor;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * target : {@link MemberController}에 있는 모든 메소드.
 * target description : 회원 가입, 로그인 API. {@link LoginCheckInterceptor}가 적용되지 않는다.
 *                      성공한 경우 {@link MemberResDto}을 리턴하거나 {@link JwtTokenDto}를 리턴한다.
 *                      입력값에 문제가 있는 경우 {@link ApiBindingError}를 리턴한다.
 *                      중복값이 들어온 경우 {@link ApiSimpleError}를 리턴한다.
 * test description : 통합 테스트
 */
@SpringBootTest
@Transactional
@AutoConfigureMockMvc
@ActiveProfiles("test")
public class MemberControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    /**
     * 테스트 메이커
     * @param email test@abc.co.kr로 고정
     * @param password 임의의 회원 비밀번호
     * @param url /member or /member/login
     * @return mockMvc post 요청
     * @throws JsonProcessingException objectMapper.writeValueAsString();
     */
    private MockHttpServletRequestBuilder makePostRequest(String email, String password, String url) throws JsonProcessingException {

        MemberReqDto memberReqDto = new MemberReqDto(email, password);
        String requestData = objectMapper.writeValueAsString(memberReqDto);
        return post(url)
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestData);

    }

    /**
     * 회원가입 API 관련 테스트
     * @see MemberController#save(MemberReqDto, BindingResult)
     * @throws Exception mockMvc.perform();
     */
    @Test
    @DisplayName("회원가입 정상 - 리턴받은 MemberResDto에 id가 있는지 확인한다.")
    void joinTestNormal() throws Exception {

        // given
        MockHttpServletRequestBuilder request = makePostRequest("test@abc.co.kr", "3zDF!43A", "/member");

        // when - then
        mockMvc.perform(request)
                .andExpect(status().isCreated())
                .andExpect(jsonPath("id").isNumber());

    }

    @Test
    @DisplayName("회원가입 비정상 - 이메일이나 비밀번호의 형식에 문제가 있는 경우 ApiBindingError를 리턴한다. 문제가 있는 필드의 이름이 errors 객체에 키로 존재하는지 확인한다.")
    void joinTestAbnormalArguments() throws Exception {

        // given
        MockHttpServletRequestBuilder request = makePostRequest("test@abc.co.kr", "1z4F#", "/member");

        // when - then
        mockMvc.perform(request)
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("message").value("입력값 형식 오류"))
                .andExpect(jsonPath("errors.password").isNotEmpty());

    }

    @Test
    @DisplayName("회원가입 비정상 - 같은 이메일을 가진 회원이 이미 있는 경우 ApiSimpleError를 리턴한다. message 필드의 내용을 확인한다.")
    void joinTestAbnormalDuplicatedMember() throws Exception {

        // given
        MockHttpServletRequestBuilder request = makePostRequest("test@abc.co.kr", "3zDF!43A", "/member");
        mockMvc.perform(request); // 중복 회원가입을 테스트하기 위해서 같은 요청을 두 번 보냄

        // when - then
        mockMvc.perform(request)
                .andExpect(status().isConflict())
                .andExpect(jsonPath("message").value("Duplicated Member"));

    }


    /**
     * 로그인 API 관련 테스트
     * @see MemberController#login(MemberReqDto, BindingResult)
     * @throws Exception mockMvc.perform();
     */
    @Test
    @DisplayName("로그인 정상 - JwtTokenDto에 토큰이 있는지 확인한다.")
    void loginTestNormal() throws Exception {

        // given
        MockHttpServletRequestBuilder joinRequest = makePostRequest("test@abc.co.kr", "3zDF!43A", "/member");
        mockMvc.perform(joinRequest);

        MockHttpServletRequestBuilder loginRequest = makePostRequest("test@abc.co.kr", "3zDF!43A", "/member/login");

        // when - then
        mockMvc.perform(loginRequest)
                .andExpect(status().isOk())
                .andExpect(jsonPath("token").isString())
                .andDo(print());

    }

    @Test
    @DisplayName("로그인 비정상 - 이메일이나 비밀번호의 형식에 문제가 있는 경우 ApiBindingError를 리턴한다. errors 객체에 문제가 되는 필드의 이름이 key로 있는지 확인한다.")
    void loginTestAbnormalInvalidArguments() throws Exception {

        // given
        MockHttpServletRequestBuilder request = makePostRequest("", "", "/member/login");

        // when - then
        mockMvc.perform(request)
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("message").value("입력값 타입/내용 오류"))
                .andExpect(jsonPath("errors.password").isNotEmpty())
                .andExpect(jsonPath("errors.email").isNotEmpty());

    }

    @Test
    @DisplayName("로그인 비정상 - 로그인하고자 하는 회원 정보가 DB에 없는 경우 ApiSimpleError를 리턴한다. message 필드의 내용을 확인한다.")
    void loginTestAbnormalResourceNotFound() throws Exception {

        // given
        MockHttpServletRequestBuilder request = makePostRequest("test@abc.co.kr", "du12%1aC", "/member/login");

        // when - then
        mockMvc.perform(request)
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("message").value("Resource not found"));

    }

    @Test
    @DisplayName("로그인 비정상 - 이메일은 맞으나 비밀번호가 틀린 경우 ApiBindingError를 리턴한다. errors 객체에 비밀번호 key가 있는지 확인한다.")
    void loginTestAbnormalWrongPassword() throws Exception {

        // given
        MockHttpServletRequestBuilder joinRequest = makePostRequest("test@abc.co.kr", "du12%1aC", "/member");
        mockMvc.perform(joinRequest);

        MockHttpServletRequestBuilder loginRequest = makePostRequest("test@abc.co.kr", "Ca1%21ud", "/member/login");

        // when - then
        mockMvc.perform(loginRequest)
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("errors.password").isNotEmpty());

    }


}
