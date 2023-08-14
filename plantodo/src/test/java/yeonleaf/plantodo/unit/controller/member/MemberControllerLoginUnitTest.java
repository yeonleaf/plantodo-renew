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
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;
import org.springframework.validation.BindingResult;
import yeonleaf.plantodo.TestConfig;
import yeonleaf.plantodo.controller.MemberController;
import yeonleaf.plantodo.dto.JwtTokenDto;
import yeonleaf.plantodo.dto.MemberReqDto;
import yeonleaf.plantodo.exceptions.ApiBindingError;
import yeonleaf.plantodo.exceptions.ApiSimpleError;
import yeonleaf.plantodo.exceptions.ResourceNotFoundException;
import yeonleaf.plantodo.provider.JwtProvider;
import yeonleaf.plantodo.service.MemberService;
import java.time.Duration;
import java.util.Date;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * target : {@link MemberController#login(MemberReqDto, BindingResult)}
 * target description : {@link MemberReqDto}를 검증한 후 문제가 없으면 Jwt Token을 리턴한다.
 *
 * test description : 정상값을 넣었을 때 {@link JwtTokenDto}를 리턴하는지 확인한다.
 *                    타입이 비정상인 값을 넣었을 때 {@link ApiBindingError}를 리턴하고 오류가 난 필드를 errors 리스트에 포함하고 있는지 확인한다.
 *                    가입된 멤버가 없을 경우 {@link ApiSimpleError}를 리턴하고 메시지가 Resource not found와 일치하는지 확인한다.
 */
@WebMvcTest(MemberController.class)
public class MemberControllerLoginUnitTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    JwtProvider jwtProvider;

    @MockBean
    MemberService memberService;

    ObjectMapper objectMapper = new ObjectMapper();

    private MockHttpServletRequestBuilder makeLoginRequest(MemberReqDto memberReqDto) throws JsonProcessingException {
        String requestData = objectMapper.writeValueAsString(memberReqDto);
        return post("/member/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestData);
    }

    /**
     * 정상 Jwt 토큰을 리턴하는 테스트 메소드
     */
    private String validJwtToken() {
        Date now = new Date();

        return Jwts.builder()
                .setHeaderParam(Header.TYPE, Header.JWT_TYPE)
                .setIssuer("fresh")
                .setIssuedAt(now)
                .setExpiration(new Date(now.getTime() + Duration.ofMinutes(30).toMillis()))
                .signWith(Keys.secretKeyFor(SignatureAlgorithm.HS256), SignatureAlgorithm.HS256)
                .claim("id", 1L).compact();
    }


    @Test
    @DisplayName("정상 로그인의 경우 토큰을 리턴한다.")
    void loginTest_normal() throws Exception {

        // given
        MemberReqDto memberReqDto = new MemberReqDto("test@abc.co.kr", "13za$%a1");
        MockHttpServletRequestBuilder request = makeLoginRequest(memberReqDto);

        // when
        when(memberService.login(any())).thenReturn(1L);
        when(jwtProvider.generateToken(any())).thenReturn(validJwtToken());

        // then
        mockMvc.perform(request)
                .andExpect(status().isOk())
                .andExpect(jsonPath("token").isString())
                .andExpect(jsonPath("_links.plans").exists());

    }

    @Test
    @DisplayName("비정상 로그인 - 이메일 형식 오류 - blank인 경우 ApiBindingError를 리턴한다.")
    void loginTest_abnormal_format_emailBlank() throws Exception {

        // given
        MemberReqDto memberReqDto = new MemberReqDto("", "13za$%a1");
        MockHttpServletRequestBuilder request = makeLoginRequest(memberReqDto);

        // when
        when(memberService.login(any())).thenReturn(1L);
        when(jwtProvider.generateToken(any())).thenReturn(validJwtToken());

        // then
        mockMvc.perform(request)
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("errors.email").isNotEmpty());

    }

    @Test
    @DisplayName("비정상 로그인 - 유저를 찾을 수 없는 경우 ApiSimpleError를 리턴한다.")
    void loginTest_abnormal_cannotFindMember() throws Exception {

        // given
        MemberReqDto memberReqDto = new MemberReqDto("test@abc.co.kr", "13za$%a1");
        MockHttpServletRequestBuilder request = makeLoginRequest(memberReqDto);

        // when
        doThrow(ResourceNotFoundException.class).when(memberService).login(any());
        when(jwtProvider.generateToken(any())).thenReturn(validJwtToken());

        // then
        mockMvc.perform(request)
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("message").value("Resource not found"));

    }

}
