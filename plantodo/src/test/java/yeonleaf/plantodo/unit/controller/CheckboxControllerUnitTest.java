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
import yeonleaf.plantodo.dto.CheckboxUpdateReqDto;
import yeonleaf.plantodo.exceptions.ResourceNotFoundException;
import yeonleaf.plantodo.service.CheckboxServiceTestImpl;

import java.time.LocalDate;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
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

    @Test
    @DisplayName("단건 정상 조회")
    void oneTestNormal() throws Exception {

        when(checkboxService.one(any())).thenReturn(new CheckboxResDto(1L, "checkbox", false));

        MockHttpServletRequestBuilder request = get("/checkbox/1");

        mockMvc.perform(request)
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaTypes.HAL_JSON))
                .andExpect(jsonPath("id").isNumber());

    }

    @Test
    @DisplayName("단건 비정상 조회")
    void oneTestAbnormal() throws Exception {

        when(checkboxService.one(any())).thenThrow(ResourceNotFoundException.class);

        MockHttpServletRequestBuilder request = get("/checkbox/1");

        mockMvc.perform(request)
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("message").value("Resource not found"));

    }

    @Test
    @DisplayName("정상 수정")
    void updateTestNormal() throws Exception {

        CheckboxUpdateReqDto checkboxUpdateReqDto = new CheckboxUpdateReqDto(1L, "updatedTitle");
        MockHttpServletRequestBuilder request = put("/checkbox")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(checkboxUpdateReqDto));

        when(checkboxService.update(any())).thenReturn(new CheckboxResDto(1L, "updatedTitle", false));

        mockMvc.perform(request)
                .andExpect(status().isOk())
                .andExpect(jsonPath("title").value("updatedTitle"));

    }

    @Test
    @DisplayName("비정상 수정 - ArgumentResolver Validation")
    void updateTestAbnormal_argumentResolverValidation() throws Exception {

        CheckboxUpdateReqDto checkboxUpdateReqDto = new CheckboxUpdateReqDto(null, "updatedTitle");

        MockHttpServletRequestBuilder request = put("/checkbox")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(checkboxUpdateReqDto));

        mockMvc.perform(request)
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("errors.id").exists())
                .andDo(print());

    }

    @Test
    @DisplayName("비정상 수정 - Resource not found")
    void updateTestAbnormal_resourceNotFound() throws Exception {

        CheckboxUpdateReqDto checkboxUpdateReqDto = new CheckboxUpdateReqDto(Long.MAX_VALUE, "updatedTitle");
        MockHttpServletRequestBuilder request = put("/checkbox")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(checkboxUpdateReqDto));

        when(checkboxService.update(any())).thenThrow(ResourceNotFoundException.class);

        mockMvc.perform(request)
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("message").value("Resource not found"));

    }

    @Test
    @DisplayName("정상 삭제")
    void deleteTestNormal() throws Exception {

        MockHttpServletRequestBuilder request = delete("/checkbox/1");

        doNothing().when(checkboxService).delete(any());

        mockMvc.perform(request)
                .andExpect(status().isNoContent());

    }

    @Test
    @DisplayName("비정상 삭제")
    void deleteTestAbnormal() throws Exception {

        MockHttpServletRequestBuilder request = delete("/checkbox/1");

        doThrow(ResourceNotFoundException.class).when(checkboxService).delete(any());

        mockMvc.perform(request)
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("message").value("Resource not found"));

    }

    @Test
    @DisplayName("정상 상태 변경")
    void changeStatusTestNormal() throws Exception {

        MockHttpServletRequestBuilder request = patch("/checkbox/1");

        doReturn(new CheckboxResDto(1L, "title", true)).when(checkboxService).change(any());

        mockMvc.perform(request)
                .andExpect(status().isOk())
                .andExpect(jsonPath("checked").value("true"));

    }

    @Test
    @DisplayName("비정상 상태 변경 - Resource not found")
    void changeStatusTestAbnormal() throws Exception {

        MockHttpServletRequestBuilder request = patch("/checkbox/1");

        doThrow(ResourceNotFoundException.class).when(checkboxService).change(any());

        mockMvc.perform(request)
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("message").value("Resource not found"));

    }

}
