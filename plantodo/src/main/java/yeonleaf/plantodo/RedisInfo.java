package yeonleaf.plantodo;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@Configuration
@ConfigurationProperties(prefix = "spring.data.redis.cluster")
public class RedisInfo {

    private int maxRedirects;
    private List<String> nodes;

}
