package yeonleaf.plantodo.unit.controller.member;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.jsonwebtoken.Header;
import io.jsonwebtoken.JwtBuilder;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
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
import yeonleaf.plantodo.dto.JwtTokenDto;
import yeonleaf.plantodo.dto.MemberReqDto;
import yeonleaf.plantodo.exceptions.ApiBindingError;
import yeonleaf.plantodo.exceptions.ApiSimpleError;
import yeonleaf.plantodo.exceptions.ResourceNotFoundException;
import yeonleaf.plantodo.service.MemberService;
import java.time.Duration;
import java.util.Date;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(MemberController.class)
public class MemberControllerLoginUnitTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    JwtBuilder jwtBuilder;

    @MockBean
    MemberService memberService;

    private JwtBuilder jwtTestBuilder() {
        Date now = new Date();

        return Jwts.builder()
                .setHeaderParam(Header.TYPE, Header.JWT_TYPE)
                .setIssuer("fresh")
                .setIssuedAt(now)
                .setExpiration(new Date(now.getTime() + Duration.ofMinutes(30).toMillis()))
                .signWith(Keys.secretKeyFor(SignatureAlgorithm.HS256), SignatureAlgorithm.HS256);
    }

    ObjectMapper objectMapper = new ObjectMapper();

    private MockHttpServletRequestBuilder makeLoginRequest(MemberReqDto memberReqDto) throws JsonProcessingException {
        String requestData = objectMapper.writeValueAsString(memberReqDto);
        return post("/member/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestData);
    }

    private Object parseResult(String target, Object object) throws JsonProcessingException {
        return objectMapper.readValue(target, object.getClass());
    }

    @Test
    @DisplayName("정상 로그인")
    void loginTest_normal() throws Exception {
        MemberReqDto memberReqDto = new MemberReqDto("test@abc.co.kr", "13za$%a1");
        MockHttpServletRequestBuilder request = makeLoginRequest(memberReqDto);

        when(memberService.login(any())).thenReturn(1L);
        when(jwtBuilder.claim(any(), any())).thenReturn(jwtTestBuilder().claim("id", 1L));

        MvcResult mvcResult = mockMvc.perform(request)
                .andExpect(status().isOk())
                .andReturn();

        JwtTokenDto body = (JwtTokenDto) parseResult(mvcResult.getResponse().getContentAsString(), new JwtTokenDto(""));
        assertThat(body.getToken()).isNotBlank();
    }

    @Test
    @DisplayName("비정상 로그인 - 이메일 형식 오류 - blank")
    void loginTest_abnormal_format_emailBlank() throws Exception {
        MemberReqDto memberReqDto = new MemberReqDto("", "13za$%a1");
        MockHttpServletRequestBuilder request = makeLoginRequest(memberReqDto);

        when(memberService.login(any())).thenReturn(1L);
        when(jwtBuilder.claim(any(), any())).thenReturn(jwtTestBuilder().claim("id", 1L));

        MvcResult mvcResult = mockMvc.perform(request)
                .andExpect(status().isBadRequest())
                .andReturn();

        ApiBindingError body = (ApiBindingError) parseResult(mvcResult.getResponse().getContentAsString(), new ApiBindingError());
        assertThat(body.getErrors().get("email").size()).isEqualTo(1);
    }

    @Test
    @DisplayName("비정상 로그인 - 유저를 찾을 수 없음")
    void loginTest_abnormal_cannotFindMember() throws Exception {
        MemberReqDto memberReqDto = new MemberReqDto("test@abc.co.kr", "13za$%a1");
        MockHttpServletRequestBuilder request = makeLoginRequest(memberReqDto);

        doThrow(ResourceNotFoundException.class).when(memberService).login(any());
        when(jwtBuilder.claim(any(), any())).thenReturn(jwtTestBuilder().claim("id", 1L));

        MvcResult mvcResult = mockMvc.perform(request)
                .andExpect(status().isNotFound())
                .andReturn();

        ApiSimpleError apiSimpleError = objectMapper.readValue(mvcResult.getResponse().getContentAsString(), ApiSimpleError.class);
        assertThat(apiSimpleError.getMessage()).isEqualTo("Resource not found");
    }

}
