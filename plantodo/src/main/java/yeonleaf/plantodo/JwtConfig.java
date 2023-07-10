package yeonleaf.plantodo;

import io.jsonwebtoken.Header;
import io.jsonwebtoken.JwtBuilder;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import javax.crypto.SecretKey;
import java.time.Duration;
import java.util.Date;

@Configuration
public class JwtConfig {
    private SecretKey key;

    public JwtConfig() {
        this.key = Keys.secretKeyFor(SignatureAlgorithm.HS256);
    }

    @Bean
    public SecretKey jwtSecretKey() {
        return key;
    }

    @Bean
    public JwtBuilder jwtBuilder() {
        Date now = new Date();

        return Jwts.builder()
                .setHeaderParam(Header.TYPE, Header.JWT_TYPE)
                .setIssuer("fresh")
                .setIssuedAt(now)
                .setExpiration(new Date(now.getTime() + Duration.ofMinutes(30).toMillis()))
                .signWith(jwtSecretKey(), SignatureAlgorithm.HS256);
    }
}
