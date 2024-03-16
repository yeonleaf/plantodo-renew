package yeonleaf.plantodo;

import com.zaxxer.hikari.HikariDataSource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.jdbc.DataSourceBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.context.annotation.Profile;
import org.springframework.jdbc.datasource.LazyConnectionDataSourceProxy;


import javax.sql.DataSource;
import java.util.concurrent.ConcurrentHashMap;

@Slf4j
@Profile("con")
@Configuration
public class DataSourceConfiguration {

    @Bean("master")
    @ConfigurationProperties(prefix = "spring.datasource.master.hikari")
    public DataSource master() {
        return DataSourceBuilder.create().type(HikariDataSource.class).build();
    }

    @Bean("slave")
    @ConfigurationProperties(prefix = "spring.datasource.slave.hikari")
    public DataSource slave() {
        return DataSourceBuilder.create().type(HikariDataSource.class).build();
    }

    @Bean("routingDataSource")
    public DataSource routingDataSource(
        @Qualifier("master") DataSource master,
        @Qualifier("slave") DataSource slave) {
        RoutingDataSource routingDataSource = new RoutingDataSource();
        ConcurrentHashMap<Object, Object> dataSourceMap = new ConcurrentHashMap<>();
        dataSourceMap.put("master", master);
        dataSourceMap.put("slave", slave);
        routingDataSource.setTargetDataSources(dataSourceMap);
        routingDataSource.setDefaultTargetDataSource(master);
        return routingDataSource;

    }

    @Primary
    @Bean("dataSource")
    public DataSource dataSource(@Qualifier("routingDataSource") DataSource routingDataSource) {
        return new LazyConnectionDataSourceProxy(routingDataSource);
    }

}
