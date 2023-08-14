package yeonleaf.plantodo;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import yeonleaf.plantodo.assembler.CheckboxModelAssembler;
import yeonleaf.plantodo.assembler.GroupModelAssembler;
import yeonleaf.plantodo.assembler.PlanModelAssembler;
import yeonleaf.plantodo.provider.JwtBasicProvider;
import yeonleaf.plantodo.provider.JwtTestProvider;
import yeonleaf.plantodo.repository.*;
import yeonleaf.plantodo.service.GroupServiceTestImpl;
import yeonleaf.plantodo.service.MemberServiceTestImpl;
import yeonleaf.plantodo.validator.RepInputValidator;

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

    @Bean
    public PlanModelAssembler planModelAssembler() {
        return new PlanModelAssembler();
    }

    @Bean
    public GroupModelAssembler groupModelAssembler() {
        return new GroupModelAssembler();
    }

    @Bean
    public CheckboxModelAssembler checkboxModelAssembler() {
        return new CheckboxModelAssembler();
    }

}
