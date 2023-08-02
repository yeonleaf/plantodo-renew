package yeonleaf.plantodo;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import yeonleaf.plantodo.converter.RepInToOutConverter;
import yeonleaf.plantodo.converter.RepOutToInConverter;
import yeonleaf.plantodo.util.DateRange;
import yeonleaf.plantodo.util.PlanDateRangeRevisionMaker;

@Configuration
public class UtilConfig {

    @Bean
    public ObjectMapper objectMapper() {
        return new ObjectMapper();
    }

    @Bean
    public RepInToOutConverter repInToOutConverter() {
        return new RepInToOutConverter();
    }

    @Bean
    public RepOutToInConverter repOutToInConverter() {
        return new RepOutToInConverter();
    }

    @Bean
    public PlanDateRangeRevisionMaker planDateRangeRevisionMaker() {
        return new PlanDateRangeRevisionMaker();
    }

    @Bean
    public DateRange dateRange() {
        return new DateRange();
    }

}
