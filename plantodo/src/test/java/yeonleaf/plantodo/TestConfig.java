package yeonleaf.plantodo;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import yeonleaf.plantodo.provider.JwtBasicProvider;
import yeonleaf.plantodo.provider.JwtTestProvider;

import javax.crypto.SecretKey;

@TestConfiguration
public class TestConfig {

    private ObjectMapper objectMapper;

    public TestConfig() {
        this.objectMapper = new ObjectMapper();
    }

    @Bean
    public ObjectMapper objectMapper() {
        return objectMapper;
    }

    @Bean
    public SecretKey jwtTestSecretKey() {
        return jwtTestProvider().secretKey();
    }

    @Bean
    public JwtTestProvider jwtTestProvider() {
        return new JwtTestProvider();
    }

}
