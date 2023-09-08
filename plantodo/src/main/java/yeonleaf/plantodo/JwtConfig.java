package yeonleaf.plantodo;

import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.DependsOn;
import yeonleaf.plantodo.provider.JwtBasicProvider;
import yeonleaf.plantodo.provider.JwtProvider;

import javax.crypto.SecretKey;

@Configuration
@RequiredArgsConstructor
public class JwtConfig {

    private final JwtProvider jwtProvider;

    @Bean
    public SecretKey jwtSecretKey() {
        return jwtProvider.secretKey();
    }

}
