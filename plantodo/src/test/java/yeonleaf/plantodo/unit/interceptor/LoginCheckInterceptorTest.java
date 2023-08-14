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
import yeonleaf.plantodo.WebConfig;
import yeonleaf.plantodo.interceptor.LoginCheckInterceptor;
import yeonleaf.plantodo.provider.JwtTestProvider;
import javax.crypto.SecretKey;
import java.time.Duration;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Date;
import yeonleaf.plantodo.exceptions.ApiSimpleError;
import yeonleaf.plantodo.JwtConfig;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * target : {@link LoginCheckInterceptor}
 * target description : {@link WebConfig#webMvcConfigurer()}에 addPathPatterns로 포함된 경로로 들어온 요청에 대해서 로그인 여부를 확인
 *                      (jwt토큰의 발급 여부를 확인하고 Exception을 던질지 컨트롤러로 보낼지 결정하는 인터셉터)
 *
 * test description : 더미 클래스인 {@link DummyController#ping()}으로 요청을 보냈을 때
 *                    정상 요청의 경우 "pong"을 리턴하고 정상 요청이 아닌 경우 {@link ApiSimpleError}를 리턴하는지, 메시지가 오류 상황에 맞는지 확인한다.
 *
 *                    ※ 실제 코드에서 사용하는 {@link JwtConfig}는 정상 Jwt 토큰만 반환하기 때문에
 *                    비정상 Jwt 토큰을 {@link JwtTestProvider#jwtBuilder()}를 사용해서 테스트용으로 사용한다.
 */
@Import({TestWithInterceptorConfig.class})
@WebMvcTest(DummyController.class)
public class LoginCheckInterceptorTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private SecretKey jwtTestSecretKey;

    @Autowired
    private JwtTestProvider jwtTestProvider;

    /**
     * 정상적인 Jwt 토큰을 리턴하는 테스트 메소드
     */
    private String makeValidKey() {

        return jwtTestProvider.jwtBuilder().signWith(jwtTestSecretKey, SignatureAlgorithm.HS256)
                .setIssuedAt(new Date())
                .setExpiration(new Date(new Date().getTime() + Duration.ofMinutes(30).toMillis()))
                .claim("email", "test@abc.co.kr").compact();

    }

    /**
     * 다른 서버에서 발급한 비정상 Jwt 토큰을 리턴하는 테스트 메소드
     */
    private String makeInvalidKey() {

        return jwtTestProvider.jwtBuilder().signWith(Keys.secretKeyFor(SignatureAlgorithm.HS256), SignatureAlgorithm.HS256)
                .setIssuedAt(new Date())
                .setExpiration(new Date(new Date().getTime() + Duration.ofMinutes(30).toMillis()))
                .claim("email", "test@abc.co.kr").compact();

    }

    @Test
    @DisplayName("정상 요청의 경우 'pong'을 리턴한다.")
    void testLoggedIn() throws Exception {

        String key = makeValidKey();
        MockHttpServletRequestBuilder request = get("/ping")
                .header("Authorization", "Bearer " + key);
        mockMvc.perform(request)
                .andExpect(status().isOk())
                .andExpect(content().string("pong"));

    }

    @Test
    @DisplayName("비정상 요청 - 다른 조건을 만족하면서 Authorization 헤더가 없는 경우 ApiSimpleError를 리턴한다.")
    void testNotLoggedIn() throws Exception {

        MockHttpServletRequestBuilder request = get("/ping");
        mockMvc.perform(request)
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("detail").value("Authorization 헤더가 없음"));

    }

    @Test
    @DisplayName("비정상 요청 - 다른 조건은 만족하면서 헤더 형식에 Bearer가 없는 경우 ApiSimpleError를 리턴한다.")
    void testInvalidHeader() throws Exception {

        MockHttpServletRequestBuilder request = get("/ping")
                .header("Authorization", makeValidKey());
        mockMvc.perform(request)
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("detail").value("JWT 헤더가 `Bearer `로 시작하지 않음"));

    }

    @Test
    @DisplayName("비정상 요청 - 다른 조건은 만족하면서 헤더에 오타 발생하는 경우 ApiSimpleError를 리턴한다.")
    void testInvalidHeader2() throws Exception {

        MockHttpServletRequestBuilder request = get("/ping")
                .header("Authorization", "Bearr " + makeValidKey());
        mockMvc.perform(request)
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("detail").value("JWT 헤더가 `Bearer `로 시작하지 않음"));

    }

    @Test
    @DisplayName("비정상 요청 - 다른 조건은 만족하면서 이 서버에서 발행한 토큰이 아닐 경우 ApiSimpleError를 리턴한다.")
    void testInvalidToken() throws Exception {

        MockHttpServletRequestBuilder request = get("/ping")
                .header("Authorization", "Bearer " + makeInvalidKey());
        mockMvc.perform(request)
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("detail").value("JWT TOKEN이 유효하지 않음"));

    }

    /**
     * 만료된 비정상 Jwt 토큰을 리턴하는 테스트 메소드
     * @param issuedTime (LocalDateTime) 현재 서버시간을 기준으로 과거의 시간
     * @return 만료된 Jwt 토큰을 반환
     */
    private String makeValidKeyByDate(LocalDateTime issuedTime) {
        LocalDateTime expiredTime = issuedTime.plusMinutes(30);
        return jwtTestProvider.jwtBuilder().signWith(jwtTestSecretKey, SignatureAlgorithm.HS256)
                .setIssuedAt(Date.from(issuedTime.atZone(ZoneId.systemDefault()).toInstant()))
                .setExpiration(Date.from(expiredTime.atZone(ZoneId.systemDefault()).toInstant()))
                .claim("email", "test@abe.co.kr")
                .compact();
    }

    @Test
    @DisplayName("비정상 요청 - 다른 조건은 만족하면서 만료된 토큰인 경우 ApiSimpleError를 리턴한다.")
    void testExpiredToken() throws Exception {

        String expiredKey = makeValidKeyByDate(LocalDateTime.of(2023, 7, 8, 0, 0, 0));
        MockHttpServletRequestBuilder request = get("/ping")
                .header("Authorization", "Bearer " + expiredKey);
        mockMvc.perform(request)
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("detail").value("JWT TOKEN이 만료 (재로그인 필요)"));

    }

}
