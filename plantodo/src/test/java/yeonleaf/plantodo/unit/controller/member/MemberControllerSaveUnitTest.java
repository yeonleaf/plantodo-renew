package yeonleaf.plantodo.unit.controller.member;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;
import org.springframework.validation.BindingResult;
import yeonleaf.plantodo.TestConfig;
import yeonleaf.plantodo.controller.MemberController;
import yeonleaf.plantodo.dto.MemberReqDto;
import yeonleaf.plantodo.dto.MemberResDto;
import yeonleaf.plantodo.exceptions.ApiBindingError;
import yeonleaf.plantodo.service.MemberServiceImpl;
import yeonleaf.plantodo.validator.JoinFormatCheckValidator;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * target : {@link MemberController#save(MemberReqDto, BindingResult)}
 * target description : MemberReqDto를 받아 필드값을 {@link JoinFormatCheckValidator}를 사용해 검증하고 검증에 통과하면 MemberService#save에 정보를 넘긴다.
 *
 * test description : 필드값 검증을 통과한 경우 {@link MemberResDto}를 반환하고 그 안에 id가 있는지 확인한다.
 *                    필드값 검증을 통과하지 못한 경우 {@link ApiBindingError}를 반환하고 내부에 문제가 발생한 필드에 대한 메시지가 있는지 확인한다.
 */
@Import(TestConfig.class)
@WebMvcTest(MemberController.class)
public class MemberControllerSaveUnitTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private MemberServiceImpl memberService;

    @Autowired
    private ObjectMapper objectMapper;

    MockHttpServletRequestBuilder makeSaveRequest(String email, String password) throws JsonProcessingException {

        // given
        MemberReqDto memberReqDto = new MemberReqDto(email, password);
        String requestData = objectMapper.writeValueAsString(memberReqDto);

        // when
        when(memberService.save(any())).thenReturn(new MemberResDto(1L, email, password));
        return post("/member")
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestData);

    }

    @Test
    @DisplayName("멤버 등록 - 정상 케이스 - 리턴값의 Http 상태코드가 200 OK여야 한다.")
    void saveTestNormal() throws Exception {

        // given - when
        MockHttpServletRequestBuilder request = makeSaveRequest("test@abc.co.kr", "sz81@Za3");

        // then
        mockMvc.perform(request)
                .andExpect(status().isCreated())
                .andDo(print());

    }

    @Test
    @DisplayName("멤버 등록 - 비정상 케이스 (파라미터 빈 문자열) - Http 상태코드가 400 Bad Request여야 한다.")
    void saveTestAbnormalEmptyString() throws Exception {

        // given - when
        MockHttpServletRequestBuilder request = makeSaveRequest("", "");

        // then
        mockMvc.perform(request)
                .andExpect(status().isBadRequest())
                .andDo(print());

    }

    @Test
    @DisplayName("멤버 등록 - 비정상 케이스 (파라미터 빈 문자열) - Http 상태코드가 400 Bad Request여야 한다.")
    void saveTestAbnormalSpace() throws Exception {

        // given - when
        MockHttpServletRequestBuilder request = makeSaveRequest(" ", " ");

        // then
        mockMvc.perform(request)
                .andExpect(status().isBadRequest());

    }

    @Test
    @DisplayName("멤버 등록 - 비정상 케이스 (이메일 정상, 비밀번호 형식 오류 1개) - password에 대한 에러 메시지 1개가 있어야 한다.")
    void saveTestInvalidPasswordFormatOne() throws Exception {

        // given - when
        MockHttpServletRequestBuilder request = makeSaveRequest("test@abc.co.kr", "41ab#");

        // then
        mockMvc.perform(request)
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("errors.password").isNotEmpty())
                .andExpect(jsonPath("errors.email").doesNotExist());

    }

    @Test
    @DisplayName("멤버 등록 - 비정상 케이스 (이메일 정상, 비밀번호 형식 오류 2개) - password에 대한 에러 메시지 2개가 있어야 한다.")
    void saveTestInvalidPasswordFormatTwo() throws Exception {

        // given - when
        MockHttpServletRequestBuilder request = makeSaveRequest("test@abc.co.kr", "41ab");

        // then
        mockMvc.perform(request)
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("errors.email").doesNotExist())
                .andExpect(jsonPath("errors.password.length()").value(2));

    }

    @Test
    @DisplayName("멤버 등록 - 비정상 케이스 (이메일 형식 오류 1개, 비밀번호 정상) - email에 대한 에러 메시지 1개가 있어야 한다.")
    void saveTestInvalidEmailFormatOne() throws Exception {

        // given - when
        MockHttpServletRequestBuilder request = makeSaveRequest("test@ab@c", "41ab$%za");

        // then
        mockMvc.perform(request)
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("errors.email.length()").value(1))
                .andExpect(jsonPath("errors.password").doesNotExist());

    }

    @Test
    @DisplayName("멤버 등록 - 비정상 케이스 (이메일 형식 오류 2개, 비밀번호 정상) - email에 대한 에러 메시지 2개가 있어야 한다.")
    void saveTestInvalidEmailFormatTwo() throws Exception {

        // given - when
        MockHttpServletRequestBuilder request = makeSaveRequest("te..st@ab한c", "41ab$%za");

        // then
        mockMvc.perform(request)
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("errors.email.length()").value(2))
                .andExpect(jsonPath("errors.password").doesNotExist());

    }

    @Test
    @DisplayName("멤버 등록 - 비정상 케이스 (이메일 형식 오류 1개, 비밀번호 형식 오류 1개) " +
            "- email에 대한 에러 메시지 1개, password에 대한 에러 메시지 1개가 있어야 한다.")
    void saveTestInvalidFormatBothOne() throws Exception {

        // given - when
        MockHttpServletRequestBuilder request = makeSaveRequest("test@ab한c", "41a$%za");

        // then
        mockMvc.perform(request)
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("errors.email.length()").value(1))
                .andExpect(jsonPath("errors.password.length()").value(1));

    }

    @Test
    @DisplayName("멤버 등록 - 비정상 케이스 (이메일 형식 오류 2개, 비밀번호 형식 오류 2개) " +
            "- email에 대한 에러 메시지 2개, password에 대한 에러 메시지 2개가 있어야 한다.")
    void saveTestInvalidFormatBothTwo() throws Exception {

        // given - when
        MockHttpServletRequestBuilder request = makeSaveRequest("tes..t@ab한c", "41a$/za");

        // then
        mockMvc.perform(request)
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("errors.email.length()").value(2))
                .andExpect(jsonPath("errors.password.length()").value(2));

    }

}
