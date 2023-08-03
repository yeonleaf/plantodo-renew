package yeonleaf.plantodo.unit.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.persistence.PersistenceException;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;
import yeonleaf.plantodo.TestConfig;
import yeonleaf.plantodo.controller.PlanController;
import yeonleaf.plantodo.domain.PlanStatus;
import yeonleaf.plantodo.dto.PlanReqDto;
import yeonleaf.plantodo.dto.PlanResDto;
import yeonleaf.plantodo.dto.PlanUpdateReqDto;
import yeonleaf.plantodo.exceptions.ResourceNotFoundException;
import yeonleaf.plantodo.service.PlanService;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@Import({TestConfig.class})
@WebMvcTest(PlanController.class)
public class PlanControllerUnitTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private PlanService planService;

    @Test
    @DisplayName("정상 등록 - plan만")
    void saveTestNormal() throws Exception {

        LocalDate start = LocalDate.now();
        LocalDate end = start.plusDays(3);

        PlanReqDto planReqDto = new PlanReqDto("title", start, end, 1L);
        String requestData = objectMapper.writeValueAsString(planReqDto);

        when(planService.save(any())).thenReturn(new PlanResDto(1L, "title", start, end, PlanStatus.NOW));

        MockHttpServletRequestBuilder request = post("/plan")
                .header("Authorization", "")
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestData);

        mockMvc.perform(request)
                .andExpect(status().isCreated())
                .andExpect(jsonPath("id").value(1L))
                .andExpect(jsonPath("_links.self").exists());

    }

    @Test
    @DisplayName("비정상 등록 - plan만 - 파라미터 null")
    void saveTestAbnormalNullParameters() throws Exception {

        PlanReqDto planReqDto = new PlanReqDto(null, null, null, 1L);
        String requestData = objectMapper.writeValueAsString(planReqDto);

        MockHttpServletRequestBuilder request = post("/plan")
                .header("Authorization", "")
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestData);

        mockMvc.perform(request)
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("message").value("입력값 타입/내용 오류"));

    }

    @Test
    @DisplayName("비정상 등록 - plan만 - format validation - start가 오늘 이전")
    void saveTestAbnormalFormatValidationStart() throws Exception {

        LocalDate start = LocalDate.now().minusDays(3);
        LocalDate end = start.plusDays(4);

        PlanReqDto planReqDto = new PlanReqDto("title", start, end, 1L);
        String requestData = objectMapper.writeValueAsString(planReqDto);

        MockHttpServletRequestBuilder request = post("/plan")
                .header("Authorization", "")
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestData);

        mockMvc.perform(request)
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("message").value("입력값 타입/내용 오류"))
                .andExpect(jsonPath("errors.start").isNotEmpty());

    }

    @Test
    @DisplayName("비정상 등록 - plan만 - format validation - end가 start 이전")
    void saveTestAbnormalFormatValidationEnd() throws Exception {

        LocalDate start = LocalDate.now().plusDays(3);
        LocalDate end = start.minusDays(2);

        PlanReqDto planReqDto = new PlanReqDto("title", start, end, 1L);
        String requestData = objectMapper.writeValueAsString(planReqDto);

        MockHttpServletRequestBuilder request = post("/plan")
                .header("Authorization", "")
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestData);

        mockMvc.perform(request)
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("message").value("입력값 형식 오류"))
                .andExpect(jsonPath("errors.end").isNotEmpty());

    }

    @Test
    @DisplayName("비정상 등록 - plan만 - resource not found (member)")
    void saveTestAbnormalMemberResourceNotFound() throws Exception {

        LocalDate start = LocalDate.now();
        LocalDate end = start.plusDays(3);

        PlanReqDto planReqDto = new PlanReqDto("title", start, end, 1L);
        String requestData = objectMapper.writeValueAsString(planReqDto);

        when(planService.save(any())).thenThrow(new ResourceNotFoundException());

        MockHttpServletRequestBuilder request = post("/plan")
                .header("Authorization", "")
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestData);

        mockMvc.perform(request)
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("message").value("Resource not found"));

    }

    @Test
    @DisplayName("비정상 등록 - plan만 - persistence problem")
    void saveAbnormalPersistenceProblem() throws Exception {

        LocalDate start = LocalDate.now();
        LocalDate end = start.plusDays(3);

        PlanReqDto planReqDto = new PlanReqDto("title", start, end, 1L);
        String requestData = objectMapper.writeValueAsString(planReqDto);

        when(planService.save(any())).thenThrow(PersistenceException.class);

        MockHttpServletRequestBuilder request = post("/plan")
                .header("Authorization", "")
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestData);

        mockMvc.perform(request)
                .andExpect(status().is5xxServerError())
                .andExpect(jsonPath("message").value("Possible server error"));

    }

    @Test
    @DisplayName("정상 단건 조회")
    void getOneTestNormal() throws Exception {

        MockHttpServletRequestBuilder request = get("/plan/1")
                .header("Authorization", "");

        when(planService.one(any())).thenReturn(new PlanResDto(1L, "title", LocalDate.now(), LocalDate.now().plusDays(3), PlanStatus.NOW));

        mockMvc.perform(request)
                .andExpect(status().isOk())
                .andExpect(jsonPath("id").value(1L))
                .andExpect(jsonPath("_links.self").exists())
                .andExpect(jsonPath("_links.groups").exists());

    }

    @Test
    @DisplayName("비정상 단건 조회")
    void getOneTestAbnormal() throws Exception {

        MockHttpServletRequestBuilder request = get("/plan/1")
                .header("Authorization", "");

        when(planService.one(any())).thenThrow(new ResourceNotFoundException());

        mockMvc.perform(request)
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("message").value("Resource not found"));

    }

    @Test
    @DisplayName("정상 수정")
    void updateTestNormal() throws Exception {

        PlanUpdateReqDto planUpdateReqDto = new PlanUpdateReqDto(1L, "revisedTitle", LocalDate.now(), LocalDate.now().plusDays(3));
        when(planService.update(any())).thenReturn(new PlanResDto(1L, "revisedTitle", LocalDate.now(), LocalDate.now().plusDays(3), PlanStatus.NOW));
        MockHttpServletRequestBuilder request = put("/plan")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(planUpdateReqDto));
        mockMvc.perform(request)
                .andExpect(status().isOk());

    }

    @Test
    @DisplayName("비정상 수정 - Resource not found")
    void updateTestAbnormal_resourceNotFound() throws Exception {

        PlanUpdateReqDto planUpdateReqDto = new PlanUpdateReqDto(1L, "revisedTitle", LocalDate.now(), LocalDate.now().plusDays(3));
        doThrow(ResourceNotFoundException.class).when(planService).update(any());
        MockHttpServletRequestBuilder request = put("/plan")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(planUpdateReqDto));
        mockMvc.perform(request)
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("message").value("Resource not found"));

    }

    @Test
    @DisplayName("비정상 수정 - ArgumentResolver Validation")
    void updateTestAbnormal_argumentResolverValidation() throws Exception {

        PlanUpdateReqDto planUpdateReqDto = new PlanUpdateReqDto(1L, null, LocalDate.now(), LocalDate.now().plusDays(3));
        doThrow(ResourceNotFoundException.class).when(planService).update(any());
        MockHttpServletRequestBuilder request = put("/plan")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(planUpdateReqDto));
        mockMvc.perform(request)
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("message").value("입력값 타입/내용 오류"));

    }

    @Test
    @DisplayName("정상 삭제")
    void deleteTestNormal() throws Exception {

        MockHttpServletRequestBuilder request = delete("/plan/1");

        doNothing().when(planService).delete(any());

        mockMvc.perform(request)
                .andExpect(status().isNoContent());

    }

    @Test
    @DisplayName("비정상 삭제")
    void deleteTestAbnormal() throws Exception {

        MockHttpServletRequestBuilder request = delete("/plan/1");

        doThrow(ResourceNotFoundException.class).when(planService).delete(any());

        mockMvc.perform(request)
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("message").value("Resource not found"));

    }

    @Test
    @DisplayName("정상 상태 변경")
    void changeStatusNormal() throws Exception {

        MockHttpServletRequestBuilder request = patch("/plan/1");

        when(planService.change(any())).thenReturn(new PlanResDto(1L, "plan", LocalDate.now(), LocalDate.now().plusDays(3), PlanStatus.COMPLETED));

        mockMvc.perform(request)
                .andExpect(status().isOk())
                .andExpect(jsonPath("status").value("COMPLETED"));

    }

    @Test
    @DisplayName("비정상 상태 변경 - Resource not found")
    void changeStatusAbnormal_resourceNotFound() throws Exception {

        MockHttpServletRequestBuilder request = patch("/plan/1");

        doThrow(ResourceNotFoundException.class).when(planService).change(any());

        mockMvc.perform(request)
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("message").value("Resource not found"));

    }

    private List<PlanResDto> makeSamplePlans() {
        List<PlanResDto> plans = new ArrayList<>();
        plans.add(new PlanResDto(1L, "title1", LocalDate.of(2023, 7, 18), LocalDate.of(2023, 7, 20), PlanStatus.PAST));
        plans.add(new PlanResDto(2L, "title2", LocalDate.of(2023, 7, 18), LocalDate.of(2023, 7, 20), PlanStatus.PAST));
        plans.add(new PlanResDto(3L, "title3", LocalDate.of(2023, 7, 18), LocalDate.of(2023, 7, 20), PlanStatus.PAST));
        return plans;
    }

    @Test
    @DisplayName("정상 순수 컬렉션 조회")
    void allTestNormal() throws Exception {

        List<PlanResDto> plans = makeSamplePlans();

        when(planService.all(any())).thenReturn(plans);

        MockHttpServletRequestBuilder request = get("/plans")
                .param("memberId", "1");

        mockMvc.perform(request)
                .andExpect(status().isOk())
                .andExpect(jsonPath("_embedded.planResDtoList.length()").value(3));

    }

    @Test
    @DisplayName("비정상 순수 컬렉션 조회")
    void allTestAbnormal() throws Exception {

        doThrow(ResourceNotFoundException.class).when(planService).all(any());

        MockHttpServletRequestBuilder request = get("/plans")
                .param("memberId", "1");

        mockMvc.perform(request)
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("message").value("Resource not found"));

    }

    @Test
    @DisplayName("정상 일별 컬렉션 조회")
    void collectionFilteredByDateTestNormal() throws Exception {

        List<PlanResDto> plans = makeSamplePlans();

        MockHttpServletRequestBuilder request = get("/plans/date")
                .param("memberId", "1")
                .param("dateKey", LocalDate.of(2023, 7, 19).toString());

        doReturn(plans).when(planService).all(any(), any());

        mockMvc.perform(request)
                .andExpect(status().isOk())
                .andExpect(jsonPath("_embedded.planResDtoList.length()").value(3));

    }

    @Test
    @DisplayName("비정상 일별 컬렉션 조회 - Resource not found")
    void collectionFilteredByDateTestAbnormal() throws Exception {

        MockHttpServletRequestBuilder request = get("/plans/date")
                .param("memberId", String.valueOf(Long.MAX_VALUE))
                .param("dateKey", LocalDate.of(2023, 7, 19).toString());

        doThrow(ResourceNotFoundException.class).when(planService).all(any(), any());

        mockMvc.perform(request)
                .andExpect(jsonPath("message").value("Resource not found"));

    }

    @Test
    @DisplayName("정상 기간 컬렉션 조회")
    void collectionFilteredByDateRangeTestNormal() throws Exception {

        List<PlanResDto> plans = makeSamplePlans();

        when(planService.all(any(), any(), any())).thenReturn(plans);

        MockHttpServletRequestBuilder request = get("/plans/range")
                .param("memberId", "1")
                .param("searchStart", LocalDate.of(2023, 7, 19).toString())
                .param("searchEnd", LocalDate.of(2023, 7, 25).toString());

        mockMvc.perform(request)
                .andExpect(status().isOk())
                .andExpect(jsonPath("_embedded.planResDtoList.length()").value(3));

    }

    @Test
    @DisplayName("비정상 기간 컬렉션 조회 - invalid query string")
    void collectionFilteredByDateRangeTestAbnormal_invalidQueryString() throws Exception {

        MockHttpServletRequestBuilder request = get("/plans/range")
                .param("memberId", "1")
                .param("searchStart", LocalDate.of(2023, 7, 19).toString())
                .param("searchEnd", LocalDate.of(2023, 7, 16).toString());
        mockMvc.perform(request)
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("errors.searchStart").exists())
                .andExpect(jsonPath("errors.searchEnd").exists());

    }

    @Test
    @DisplayName("비정상 기간 컬렉션 조회 - Resource not found")
    void collectionFilteredByDateRangeTestAbnormal_resourceNotFound() throws Exception {

        doThrow(ResourceNotFoundException.class).when(planService).all(any(), any(), any());

        MockHttpServletRequestBuilder request = get("/plans/range")
                .param("memberId", "1")
                .param("searchStart", LocalDate.of(2023, 7, 19).toString())
                .param("searchEnd", LocalDate.of(2023, 7, 25).toString());

        mockMvc.perform(request)
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("message").value("Resource not found"));

    }

}
