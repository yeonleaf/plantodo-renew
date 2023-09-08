package yeonleaf.plantodo.provider;

import io.jsonwebtoken.JwtBuilder;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;

@Component
public interface JwtBasicProvider {

    JwtBuilder jwtBuilder();
    SecretKey secretKey();
    String generateToken(Long id);
    void parseToken(String header);
    void validateToken(String header);
    String extractToken(String header);
    Long getIdFromToken(String token);

}
