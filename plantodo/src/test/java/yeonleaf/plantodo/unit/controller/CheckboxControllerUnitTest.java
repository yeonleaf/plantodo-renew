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
import java.util.ArrayList;
import java.util.List;

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
        when(checkboxService.save(any())).thenReturn(new CheckboxResDto(1L, "checkbox", LocalDate.of(2023, 7, 18), false));
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

        when(checkboxService.one(any())).thenReturn(new CheckboxResDto(1L, "checkbox", LocalDate.of(2023, 7, 18), false));

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

        when(checkboxService.update(any())).thenReturn(new CheckboxResDto(1L, "updatedTitle", LocalDate.of(2023, 7, 18), false));

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

        doReturn(new CheckboxResDto(1L, "checkbox", LocalDate.of(2023, 7, 18), true)).when(checkboxService).change(any());

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

    private List<CheckboxResDto> makeSampleCheckboxes() {
        List<CheckboxResDto> checkboxes = new ArrayList<>();
        checkboxes.add(new CheckboxResDto(1L, "checkbox", LocalDate.of(2023, 7, 18), false));
        checkboxes.add(new CheckboxResDto(1L, "checkbox", LocalDate.of(2023, 7, 18), false));
        checkboxes.add(new CheckboxResDto(1L, "checkbox", LocalDate.of(2023, 7, 18), false));
        return checkboxes;
    }

    @Test
    @DisplayName("정상 순수 컬렉션 조회 - by plan")
    void allTestNormal_byPlan() throws Exception {

        List<CheckboxResDto> checkboxes = makeSampleCheckboxes();

        when(checkboxService.allByPlan(any())).thenReturn(checkboxes);

        MockHttpServletRequestBuilder request = get("/checkboxes")
                .param("standard", "plan")
                .param("standardId", "1");

        mockMvc.perform(request)
                .andExpect(status().isOk())
                .andExpect(jsonPath("_embedded.checkboxResDtoList.length()").value(3));

    }

    @Test
    @DisplayName("정상 순수 컬렉션 조회 - by group")
    void allTestNormal_byGroup() throws Exception {

        List<CheckboxResDto> checkboxes = makeSampleCheckboxes();

        when(checkboxService.allByGroup(any())).thenReturn(checkboxes);

        MockHttpServletRequestBuilder request = get("/checkboxes")
                .param("standard", "group")
                        .param("standardId", "1");

        mockMvc.perform(request)
                .andExpect(status().isOk())
                .andExpect(jsonPath("_embedded.checkboxResDtoList.length()").value(3));

    }

    @Test
    @DisplayName("비정상 순수 컬렉션 조회 - by plan - Resource not found")
    void allTestAbnormal_byPlan_resourceNotFound() throws Exception {

        doThrow(ResourceNotFoundException.class).when(checkboxService).allByPlan(any());

        MockHttpServletRequestBuilder request = get("/checkboxes")
                .param("standard", "plan")
                .param("standardId", "1");

        mockMvc.perform(request)
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("message").value("Resource not found"));

    }

    @Test
    @DisplayName("비정상 순수 컬렉션 조회 - by group - Resource not found")
    void allTestAbnormal_byGroup_resourceNotFound() throws Exception {

        doThrow(ResourceNotFoundException.class).when(checkboxService).allByGroup(any());

        MockHttpServletRequestBuilder request = get("/checkboxes")
                .param("standard", "group")
                .param("standardId", "1");

        mockMvc.perform(request)
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("message").value("Resource not found"));

    }

    @Test
    @DisplayName("정상 일별 컬렉션 조회 - by group")
    void collectionFilteredByDateTestNormal_byGroup() throws Exception {

        List<CheckboxResDto> checkboxes = makeSampleCheckboxes();

        MockHttpServletRequestBuilder request = get("/checkboxes")
                .param("standard", "group")
                .param("standardId", "1")
                .param("dateKey", LocalDate.of(2023, 7, 31).toString());

        when(checkboxService.allByGroup(any(), any())).thenReturn(checkboxes);

        mockMvc.perform(request)
                .andExpect(status().isOk())
                .andExpect(jsonPath("_embedded.checkboxResDtoList.length()").value(3));

    }

    @Test
    @DisplayName("정상 일별 컬렉션 조회 - by group - Resource not found")
    void collectionFilteredByDateTestAbnormal_byGroup() throws Exception {

        MockHttpServletRequestBuilder request = get("/checkboxes")
                .param("standard", "group")
                .param("standardId", String.valueOf(Long.MAX_VALUE))
                .param("dateKey", LocalDate.of(2023, 7, 31).toString());

        doThrow(ResourceNotFoundException.class).when(checkboxService).allByGroup(any(), any());

        mockMvc.perform(request)
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("message").value("Resource not found"));

    }

    @Test
    @DisplayName("정상 일별 컬렉션 조회 - by plan")
    void collectionFilteredByDateTestNormal_byPlan() throws Exception {

        List<CheckboxResDto> checkboxes = makeSampleCheckboxes();

        MockHttpServletRequestBuilder request = get("/checkboxes")
                .param("standard", "plan")
                .param("standardId", "1")
                .param("dateKey", LocalDate.of(2023, 7, 31).toString());

        when(checkboxService.allByPlan(any(), any())).thenReturn(checkboxes);

        mockMvc.perform(request)
                .andExpect(status().isOk())
                .andExpect(jsonPath("_embedded.checkboxResDtoList.length()").value(3));

    }

    @Test
    @DisplayName("비정상 일별 컬렉션 조회 - by plan - Resource not found")
    void collectionFilteredByDateTestAbnormal_byPlan() throws Exception {

        MockHttpServletRequestBuilder request = get("/checkboxes")
                .param("standard", "plan")
                .param("standardId", String.valueOf(Long.MAX_VALUE))
                .param("dateKey", LocalDate.of(2023, 7, 31).toString());

        doThrow(ResourceNotFoundException.class).when(checkboxService).allByPlan(any(), any());

        mockMvc.perform(request)
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("message").value("Resource not found"));

    }

    @Test
    @DisplayName("비정상 일별 컬렉션 조회 - invalid requestParam")
    void collectionFilteredByDateTestAbnormal_invalidRequestParam() throws Exception {

        MockHttpServletRequestBuilder request = get("/checkboxes")
                .param("standard", "gorilla")
                .param("standardId", "1")
                .param("dateKey", LocalDate.of(2023, 7, 31).toString());

        mockMvc.perform(request)
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("errors.standard").exists());

    }

    @Test
    @DisplayName("정상 기간 컬렉션 조회 - by group")
    void collectionFilteredByDateRangeTest_byGroup() throws Exception {

        List<CheckboxResDto> checkboxes = makeSampleCheckboxes();

        when(checkboxService.allByGroup(any(), any(), any())).thenReturn(checkboxes);

        MockHttpServletRequestBuilder request = get("/checkboxes")
                .param("standard", "group")
                .param("standardId", "1")
                .param("searchStart", LocalDate.of(2023, 7, 19).toString())
                .param("searchEnd", LocalDate.of(2023, 7, 23).toString());

        mockMvc.perform(request)
                .andExpect(status().isOk())
                .andExpect(jsonPath("_embedded.checkboxResDtoList.length()").value(3));

    }

    @Test
    @DisplayName("비정상 기간 컬렉션 조회 - by group - Invalid query string")
    void collectionFilteredByDateRangeTest_byGroup_invalidQueryString_1() throws Exception {

        MockHttpServletRequestBuilder request = get("/checkboxes")
                .param("standard", "group")
                .param("standardId", "1")
                .param("searchStart", LocalDate.of(2023, 7, 19).toString())
                .param("searchEnd", LocalDate.of(2023, 7, 16).toString());

        mockMvc.perform(request)
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("errors.searchStart").exists())
                .andExpect(jsonPath("errors.searchEnd").exists());

    }

    @Test
    @DisplayName("비정상 기간 컬렉션 조회 - by group - Resource not found")
    void collectionFilteredByDateRangeTest_byGroup_resourceNotFound() throws Exception {

        doThrow(ResourceNotFoundException.class).when(checkboxService).allByGroup(any(), any(), any());
        MockHttpServletRequestBuilder request = get("/checkboxes")
                .param("standard", "group")
                .param("standardId", "1")
                .param("searchStart", LocalDate.of(2023, 7, 16).toString())
                .param("searchEnd", LocalDate.of(2023, 7, 19).toString());
        mockMvc.perform(request)
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("message").value("Resource not found"));

    }

    @Test
    @DisplayName("정상 기간 컬렉션 조회 - by plan")
    void collectionFilteredByDateRangeTest_byPlan() throws Exception {

        List<CheckboxResDto> checkboxes = makeSampleCheckboxes();

        when(checkboxService.allByPlan(any(), any(), any())).thenReturn(checkboxes);

        MockHttpServletRequestBuilder request = get("/checkboxes")
                .param("standard", "plan")
                .param("standardId", "1")
                .param("searchStart", LocalDate.of(2023, 7, 19).toString())
                .param("searchEnd", LocalDate.of(2023, 7, 23).toString());

        mockMvc.perform(request)
                .andExpect(status().isOk())
                .andExpect(jsonPath("_embedded.checkboxResDtoList.length()").value(3));

    }

    @Test
    @DisplayName("비정상 기간 컬렉션 조회 - by plan - Invalid query string")
    void collectionFilteredByDateRangeTestAbnormal_byPlan_invalidQueryString() throws Exception {

        MockHttpServletRequestBuilder request = get("/checkboxes")
                .param("standard", "plan")
                .param("standardId", "1")
                .param("searchStart", LocalDate.of(2023, 7, 19).toString())
                .param("searchEnd", LocalDate.of(2023, 7, 16).toString());

        mockMvc.perform(request)
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("errors.searchStart").exists())
                .andExpect(jsonPath("errors.searchEnd").exists());

    }

    @Test
    @DisplayName("비정상 기간 컬렉션 조회 - by plan - Resource not found")
    void collectionFilteredByDateRangeTestAbnormal_byPlan_resourceNotFound() throws Exception {

        doThrow(ResourceNotFoundException.class).when(checkboxService).allByPlan(any(), any(), any());
        MockHttpServletRequestBuilder request = get("/checkboxes")
                .param("standard", "plan")
                .param("standardId", "1")
                .param("searchStart", LocalDate.of(2023, 7, 16).toString())
                .param("searchEnd", LocalDate.of(2023, 7, 19).toString());
        mockMvc.perform(request)
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("message").value("Resource not found"));

    }

    @Test
    @DisplayName("비정상 기간 컬렉션 조회 - Invalid query string")
    void collectionFilteredByDateRangeTest_invalidQueryString() throws Exception {

        MockHttpServletRequestBuilder request = get("/checkboxes")
                .param("standard", "gorilla")
                .param("standardId", "1")
                .param("searchStart", LocalDate.of(2023, 7, 16).toString())
                .param("searchEnd", LocalDate.of(2023, 7, 19).toString());

        mockMvc.perform(request)
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("errors.standard").exists());

    }


}
