package yeonleaf.plantodo.provider;

import io.jsonwebtoken.JwtBuilder;

import javax.crypto.SecretKey;

public interface JwtBasicProvider {

    JwtBuilder jwtBuilder();
    SecretKey secretKey();
    String generateToken(Long id);
    void parseToken(String header);
    void validateToken(String header);
    String extractToken(String header);
    Long getIdFromToken(String token);

}
