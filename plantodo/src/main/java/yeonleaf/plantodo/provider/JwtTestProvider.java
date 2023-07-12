package yeonleaf.plantodo.provider;

import io.jsonwebtoken.Header;
import io.jsonwebtoken.JwtBuilder;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;

import javax.crypto.SecretKey;

public class JwtTestProvider implements JwtBasicProvider {

    private SecretKey key = Keys.secretKeyFor(SignatureAlgorithm.HS256);

    @Override
    public SecretKey secretKey() {
        return key;
    }

    @Override
    public JwtBuilder jwtBuilder() {
        return Jwts.builder()
                .setHeaderParam(Header.TYPE, Header.JWT_TYPE)
                .setIssuer("fresh");
    }

    @Override
    public String generateToken(Long id) {
        return jwtBuilder()
                .signWith(key, SignatureAlgorithm.HS256)
                .claim("id", id)
                .compact();
    }

    @Override
    public void parseToken(String header) {

    }

    @Override
    public void validateToken(String header) {

    }

    @Override
    public String extractToken(String header) {
        return null;
    }

    @Override
    public Long getIdFromToken(String token) {
        return 1L;
    }

}
