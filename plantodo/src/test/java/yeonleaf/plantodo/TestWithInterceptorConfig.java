package yeonleaf.plantodo;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
import yeonleaf.plantodo.interceptor.LoginCheckInterceptor;
import yeonleaf.plantodo.provider.JwtBasicProvider;
import yeonleaf.plantodo.provider.JwtTestProvider;

import javax.crypto.SecretKey;

@TestConfiguration
public class TestWithInterceptorConfig {

    private ObjectMapper objectMapper;

    public TestWithInterceptorConfig() {
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

    @Bean
    public WebMvcConfigurer testWebMvcConfigurer() {
        return new WebMvcConfigurer() {
            @Override
            public void addInterceptors(InterceptorRegistry registry) {
                registry.addInterceptor(new LoginCheckInterceptor(jwtTestSecretKey(), objectMapper()))
                        .excludePathPatterns("/member", "/member/login");
            }
        };
    }

}
