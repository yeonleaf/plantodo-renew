package yeonleaf.plantodo.interceptor;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.MalformedJwtException;
import io.jsonwebtoken.security.SignatureException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.web.servlet.HandlerInterceptor;
import yeonleaf.plantodo.exceptions.ApiSimpleError;
import yeonleaf.plantodo.exceptions.CustomJwtException;
import javax.crypto.SecretKey;
import java.io.IOException;

@RequiredArgsConstructor
public class LoginCheckInterceptor implements HandlerInterceptor {
    private final SecretKey jwtSecretKey;
    private final ObjectMapper objectMapper;

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        String authHeader = request.getHeader(HttpHeaders.AUTHORIZATION);
        try {
            parseJwtToken(authHeader);
        } catch (CustomJwtException ex) {
            setResponseError(response, ex);
            return false;
        }
        return true;
    }

    private void setResponseError(HttpServletResponse response, CustomJwtException ex) throws IOException {
        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        response.setCharacterEncoding("UTF-8");
        response.getWriter().write(objectMapper.writeValueAsString(new ApiSimpleError("JWT error", ex.getMessage())));
    }

    private void parseJwtToken(String header) {
        validateToken(header);
        String token = extractToken(header);

        try {
            Jwts.parserBuilder()
                    .setSigningKey(jwtSecretKey)
                    .build()
                    .parseClaimsJws(token)
                    .getBody();
        } catch (MalformedJwtException | SignatureException mje) {
            throw new CustomJwtException("JWT TOKEN이 유효하지 않음");
        } catch (ExpiredJwtException eje) {
            throw new CustomJwtException("JWT TOKEN이 만료 (재로그인 필요)");
        }
    }

    private void validateToken(String header) {
        if (header == null) {
            throw new CustomJwtException("Authorization 헤더가 없음");
        }

        if (!header.startsWith("Bearer ")) {
            throw new CustomJwtException("JWT 헤더가 `Bearer `로 시작하지 않음");
        }
    }

    private String extractToken(String header) {
        return header.substring("Bearer ".length());
    }
}
