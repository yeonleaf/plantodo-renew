package yeonleaf.plantodo.unit.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.hateoas.MediaTypes;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;
import yeonleaf.plantodo.TestConfig;
import yeonleaf.plantodo.controller.CheckboxController;
import yeonleaf.plantodo.dto.CheckboxReqDto;
import yeonleaf.plantodo.dto.CheckboxResDto;
import yeonleaf.plantodo.exceptions.ResourceNotFoundException;
import yeonleaf.plantodo.service.CheckboxServiceTestImpl;

import java.time.LocalDate;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@Import({TestConfig.class})
@WebMvcTest(CheckboxController.class)
public class CheckboxControllerUnitTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private CheckboxServiceTestImpl checkboxService;

    @Test
    @DisplayName("정상 등록")
    void saveTestNormal() throws Exception {

        CheckboxReqDto checkboxReqDto = new CheckboxReqDto("checkbox", 1L, LocalDate.now());
        when(checkboxService.save(any())).thenReturn(new CheckboxResDto(1L, "checkbox", false));
        MockHttpServletRequestBuilder request = post("/checkbox")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(checkboxReqDto));
        mockMvc.perform(request)
                .andExpect(status().isCreated())
                .andExpect(jsonPath("id").value(1L))
                .andExpect(jsonPath("_links").exists());

    }

    @Test
    @DisplayName("비정상 등록 - Argument Resolver Validation")
    void saveTestAbnormal_ArgumentResolverValidation() throws Exception {

        CheckboxReqDto checkboxReqDto = new CheckboxReqDto("checkbox", null, LocalDate.now());
        MockHttpServletRequestBuilder request = post("/checkbox")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(checkboxReqDto));
        mockMvc.perform(request)
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("message").value("입력값 타입/내용 오류"));

    }

    @Test
    @DisplayName("비정상 등록 - Resource not found")
    void saveTestAbnormal_ResourceNotFound_Plan() throws Exception {

        CheckboxReqDto checkboxReqDto = new CheckboxReqDto("checkbox", 1L, LocalDate.now());

        when(checkboxService.save(any())).thenThrow(new ResourceNotFoundException());

        MockHttpServletRequestBuilder request = post("/checkbox")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(checkboxReqDto));

        mockMvc.perform(request)
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("message").value("Resource not found"));

    }

}
