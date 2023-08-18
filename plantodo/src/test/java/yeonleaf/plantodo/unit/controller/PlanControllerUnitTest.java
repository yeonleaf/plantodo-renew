package yeonleaf.plantodo.unit.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.persistence.PersistenceException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;
import org.springframework.validation.BindingResult;
import yeonleaf.plantodo.TestConfig;
import yeonleaf.plantodo.controller.PlanController;
import yeonleaf.plantodo.domain.PlanStatus;
import yeonleaf.plantodo.dto.PlanReqDto;
import yeonleaf.plantodo.dto.PlanResDto;
import yeonleaf.plantodo.dto.PlanUpdateReqDto;
import yeonleaf.plantodo.exceptions.ResourceNotFoundException;
import yeonleaf.plantodo.service.PlanService;
import yeonleaf.plantodo.exceptions.ApiBindingError;
import yeonleaf.plantodo.exceptions.ApiSimpleError;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * target : {@link PlanController}에 있는 모든 메소드
 * target description : 일정을 생성, 조회, 수정, 상태 변경, 삭제하는 API
 *
 * test description : 삭제 API를 제외하고 정상적으로 수행한 경우 {@link PlanResDto}나 List<PlanResDto>를 리턴하는지 확인한다.
 *                    query string이나 argument validation에 통과하지 못한 경우 {@link ApiBindingError}를 리턴하는지 확인한다.
 *                    대상이 존재하지 않을 경우 {@link ApiSimpleError}를 리턴하는지 확인한다.
 */
@Import({TestConfig.class})
@WebMvcTest(PlanController.class)
public class PlanControllerUnitTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private PlanService planService;


    /**
     * 등록 API 관련 테스트
     * @see PlanController#save(PlanReqDto, BindingResult)
     * @throws Exception mockMvc.perform()
     */
    @Test
    @DisplayName("정상 등록 - plan만")
    void saveTestNormal() throws Exception {

        // given
        LocalDate start = LocalDate.now();
        LocalDate end = start.plusDays(3);

        PlanReqDto planReqDto = new PlanReqDto("title", start, end, 1L);
        String requestData = objectMapper.writeValueAsString(planReqDto);

        MockHttpServletRequestBuilder request = post("/plan")
                .header("Authorization", "")
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestData);

        // when
        when(planService.save(any())).thenReturn(new PlanResDto(1L, "title", start, end, PlanStatus.NOW));


        // then
        mockMvc.perform(request)
                .andExpect(status().isCreated())
                .andExpect(jsonPath("id").value(1L))
                .andExpect(jsonPath("_links.self").exists());

    }

    @Test
    @DisplayName("비정상 등록 - plan만 - 파라미터 null")
    void saveTestAbnormalNullParameters() throws Exception {

        // given
        PlanReqDto planReqDto = new PlanReqDto(null, null, null, 1L);
        String requestData = objectMapper.writeValueAsString(planReqDto);

        MockHttpServletRequestBuilder request = post("/plan")
                .header("Authorization", "")
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestData);

        // when - then
        mockMvc.perform(request)
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("message").value("입력값 타입/내용 오류"));

    }

    @Test
    @DisplayName("비정상 등록 - plan만 - format validation - start가 오늘 이전")
    void saveTestAbnormalFormatValidationStart() throws Exception {

        // given
        LocalDate start = LocalDate.now().minusDays(3);
        LocalDate end = start.plusDays(4);

        PlanReqDto planReqDto = new PlanReqDto("title", start, end, 1L);
        String requestData = objectMapper.writeValueAsString(planReqDto);

        MockHttpServletRequestBuilder request = post("/plan")
                .header("Authorization", "")
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestData);

        // when - then
        mockMvc.perform(request)
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("message").value("입력값 타입/내용 오류"))
                .andExpect(jsonPath("errors.start").isNotEmpty());

    }

    @Test
    @DisplayName("비정상 등록 - plan만 - format validation - end가 start 이전")
    void saveTestAbnormalFormatValidationEnd() throws Exception {

        // given
        LocalDate start = LocalDate.now().plusDays(3);
        LocalDate end = start.minusDays(2);

        PlanReqDto planReqDto = new PlanReqDto("title", start, end, 1L);
        String requestData = objectMapper.writeValueAsString(planReqDto);

        MockHttpServletRequestBuilder request = post("/plan")
                .header("Authorization", "")
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestData);

        // when - then
        mockMvc.perform(request)
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("message").value("입력값 형식 오류"))
                .andExpect(jsonPath("errors.end").isNotEmpty());

    }

    @Test
    @DisplayName("비정상 등록 - plan만 - resource not found (member)")
    void saveTestAbnormalMemberResourceNotFound() throws Exception {

        // given
        LocalDate start = LocalDate.now();
        LocalDate end = start.plusDays(3);

        PlanReqDto planReqDto = new PlanReqDto("title", start, end, 1L);
        String requestData = objectMapper.writeValueAsString(planReqDto);

        MockHttpServletRequestBuilder request = post("/plan")
                .header("Authorization", "")
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestData);

        // when
        when(planService.save(any())).thenThrow(new ResourceNotFoundException());

        // then
        mockMvc.perform(request)
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("message").value("Resource not found"));

    }

    @Test
    @DisplayName("비정상 등록 - plan만 - persistence problem")
    void saveAbnormalPersistenceProblem() throws Exception {

        // given
        LocalDate start = LocalDate.now();
        LocalDate end = start.plusDays(3);

        PlanReqDto planReqDto = new PlanReqDto("title", start, end, 1L);
        String requestData = objectMapper.writeValueAsString(planReqDto);

        MockHttpServletRequestBuilder request = post("/plan")
                .header("Authorization", "")
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestData);

        // when
        when(planService.save(any())).thenThrow(PersistenceException.class);

        // then
        mockMvc.perform(request)
                .andExpect(status().is5xxServerError())
                .andExpect(jsonPath("message").value("Possible server error"));

    }


    /**
     * 단건 조회 API 관련 테스트
     * @see PlanController#one(Long)
     * @throws Exception mockMvc.perform()
     */
    @Test
    @DisplayName("정상 단건 조회")
    void getOneTestNormal() throws Exception {

        // given
        MockHttpServletRequestBuilder request = get("/plan/1")
                .header("Authorization", "");

        // when
        when(planService.one(any())).thenReturn(new PlanResDto(1L, "title", LocalDate.now(), LocalDate.now().plusDays(3), PlanStatus.NOW));

        // then
        mockMvc.perform(request)
                .andExpect(status().isOk())
                .andExpect(jsonPath("id").value(1L))
                .andExpect(jsonPath("_links.self").exists())
                .andExpect(jsonPath("_links.groups").exists());

    }

    @Test
    @DisplayName("비정상 단건 조회")
    void getOneTestAbnormal() throws Exception {

        // given
        MockHttpServletRequestBuilder request = get("/plan/1")
                .header("Authorization", "");

        // when
        when(planService.one(any())).thenThrow(new ResourceNotFoundException());

        // then
        mockMvc.perform(request)
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("message").value("Resource not found"));

    }


    /**
     * 수정 API 관련 테스트
     * @see PlanController#update(PlanUpdateReqDto, BindingResult)
     * @throws Exception mockMvc.perform()
     */
    @Test
    @DisplayName("정상 수정")
    void updateTestNormal() throws Exception {

        // given
        PlanUpdateReqDto planUpdateReqDto = new PlanUpdateReqDto(1L, "revisedTitle", LocalDate.now(), LocalDate.now().plusDays(3));
        MockHttpServletRequestBuilder request = put("/plan")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(planUpdateReqDto));

        // when
        when(planService.update(any())).thenReturn(new PlanResDto(1L, "revisedTitle", LocalDate.now(), LocalDate.now().plusDays(3), PlanStatus.NOW));

        // then
        mockMvc.perform(request)
                .andExpect(status().isOk());

    }

    @Test
    @DisplayName("비정상 수정 - Resource not found")
    void updateTestAbnormal_resourceNotFound() throws Exception {

        // given
        PlanUpdateReqDto planUpdateReqDto = new PlanUpdateReqDto(1L, "revisedTitle", LocalDate.now(), LocalDate.now().plusDays(3));
        MockHttpServletRequestBuilder request = put("/plan")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(planUpdateReqDto));

        // when
        doThrow(ResourceNotFoundException.class).when(planService).update(any());

        // then
        mockMvc.perform(request)
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("message").value("Resource not found"));

    }

    @Test
    @DisplayName("비정상 수정 - ArgumentResolver Validation")
    void updateTestAbnormal_argumentResolverValidation() throws Exception {

        // given
        PlanUpdateReqDto planUpdateReqDto = new PlanUpdateReqDto(1L, null, LocalDate.now(), LocalDate.now().plusDays(3));
        MockHttpServletRequestBuilder request = put("/plan")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(planUpdateReqDto));

        // when
        doThrow(ResourceNotFoundException.class).when(planService).update(any());

        // then
        mockMvc.perform(request)
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("message").value("입력값 타입/내용 오류"));

    }

    @Test
    @DisplayName("비정상 수정 - ArgumentResolver Validation")
    void updateTestAbnormal_argumentResolverValidation2() throws Exception {

        // given
        PlanUpdateReqDto planUpdateReqDto = new PlanUpdateReqDto(1L, "plan-update", LocalDate.now().plusDays(3), LocalDate.now());
        MockHttpServletRequestBuilder request = put("/plan")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(planUpdateReqDto));

        // when
        doThrow(ResourceNotFoundException.class).when(planService).update(any());

        // then
        mockMvc.perform(request)
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("message").value("입력값 타입/내용 오류"));

    }


    /**
     * 삭제 API 관련 테스트
     * @see PlanController#delete(Long)
     * @throws Exception mockMvc.perform()
     */
    @Test
    @DisplayName("정상 삭제")
    void deleteTestNormal() throws Exception {

        // given
        MockHttpServletRequestBuilder request = delete("/plan/1");

        // when
        doNothing().when(planService).delete(any());

        // then
        mockMvc.perform(request)
                .andExpect(status().isNoContent());

    }

    @Test
    @DisplayName("비정상 삭제")
    void deleteTestAbnormal() throws Exception {

        // given
        MockHttpServletRequestBuilder request = delete("/plan/1");

        // when
        doThrow(ResourceNotFoundException.class).when(planService).delete(any());

        // then
        mockMvc.perform(request)
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("message").value("Resource not found"));

    }


    /**
     * 상태 변경 API 관련 테스트
     * @see PlanController#change(Long)
     * @throws Exception mockMvc.perform()
     */
    @Test
    @DisplayName("정상 상태 변경")
    void changeStatusNormal() throws Exception {

        // given
        MockHttpServletRequestBuilder request = patch("/plan/1");

        // when
        when(planService.change(any())).thenReturn(new PlanResDto(1L, "plan", LocalDate.now(), LocalDate.now().plusDays(3), PlanStatus.COMPLETED));

        // then
        mockMvc.perform(request)
                .andExpect(status().isOk())
                .andExpect(jsonPath("status").value("COMPLETED"));

    }

    @Test
    @DisplayName("비정상 상태 변경 - Resource not found")
    void changeStatusAbnormal_resourceNotFound() throws Exception {

        // given
        MockHttpServletRequestBuilder request = patch("/plan/1");

        // when
        doThrow(ResourceNotFoundException.class).when(planService).change(any());

        // then
        mockMvc.perform(request)
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("message").value("Resource not found"));

    }


    /**
     * 컬렉션 조회 API의 더미 리턴값을 생성하는 메소드
     * PlanService의 메소드가 호출될 경우 더미 리턴값을 반환함
     */
    private List<PlanResDto> makeSamplePlans() {
        List<PlanResDto> plans = new ArrayList<>();
        plans.add(new PlanResDto(1L, "title1", LocalDate.of(2023, 7, 18), LocalDate.of(2023, 7, 20), PlanStatus.PAST));
        plans.add(new PlanResDto(2L, "title2", LocalDate.of(2023, 7, 18), LocalDate.of(2023, 7, 20), PlanStatus.PAST));
        plans.add(new PlanResDto(3L, "title3", LocalDate.of(2023, 7, 18), LocalDate.of(2023, 7, 20), PlanStatus.PAST));
        return plans;
    }


    /**
     * 순수 컬렉션 조회 API 관련 테스트
     * 순수 컬렉션이란 필터링이 걸려 있지 않은 컬렉션 조회 API를 의미함
     * @see PlanController#all(Long)
     * @throws Exception mockMvc.perform()
     */
    @Test
    @DisplayName("정상 순수 컬렉션 조회")
    void allTestNormal() throws Exception {

        // given
        List<PlanResDto> plans = makeSamplePlans();
        MockHttpServletRequestBuilder request = get("/plans")
                .param("memberId", "1");

        // when
        when(planService.all(any())).thenReturn(plans);

        // then
        mockMvc.perform(request)
                .andExpect(status().isOk())
                .andExpect(jsonPath("_embedded.planResDtoList.length()").value(3));

    }

    @Test
    @DisplayName("비정상 순수 컬렉션 조회")
    void allTestAbnormal() throws Exception {

        // given
        MockHttpServletRequestBuilder request = get("/plans")
                .param("memberId", "1");

        // when
        doThrow(ResourceNotFoundException.class).when(planService).all(any());

        // then
        mockMvc.perform(request)
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("message").value("Resource not found"));

    }


    /**
     * 일별 컬렉션 API 관련 테스트
     * 일별 컬렉션이란 날짜(하루)를 기준으로 조회한 컬렉션을 의미함
     * @see PlanController#all(Long, LocalDate)
     * @throws Exception mockMvc.perform()
     */
    @Test
    @DisplayName("정상 일별 컬렉션 조회")
    void collectionFilteredByDateTestNormal() throws Exception {

        // given
        List<PlanResDto> plans = makeSamplePlans();

        MockHttpServletRequestBuilder request = get("/plans/date")
                .param("memberId", "1")
                .param("dateKey", LocalDate.of(2023, 7, 19).toString());

        // when
        doReturn(plans).when(planService).all(any(), any());

        // then
        mockMvc.perform(request)
                .andExpect(status().isOk())
                .andExpect(jsonPath("_embedded.planResDtoList.length()").value(3));

    }

    @Test
    @DisplayName("비정상 일별 컬렉션 조회 - Resource not found")
    void collectionFilteredByDateTestAbnormal() throws Exception {

        // given
        MockHttpServletRequestBuilder request = get("/plans/date")
                .param("memberId", String.valueOf(Long.MAX_VALUE))
                .param("dateKey", LocalDate.of(2023, 7, 19).toString());

        // when
        doThrow(ResourceNotFoundException.class).when(planService).all(any(), any());

        // then
        mockMvc.perform(request)
                .andExpect(jsonPath("message").value("Resource not found"));

    }


    /**
     * 기간 컬렉션 조회 API 관련 테스트
     * 기간 컬렉션이란 시작일 ~ 종료일 사이에 Checkbox를 하나라도 가지고 있는 컬렉션을 의미한다.
     * @see PlanController#all(Long, LocalDate, LocalDate)
     * @throws Exception mockMvc.perform()
     */
    @Test
    @DisplayName("정상 기간 컬렉션 조회")
    void collectionFilteredByDateRangeTestNormal() throws Exception {

        // given
        List<PlanResDto> plans = makeSamplePlans();
        MockHttpServletRequestBuilder request = get("/plans/range")
                .param("memberId", "1")
                .param("searchStart", LocalDate.of(2023, 7, 19).toString())
                .param("searchEnd", LocalDate.of(2023, 7, 25).toString());

        // when
        when(planService.all(any(), any(), any())).thenReturn(plans);

        // then
        mockMvc.perform(request)
                .andExpect(status().isOk())
                .andExpect(jsonPath("_embedded.planResDtoList.length()").value(3));

    }

    @Test
    @DisplayName("비정상 기간 컬렉션 조회 - invalid query string")
    void collectionFilteredByDateRangeTestAbnormal_invalidQueryString() throws Exception {

        // given
        MockHttpServletRequestBuilder request = get("/plans/range")
                .param("memberId", "1")
                .param("searchStart", LocalDate.of(2023, 7, 19).toString())
                .param("searchEnd", LocalDate.of(2023, 7, 16).toString());

        // when - then
        mockMvc.perform(request)
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("errors.searchStart").exists())
                .andExpect(jsonPath("errors.searchEnd").exists());

    }

    @Test
    @DisplayName("비정상 기간 컬렉션 조회 - Resource not found")
    void collectionFilteredByDateRangeTestAbnormal_resourceNotFound() throws Exception {

        // given
        MockHttpServletRequestBuilder request = get("/plans/range")
                .param("memberId", "1")
                .param("searchStart", LocalDate.of(2023, 7, 19).toString())
                .param("searchEnd", LocalDate.of(2023, 7, 25).toString());

        // when
        doThrow(ResourceNotFoundException.class).when(planService).all(any(), any(), any());

        // then
        mockMvc.perform(request)
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("message").value("Resource not found"));

    }

}
