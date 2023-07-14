package yeonleaf.plantodo.unit.interceptor;

import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;
import yeonleaf.plantodo.TestWithInterceptorConfig;
import yeonleaf.plantodo.provider.JwtTestProvider;
import javax.crypto.SecretKey;
import java.time.Duration;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Date;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@Import({TestWithInterceptorConfig.class})
@WebMvcTest(DummyController.class)
public class LoginCheckInterceptorTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private SecretKey jwtTestSecretKey;

    @Autowired
    private JwtTestProvider jwtTestProvider;

    private String makeValidKey() {

        return jwtTestProvider.jwtBuilder().signWith(jwtTestSecretKey, SignatureAlgorithm.HS256)
                .setIssuedAt(new Date())
                .setExpiration(new Date(new Date().getTime() + Duration.ofMinutes(30).toMillis()))
                .claim("email", "test@abc.co.kr").compact();

    }

    private String makeInvalidKey() {

        return jwtTestProvider.jwtBuilder().signWith(Keys.secretKeyFor(SignatureAlgorithm.HS256), SignatureAlgorithm.HS256)
                .setIssuedAt(new Date())
                .setExpiration(new Date(new Date().getTime() + Duration.ofMinutes(30).toMillis()))
                .claim("email", "test@abc.co.kr").compact();

    }

    @Test
    @DisplayName("정상 요청")
    void testLoggedIn() throws Exception {

        String key = makeValidKey();
        MockHttpServletRequestBuilder request = get("/ping")
                .header("Authorization", "Bearer " + key);
        mockMvc.perform(request)
                .andExpect(status().isOk())
                .andExpect(content().string("pong"));

    }

    @Test
    @DisplayName("비정상 요청 - 다른 조건을 만족하면서 Authorization 헤더가 없음")
    void testNotLoggedIn() throws Exception {

        MockHttpServletRequestBuilder request = get("/ping");
        mockMvc.perform(request)
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("detail").value("Authorization 헤더가 없음"));

    }

    @Test
    @DisplayName("비정상 요청 - 다른 조건은 만족하면서 헤더 형식에 Bearer가 없음")
    void testInvalidHeader() throws Exception {

        MockHttpServletRequestBuilder request = get("/ping")
                .header("Authorization", makeValidKey());
        mockMvc.perform(request)
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("detail").value("JWT 헤더가 `Bearer `로 시작하지 않음"));

    }

    @Test
    @DisplayName("비정상 요청 - 다른 조건은 만족하면서 헤더에 오타 발생")
    void testInvalidHeader2() throws Exception {

        MockHttpServletRequestBuilder request = get("/ping")
                .header("Authorization", "Bearr " + makeValidKey());
        mockMvc.perform(request)
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("detail").value("JWT 헤더가 `Bearer `로 시작하지 않음"));

    }

    @Test
    @DisplayName("비정상 요청 - 다른 조건은 만족하면서 이 서버에서 발행한 토큰이 아님")
    void testInvalidToken() throws Exception {

        MockHttpServletRequestBuilder request = get("/ping")
                .header("Authorization", "Bearer " + makeInvalidKey());
        mockMvc.perform(request)
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("detail").value("JWT TOKEN이 유효하지 않음"));

    }

    private String makeValidKeyByDate(LocalDateTime issuedTime) {
        LocalDateTime expiredTime = issuedTime.plusMinutes(30);
        return jwtTestProvider.jwtBuilder().signWith(jwtTestSecretKey, SignatureAlgorithm.HS256)
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
