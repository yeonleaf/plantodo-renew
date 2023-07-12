package yeonleaf.plantodo.provider;

import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import io.jsonwebtoken.security.SignatureException;
import lombok.NoArgsConstructor;
import org.springframework.stereotype.Component;
import yeonleaf.plantodo.exceptions.CustomJwtException;

import javax.crypto.SecretKey;
import java.time.Duration;
import java.util.Date;

@NoArgsConstructor
public class JwtProvider implements JwtBasicProvider {

    private SecretKey key = Keys.secretKeyFor(SignatureAlgorithm.HS256);

    @Override
    public JwtBuilder jwtBuilder() {
        Date now = new Date();
        return Jwts.builder()
                .setHeaderParam(Header.TYPE, Header.JWT_TYPE)
                .setIssuer("fresh")
                .setIssuedAt(now)
                .setExpiration(new Date(now.getTime() + Duration.ofMinutes(30).toMillis()))
                .signWith(key, SignatureAlgorithm.HS256);
    }

    @Override
    public String generateToken(Long id) {
        return jwtBuilder()
                .claim("id", id)
                .compact();
    }

    @Override
    public void parseToken(String header) {
        validateToken(header);

        String token = extractToken(header);

        try {
            Jwts.parserBuilder()
                    .setSigningKey(key)
                    .build()
                    .parseClaimsJws(token)
                    .getBody();
        } catch (MalformedJwtException | SignatureException mje) {
            throw new CustomJwtException("JWT TOKEN이 유효하지 않음");
        } catch (ExpiredJwtException eje) {
            throw new CustomJwtException("JWT TOKEN이 만료 (재로그인 필요)");
        }
    }

    @Override
    public void validateToken(String header) {
        if (header == null) {
            throw new CustomJwtException("Authorization 헤더가 없음");
        }

        if (!header.startsWith("Bearer ")) {
            throw new CustomJwtException("JWT 헤더가 `Bearer `로 시작하지 않음");
        }
    }

    @Override
    public String extractToken(String header) {
        return header.substring("Bearer ".length());
    }

    @Override
    public SecretKey secretKey() {
        return key;
    }

    public Long getIdFromToken(String token) {
        Claims body = Jwts.parserBuilder()
                .setSigningKey(key)
                .build()
                .parseClaimsJws(token)
                .getBody();
        return body.get("id", Long.class);
    }

}
