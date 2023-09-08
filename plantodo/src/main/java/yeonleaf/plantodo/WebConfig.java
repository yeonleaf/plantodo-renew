package yeonleaf.plantodo;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
import yeonleaf.plantodo.interceptor.LoginCheckInterceptor;

import javax.crypto.SecretKey;

@Configuration
public class WebConfig {

    private SecretKey jwtSecretKey;
    private ObjectMapper objectMapper;

    @Autowired
    public WebConfig(SecretKey jwtSecretKey, ObjectMapper objectMapper) {
        this.jwtSecretKey = jwtSecretKey;
        this.objectMapper = objectMapper;
    }

    @Bean
    public WebMvcConfigurer webMvcConfigurer() {
        return new WebMvcConfigurer() {
            @Override
            public void addInterceptors(InterceptorRegistry registry) {
                registry.addInterceptor(new LoginCheckInterceptor(jwtSecretKey, objectMapper))
                        .addPathPatterns("/plan", "/plan/*", "/checkbox", "/checkbox/*", "/group", "/group/*",
                                "/plans", "/plans/*", "/groups", "/groups/*", "/checkboxes", "/checkboxes/*", "/checkboxes/*/*",
                                "/calendar", "/calendar/*");
            }
        };
    }

}
