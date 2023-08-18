package yeonleaf.plantodo;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
import yeonleaf.plantodo.interceptor.LoginCheckInterceptor;

import javax.crypto.SecretKey;

@Configuration
@RequiredArgsConstructor
public class WebConfig {

    private final SecretKey jwtSecretKey;
    private final ObjectMapper objectMapper;

    @Bean
    public WebMvcConfigurer webMvcConfigurer() {
        return new WebMvcConfigurer() {
            @Override
            public void addInterceptors(InterceptorRegistry registry) {
                registry.addInterceptor(new LoginCheckInterceptor(jwtSecretKey, objectMapper))
                        .addPathPatterns("/plan", "/plan/*", "/checkbox", "/checkbox/*", "/group", "/group/*",
                                "/plans", "/plans/*", "/groups", "/groups/*", "/checkboxes", "/checkboxes/*",
                                "/calendar", "/calendar/*");
            }
        };
    }

}
