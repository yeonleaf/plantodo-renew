package yeonleaf.plantodo;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.DependsOn;
import yeonleaf.plantodo.provider.JwtBasicProvider;
import yeonleaf.plantodo.provider.JwtProvider;

import javax.crypto.SecretKey;

@Configuration
public class JwtConfig {

    @Bean
    public JwtBasicProvider jwtProvider() {
        return new JwtProvider();
    }

    @Bean
    public SecretKey jwtSecretKey() {
        return jwtProvider().secretKey();
    }

}
