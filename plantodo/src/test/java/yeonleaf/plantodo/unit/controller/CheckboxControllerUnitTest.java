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
import org.springframework.validation.BindingResult;
import yeonleaf.plantodo.TestConfig;
import yeonleaf.plantodo.controller.CheckboxController;
import yeonleaf.plantodo.dto.CheckboxReqDto;
import yeonleaf.plantodo.dto.CheckboxResDto;
import yeonleaf.plantodo.dto.CheckboxUpdateReqDto;
import yeonleaf.plantodo.exceptions.ResourceNotFoundException;
import yeonleaf.plantodo.service.CheckboxServiceTestImpl;
import yeonleaf.plantodo.domain.Checkbox;
import yeonleaf.plantodo.exceptions.ApiBindingError;
import yeonleaf.plantodo.exceptions.ApiSimpleError;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;


/**
 * target : {@link CheckboxController}의 모든 메소드
 * target description : {@link CheckboxReqDto}나 {@link CheckboxUpdateReqDto}를 받아 {@link Checkbox}에 대한 등록, 수정, 조회, 삭제, 상태 변경을 수행하는 API
 *                      조회 API의 경우 조회 기준(plan 기준 조회, group 기준 조회)과 필터링(날짜, 기간)을 제공함
 *
 * test description : 컨트롤러 단일 테스트 (인터셉터 포함 안함)
 *                    삭제 API를 제외하고 정상적으로 요청이 수행된 경우 {@link CheckboxResDto} 혹은 List<CheckboxResDto>를 리턴하는지 확인
 *                    쿼리 스트링이나 argument에 대한 validation을 통과하지 못한 경우 {@link ApiBindingError}를 리턴하는지 확인
 *                    기준(plan, group)을 조회할 수 없는 경우 {@link ApiSimpleError}를 리턴하는지 확인
 *
 */
@Import({TestConfig.class})
@WebMvcTest(CheckboxController.class)
public class CheckboxControllerUnitTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private CheckboxServiceTestImpl checkboxService;


    /**
     * 등록 API 관련 테스트
     * @see CheckboxController#save(CheckboxReqDto, BindingResult)
     * @throws Exception mockMvc.perform()
     */
    @Test
    @DisplayName("정상 등록")
    void saveTestNormal() throws Exception {

        // given
        CheckboxReqDto checkboxReqDto = new CheckboxReqDto("checkbox", 1L, LocalDate.now());
        MockHttpServletRequestBuilder request = post("/checkbox")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(checkboxReqDto));

        // when
        when(checkboxService.save(any())).thenReturn(new CheckboxResDto(1L, "checkbox", LocalDate.of(2023, 7, 18), false));

        // then
        mockMvc.perform(request)
                .andExpect(status().isCreated())
                .andExpect(jsonPath("id").value(1L))
                .andExpect(jsonPath("_links").exists());

    }

    @Test
    @DisplayName("비정상 등록 - Argument Resolver Validation")
    void saveTestAbnormal_ArgumentResolverValidation() throws Exception {

        // given
        CheckboxReqDto checkboxReqDto = new CheckboxReqDto("checkbox", null, LocalDate.now());
        MockHttpServletRequestBuilder request = post("/checkbox")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(checkboxReqDto));

        // when - then
        mockMvc.perform(request)
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("message").value("입력값 타입/내용 오류"));

    }

    @Test
    @DisplayName("비정상 등록 - Resource not found")
    void saveTestAbnormal_ResourceNotFound_Plan() throws Exception {

        // given
        CheckboxReqDto checkboxReqDto = new CheckboxReqDto("checkbox", 1L, LocalDate.now());
        MockHttpServletRequestBuilder request = post("/checkbox")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(checkboxReqDto));

        // when
        when(checkboxService.save(any())).thenThrow(new ResourceNotFoundException());

        // then
        mockMvc.perform(request)
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("message").value("Resource not found"));

    }


    /**
     * 단건 조회 API 관련 테스트
     * @see CheckboxController#one(Long)
     * @throws Exception mockMvc.perform()
     */
    @Test
    @DisplayName("단건 정상 조회")
    void oneTestNormal() throws Exception {

        // given
        MockHttpServletRequestBuilder request = get("/checkbox/1");

        // when
        when(checkboxService.one(any())).thenReturn(new CheckboxResDto(1L, "checkbox", LocalDate.of(2023, 7, 18), false));

        // then
        mockMvc.perform(request)
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaTypes.HAL_JSON))
                .andExpect(jsonPath("id").isNumber());

    }

    @Test
    @DisplayName("단건 비정상 조회")
    void oneTestAbnormal() throws Exception {

        // given
        MockHttpServletRequestBuilder request = get("/checkbox/1");

        // when
        when(checkboxService.one(any())).thenThrow(ResourceNotFoundException.class);

        // then
        mockMvc.perform(request)
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("message").value("Resource not found"));

    }


    /**
     * 수정 API 관련 테스트
     * @see CheckboxController#update(CheckboxUpdateReqDto, BindingResult)
     * @throws Exception mockMvc.perform()
     */
    @Test
    @DisplayName("정상 수정")
    void updateTestNormal() throws Exception {

        // given
        CheckboxUpdateReqDto checkboxUpdateReqDto = new CheckboxUpdateReqDto(1L, "updatedTitle");
        MockHttpServletRequestBuilder request = put("/checkbox")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(checkboxUpdateReqDto));

        // when
        when(checkboxService.update(any())).thenReturn(new CheckboxResDto(1L, "updatedTitle", LocalDate.of(2023, 7, 18), false));

        // then
        mockMvc.perform(request)
                .andExpect(status().isOk())
                .andExpect(jsonPath("title").value("updatedTitle"));

    }

    @Test
    @DisplayName("비정상 수정 - ArgumentResolver Validation")
    void updateTestAbnormal_argumentResolverValidation() throws Exception {

        // given
        CheckboxUpdateReqDto checkboxUpdateReqDto = new CheckboxUpdateReqDto(null, "updatedTitle");
        MockHttpServletRequestBuilder request = put("/checkbox")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(checkboxUpdateReqDto));

        // when - then
        mockMvc.perform(request)
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("errors.id").exists())
                .andDo(print());

    }

    @Test
    @DisplayName("비정상 수정 - Resource not found")
    void updateTestAbnormal_resourceNotFound() throws Exception {

        // given
        CheckboxUpdateReqDto checkboxUpdateReqDto = new CheckboxUpdateReqDto(Long.MAX_VALUE, "updatedTitle");
        MockHttpServletRequestBuilder request = put("/checkbox")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(checkboxUpdateReqDto));

        // when
        when(checkboxService.update(any())).thenThrow(ResourceNotFoundException.class);

        // then
        mockMvc.perform(request)
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("message").value("Resource not found"));

    }


    /**
     * 삭제 API 관련 테스트
     * @see CheckboxController#delete(Long)
     * @throws Exception mockMvc.perform()
     */
    @Test
    @DisplayName("정상 삭제")
    void deleteTestNormal() throws Exception {

        // given
        MockHttpServletRequestBuilder request = delete("/checkbox/1");

        // when
        doNothing().when(checkboxService).delete(any());

        // then
        mockMvc.perform(request)
                .andExpect(status().isNoContent());

    }

    @Test
    @DisplayName("비정상 삭제")
    void deleteTestAbnormal() throws Exception {

        // given
        MockHttpServletRequestBuilder request = delete("/checkbox/1");

        // when
        doThrow(ResourceNotFoundException.class).when(checkboxService).delete(any());

        // then
        mockMvc.perform(request)
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("message").value("Resource not found"));

    }


    /**
     * 상태 변경 API 관련 테스트
     * @see CheckboxController#change(Long)
     * @throws Exception mockMvc.perform
     */
    @Test
    @DisplayName("정상 상태 변경")
    void changeStatusTestNormal() throws Exception {

        // given
        MockHttpServletRequestBuilder request = patch("/checkbox/1");

        // when
        doReturn(new CheckboxResDto(1L, "checkbox", LocalDate.of(2023, 7, 18), true)).when(checkboxService).change(any());

        // then
        mockMvc.perform(request)
                .andExpect(status().isOk())
                .andExpect(jsonPath("checked").value("true"));

    }

    @Test
    @DisplayName("비정상 상태 변경 - Resource not found")
    void changeStatusTestAbnormal() throws Exception {

        // given
        MockHttpServletRequestBuilder request = patch("/checkbox/1");

        // when
        doThrow(ResourceNotFoundException.class).when(checkboxService).change(any());

        // then
        mockMvc.perform(request)
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("message").value("Resource not found"));

    }


    /**
     * 컬렉션 조회 API의 더미 리턴값을 만드는 메소드
     * CheckboxService의 메소드가 호출될 경우 더미 리턴값을 반환함
     */
    private List<CheckboxResDto> makeSampleCheckboxes() {

        List<CheckboxResDto> checkboxes = new ArrayList<>();
        checkboxes.add(new CheckboxResDto(1L, "checkbox", LocalDate.of(2023, 7, 18), false));
        checkboxes.add(new CheckboxResDto(1L, "checkbox", LocalDate.of(2023, 7, 18), false));
        checkboxes.add(new CheckboxResDto(1L, "checkbox", LocalDate.of(2023, 7, 18), false));
        return checkboxes;

    }


    /**
     * 순수 컬렉션 조회 API 관련 테스트
     * 순수 컬렉션이란 필터링이 걸려 있지 않은 컬렉션 조회 API를 의미함
     * @see CheckboxController#allByPlan(Long)
     * @see CheckboxController#allByGroup(Long)
     * @throws Exception mockMvc.perform()
     */
    @Test
    @DisplayName("정상 순수 컬렉션 조회 - by plan")
    void allTestNormal_byPlan() throws Exception {

        // given
        List<CheckboxResDto> checkboxes = makeSampleCheckboxes();
        MockHttpServletRequestBuilder request = get("/checkboxes/plan")
                .param("planId", "1");

        // when
        when(checkboxService.allByPlan(any())).thenReturn(checkboxes);

        // then
        mockMvc.perform(request)
                .andExpect(status().isOk())
                .andExpect(jsonPath("_embedded.checkboxResDtoList.length()").value(3))
                .andExpect(jsonPath("_links.plan").exists());

    }

    @Test
    @DisplayName("정상 순수 컬렉션 조회 - by group")
    void allTestNormal_byGroup() throws Exception {

        // given
        List<CheckboxResDto> checkboxes = makeSampleCheckboxes();
        MockHttpServletRequestBuilder request = get("/checkboxes/group")
                .param("groupId", "1");

        // when
        when(checkboxService.allByGroup(any())).thenReturn(checkboxes);

        // then
        mockMvc.perform(request)
                .andExpect(status().isOk())
                .andExpect(jsonPath("_embedded.checkboxResDtoList.length()").value(3))
                .andExpect(jsonPath("_links.group").exists());

    }

    @Test
    @DisplayName("비정상 순수 컬렉션 조회 - by plan - Resource not found")
    void allTestAbnormal_byPlan_resourceNotFound() throws Exception {

        // given
        MockHttpServletRequestBuilder request = get("/checkboxes/plan")
                .param("planId", "1");

        // when
        doThrow(ResourceNotFoundException.class).when(checkboxService).allByPlan(any());

        // then
        mockMvc.perform(request)
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("message").value("Resource not found"));

    }

    @Test
    @DisplayName("비정상 순수 컬렉션 조회 - by group - Resource not found")
    void allTestAbnormal_byGroup_resourceNotFound() throws Exception {

        // given
        MockHttpServletRequestBuilder request = get("/checkboxes/group")
                .param("groupId", "1");

        // when
        doThrow(ResourceNotFoundException.class).when(checkboxService).allByGroup(any());

        // then
        mockMvc.perform(request)
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("message").value("Resource not found"));

    }


    /**
     * 일별 컬렉션 조회 API 관련 테스트
     * 일별 컬렉션이란 날짜(하루)를 기준으로 조회한 컬렉션을 의미함
     * @see CheckboxController#allByPlan(Long, LocalDate)
     * @see CheckboxController#allByGroup(Long, LocalDate)
     * @throws Exception mockMvc.perform()
     */
    @Test
    @DisplayName("정상 일별 컬렉션 조회 - by group")
    void collectionFilteredByDateTestNormal_byGroup() throws Exception {

        // given
        List<CheckboxResDto> checkboxes = makeSampleCheckboxes();
        MockHttpServletRequestBuilder request = get("/checkboxes/group/date")
                .param("groupId", "1")
                .param("dateKey", LocalDate.of(2023, 7, 31).toString());

        // when
        when(checkboxService.allByGroup(any(), any())).thenReturn(checkboxes);

        // then
        mockMvc.perform(request)
                .andExpect(status().isOk())
                .andExpect(jsonPath("_embedded.checkboxResDtoList.length()").value(3));

    }

    @Test
    @DisplayName("정상 일별 컬렉션 조회 - by group - Resource not found")
    void collectionFilteredByDateTestAbnormal_byGroup() throws Exception {

        // given
        MockHttpServletRequestBuilder request = get("/checkboxes/group/date")
                .param("groupId", String.valueOf(Long.MAX_VALUE))
                .param("dateKey", LocalDate.of(2023, 7, 31).toString());

        // when
        doThrow(ResourceNotFoundException.class).when(checkboxService).allByGroup(any(), any());

        // then
        mockMvc.perform(request)
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("message").value("Resource not found"));

    }

    @Test
    @DisplayName("정상 일별 컬렉션 조회 - by plan")
    void collectionFilteredByDateTestNormal_byPlan() throws Exception {

        // given
        List<CheckboxResDto> checkboxes = makeSampleCheckboxes();
        MockHttpServletRequestBuilder request = get("/checkboxes/plan/date")
                .param("planId", "1")
                .param("dateKey", LocalDate.of(2023, 7, 31).toString());

        // when
        when(checkboxService.allByPlan(any(), any())).thenReturn(checkboxes);

        // then
        mockMvc.perform(request)
                .andExpect(status().isOk())
                .andExpect(jsonPath("_embedded.checkboxResDtoList.length()").value(3));

    }

    @Test
    @DisplayName("비정상 일별 컬렉션 조회 - by plan - Resource not found")
    void collectionFilteredByDateTestAbnormal_byPlan() throws Exception {

        // given
        MockHttpServletRequestBuilder request = get("/checkboxes/plan/date")
                .param("planId", String.valueOf(Long.MAX_VALUE))
                .param("dateKey", LocalDate.of(2023, 7, 31).toString());

        // when
        doThrow(ResourceNotFoundException.class).when(checkboxService).allByPlan(any(), any());

        // then
        mockMvc.perform(request)
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("message").value("Resource not found"));

    }


    /**
     * 기간 컬렉션 조회 API 관련 테스트
     * 기간 컬렉션이란 시작일 ~ 종료일 사이에 Checkbox를 하나라도 가지고 있는 컬렉션을 의미한다.
     * @see CheckboxController#allByPlan(Long, LocalDate, LocalDate)
     * @see CheckboxController#allByGroup(Long, LocalDate, LocalDate)
     * @throws Exception mockMvc.perform()
     */
    @Test
    @DisplayName("정상 기간 컬렉션 조회 - by group")
    void collectionFilteredByDateRangeTest_byGroup() throws Exception {

        // given
        List<CheckboxResDto> checkboxes = makeSampleCheckboxes();
        MockHttpServletRequestBuilder request = get("/checkboxes/group/range")
                .param("groupId", "1")
                .param("searchStart", LocalDate.of(2023, 7, 19).toString())
                .param("searchEnd", LocalDate.of(2023, 7, 23).toString());

        // when
        when(checkboxService.allByGroup(any(), any(), any())).thenReturn(checkboxes);

        // then
        mockMvc.perform(request)
                .andExpect(status().isOk())
                .andExpect(jsonPath("_embedded.checkboxResDtoList.length()").value(3));

    }

    @Test
    @DisplayName("비정상 기간 컬렉션 조회 - by group - Invalid query string")
    void collectionFilteredByDateRangeTest_byGroup_invalidQueryString_1() throws Exception {

        // given
        MockHttpServletRequestBuilder request = get("/checkboxes/group/range")
                .param("groupId", "1")
                .param("searchStart", LocalDate.of(2023, 7, 19).toString())
                .param("searchEnd", LocalDate.of(2023, 7, 16).toString());

        // when - then
        mockMvc.perform(request)
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("errors.searchStart").exists())
                .andExpect(jsonPath("errors.searchEnd").exists());

    }

    @Test
    @DisplayName("비정상 기간 컬렉션 조회 - by group - Resource not found")
    void collectionFilteredByDateRangeTest_byGroup_resourceNotFound() throws Exception {

        // given
        MockHttpServletRequestBuilder request = get("/checkboxes/group/range")
                .param("groupId", "1")
                .param("searchStart", LocalDate.of(2023, 7, 16).toString())
                .param("searchEnd", LocalDate.of(2023, 7, 19).toString());

        // when
        doThrow(ResourceNotFoundException.class).when(checkboxService).allByGroup(any(), any(), any());

        // then
        mockMvc.perform(request)
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("message").value("Resource not found"));

    }

    @Test
    @DisplayName("정상 기간 컬렉션 조회 - by plan")
    void collectionFilteredByDateRangeTest_byPlan() throws Exception {

        // given
        List<CheckboxResDto> checkboxes = makeSampleCheckboxes();
        MockHttpServletRequestBuilder request = get("/checkboxes/plan/range")
                .param("planId", "1")
                .param("searchStart", LocalDate.of(2023, 7, 19).toString())
                .param("searchEnd", LocalDate.of(2023, 7, 23).toString());

        // when
        when(checkboxService.allByPlan(any(), any(), any())).thenReturn(checkboxes);

        // then
        mockMvc.perform(request)
                .andExpect(status().isOk())
                .andExpect(jsonPath("_embedded.checkboxResDtoList.length()").value(3));

    }

    @Test
    @DisplayName("비정상 기간 컬렉션 조회 - by plan - Invalid query string")
    void collectionFilteredByDateRangeTestAbnormal_byPlan_invalidQueryString() throws Exception {

        // given
        MockHttpServletRequestBuilder request = get("/checkboxes/plan/range")
                .param("planId", "1")
                .param("searchStart", LocalDate.of(2023, 7, 19).toString())
                .param("searchEnd", LocalDate.of(2023, 7, 16).toString());

        // when - then
        mockMvc.perform(request)
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("errors.searchStart").exists())
                .andExpect(jsonPath("errors.searchEnd").exists());

    }

    @Test
    @DisplayName("비정상 기간 컬렉션 조회 - by plan - Resource not found")
    void collectionFilteredByDateRangeTestAbnormal_byPlan_resourceNotFound() throws Exception {

        // given
        MockHttpServletRequestBuilder request = get("/checkboxes/plan/range")
                .param("planId", "1")
                .param("searchStart", LocalDate.of(2023, 7, 16).toString())
                .param("searchEnd", LocalDate.of(2023, 7, 19).toString());

        // when
        doThrow(ResourceNotFoundException.class).when(checkboxService).allByPlan(any(), any(), any());

        // then
        mockMvc.perform(request)
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("message").value("Resource not found"));

    }

}
