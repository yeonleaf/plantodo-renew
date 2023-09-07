package yeonleaf.plantodo;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.flyway.FlywayMigrationStrategy;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class FlywayConfig {

    @Value("${flyway.enabled}")
    private boolean flywayEnabled;

    @Bean
    public FlywayMigrationStrategy repairMigrationStrategy() {
        return flyway -> {
            if (flywayEnabled) {
                flyway.repair();
                flyway.migrate();
            }
        };
    }

}
