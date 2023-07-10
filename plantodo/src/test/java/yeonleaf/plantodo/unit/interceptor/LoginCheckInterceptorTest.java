package yeonleaf.plantodo.unit.interceptor;

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
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
import yeonleaf.plantodo.exceptions.ApiSimpleError;
import yeonleaf.plantodo.interceptor.LoginCheckInterceptor;

import javax.crypto.SecretKey;
import java.io.UnsupportedEncodingException;
import java.time.Duration;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Date;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(DummyController.class)
public class LoginCheckInterceptorTest {

    @TestConfiguration
    static class TestConfig {
        private SecretKey key;
        private ObjectMapper objectMapper;

        public TestConfig() {
            this.key = Keys.secretKeyFor(SignatureAlgorithm.HS256);
            this.objectMapper = new ObjectMapper();
        }

        @Bean
        public ObjectMapper objectMapper() {
            return objectMapper;
        }

        @Bean
        public SecretKey jwtTestSecretKey() {
            return key;
        }

        @Bean
        public JwtBuilder jwtTestBuilder() {
            return Jwts.builder()
                    .setHeaderParam(Header.TYPE, Header.JWT_TYPE)
                    .setIssuer("fresh");
        }

        @Bean
        public WebMvcConfigurer testWebMvcConfigurer() {
            return new WebMvcConfigurer() {
                @Override
                public void addInterceptors(InterceptorRegistry registry) {
                    registry.addInterceptor(new LoginCheckInterceptor(jwtTestSecretKey(), objectMapper()))
                            .excludePathPatterns("/member", "/member/login");
                }
            };
        }
    }

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private JwtBuilder jwtTestBuilder;

    @Autowired
    private SecretKey jwtTestSecretKey;

    @Autowired
    private ObjectMapper objectMapper;

    private String makeValidKey() {
        return jwtTestBuilder.signWith(jwtTestSecretKey, SignatureAlgorithm.HS256)
                .setIssuedAt(new Date())
                .setExpiration(new Date(new Date().getTime() + Duration.ofMinutes(30).toMillis()))
                .claim("email", "test@abc.co.kr").compact();
    }

    private String makeInvalidKey() {
        return jwtTestBuilder.signWith(Keys.secretKeyFor(SignatureAlgorithm.HS256), SignatureAlgorithm.HS256)
                .setIssuedAt(new Date())
                .setExpiration(new Date(new Date().getTime() + Duration.ofMinutes(30).toMillis()))
                .claim("email", "test@abc.co.kr").compact();
    }

    private ApiSimpleError parseToApiSimpleError(String target) throws JsonProcessingException {
        return objectMapper.readValue(target, ApiSimpleError.class);
    }

    @Test
    @DisplayName("정상 요청")
    void testLoggedIn() throws Exception {
        String key = makeValidKey();
        MockHttpServletRequestBuilder request = get("/ping")
                .header("Authorization", "Bearer " + key);
        MvcResult mvcResult = mockMvc.perform(request)
                .andExpect(status().isOk())
                .andReturn();
        assertThat(mvcResult.getResponse().getContentAsString()).isEqualTo("pong");
    }

    private void validateMvcResultContent(MvcResult mvcResult, String detail) throws UnsupportedEncodingException, JsonProcessingException {
        String resultString = mvcResult.getResponse().getContentAsString();
        ApiSimpleError apiSimpleError = parseToApiSimpleError(resultString);
        assertThat(apiSimpleError.getDetail()).isEqualTo(detail);
    }

    @Test
    @DisplayName("비정상 요청 - 다른 조건을 만족하면서 Authorization 헤더가 없음")
    void testNotLoggedIn() throws Exception {
        MockHttpServletRequestBuilder request = get("/ping");
        MvcResult mvcResult = mockMvc.perform(request)
                .andExpect(status().isUnauthorized())
                .andReturn();
        validateMvcResultContent(mvcResult, "Authorization 헤더가 없음");
    }

    @Test
    @DisplayName("비정상 요청 - 다른 조건은 만족하면서 헤더 형식에 Bearer가 없음")
    void testInvalidHeader() throws Exception {
        String key = makeValidKey();
        MockHttpServletRequestBuilder request = get("/ping")
                .header("Authorization", key);
        MvcResult mvcResult = mockMvc.perform(request)
                .andExpect(status().isUnauthorized())
                .andReturn();
        validateMvcResultContent(mvcResult, "JWT 헤더가 `Bearer `로 시작하지 않음");
    }

    @Test
    @DisplayName("비정상 요청 - 다른 조건은 만족하면서 헤더에 오타 발생")
    void testInvalidHeader2() throws Exception {
        String key = makeValidKey();
        MockHttpServletRequestBuilder request = get("/ping")
                .header("Authorization", "Bearr " + key);
        MvcResult mvcResult = mockMvc.perform(request)
                .andExpect(status().isUnauthorized())
                .andReturn();
        validateMvcResultContent(mvcResult, "JWT 헤더가 `Bearer `로 시작하지 않음");
    }

    @Test
    @DisplayName("비정상 요청 - 다른 조건은 만족하면서 이 서버에서 발행한 토큰이 아님")
    void testInvalidToken() throws Exception {
        String key = makeInvalidKey();
        MockHttpServletRequestBuilder request = get("/ping")
                .header("Authorization", "Bearer " + key);
        MvcResult mvcResult = mockMvc.perform(request)
                .andExpect(status().isUnauthorized())
                .andReturn();
        validateMvcResultContent(mvcResult, "JWT TOKEN이 유효하지 않음");
    }

    private String makeValidKeyByDate(LocalDateTime issuedTime) {
        LocalDateTime expiredTime = issuedTime.plusMinutes(30);
        return jwtTestBuilder.signWith(jwtTestSecretKey, SignatureAlgorithm.HS256)
                .setIssuedAt(Date.from(issuedTime.atZone(ZoneId.systemDefault()).toInstant()))
                .setExpiration(Date.from(expiredTime.atZone(ZoneId.systemDefault()).toInstant()))
                .claim("email", "test@abe.co.kr")
                .compact();
    }

    @Test
    @DisplayName("비정상 요청 - 다른 조건은 만족하면서 만료된 토큰")
    void testExpiredToken() throws Exception {
        String expiredKey = makeValidKeyByDate(LocalDateTime.of(2023, 7, 8, 0, 0, 0));
        MockHttpServletRequestBuilder request = get("/ping")
                .header("Authorization", "Bearer " + expiredKey);
        mockMvc.perform(request)
                .andExpect(status().isUnauthorized());
    }
}
