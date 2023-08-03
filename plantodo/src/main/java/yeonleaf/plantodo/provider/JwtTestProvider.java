package yeonleaf.plantodo.provider;

import io.jsonwebtoken.Header;
import io.jsonwebtoken.JwtBuilder;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;

import javax.crypto.SecretKey;
import java.util.Base64;

public class JwtTestProvider implements JwtBasicProvider {

    private String plainKey = "zO61CkG7DzurakzrvCOUodF0dg6Fdfa3rMjk8Wex9NaTO6pWRDh9sy84HtoDgFwhhoFSPLAYxHQHazlVrp765Hl25ApLxtfTcj0c";

    private SecretKey key;

    @Override
    public SecretKey secretKey() {
        if (key == null) {
            key = _getSecretKey();
        }
        return key;
    }

    private SecretKey _getSecretKey() {
        String keyBase64Encoded = Base64.getEncoder().encodeToString(plainKey.getBytes());
        return Keys.hmacShaKeyFor(keyBase64Encoded.getBytes());
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
                .signWith(secretKey(), SignatureAlgorithm.HS256)
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
