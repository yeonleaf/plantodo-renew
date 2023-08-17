package yeonleaf.plantodo.unit.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
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
import yeonleaf.plantodo.controller.CalendarController;
import yeonleaf.plantodo.domain.PlanStatus;
import yeonleaf.plantodo.dto.CalendarRangeReqDto;
import yeonleaf.plantodo.dto.CheckboxResDto;
import yeonleaf.plantodo.dto.PlanResDto;
import yeonleaf.plantodo.exceptions.ResourceNotFoundException;
import yeonleaf.plantodo.service.CheckboxService;
import yeonleaf.plantodo.service.MemberService;
import yeonleaf.plantodo.service.PlanService;

import java.time.LocalDate;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * target : {@link CalendarController}에 있는 모든 메소드
 * target description : 날짜별로 여러 종류의 엔티티를 조회하는 API
 * test description : 인터셉터를 포함하지 않은 단일 컨트롤러 테스트
 */
@Import({TestConfig.class})
@WebMvcTest(CalendarController.class)
public class CalendarControllerUnitTest {

    @MockBean
    private MemberService memberService;

    @MockBean
    private PlanService planService;

    @MockBean
    private CheckboxService checkboxService;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private MockMvc mockMvc;


    /**
     * 기간 캘린더 조회 API 관련 테스트
     * 기간별로 일정과 일정과 연관된 할일 (그룹 할일, 일일 할일)을 함께 조회하는 API
     * @see CalendarController#getByRange(CalendarRangeReqDto, BindingResult)
     * @throws Exception mockMvc.perform();
    **/
    @Test
    @DisplayName("모든 validation을 통과한 경우 200 OK를 리턴하는지 확인한다.")
    void range_normalTest_checkStatusCode() throws Exception {

        // given
        LocalDate searchStart = LocalDate.of(2023, 8, 15);
        LocalDate searchEnd = LocalDate.of(2023, 8, 30);

        CalendarRangeReqDto calendarRangeReqDto = new CalendarRangeReqDto(1L, searchStart, searchEnd);

        MockHttpServletRequestBuilder request = get("/calendar/range")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(calendarRangeReqDto));

        // when - then
        mockMvc.perform(request)
                .andExpect(status().isOk());

    }

    @Test
    @DisplayName("조회할 회원이 없는 경우 ResourceNotFoundException을 던지는지 확인한다.")
    void range_abnormalTest_resourceNotFoundException() throws Exception {

        // given
        LocalDate searchStart = LocalDate.of(2023, 8, 15);
        LocalDate searchEnd = LocalDate.of(2023, 8, 30);

        CalendarRangeReqDto calendarRangeReqDto = new CalendarRangeReqDto(1L, searchStart, searchEnd);

        doThrow(ResourceNotFoundException.class).when(memberService).findById(any());

        MockHttpServletRequestBuilder request = get("/calendar/range")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(calendarRangeReqDto));


        // when - then
        mockMvc.perform(request)
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("message").value("Resource not found"));

    }

    @Test
    @DisplayName("검색 시작일이나 종료일에 null값이 들어가 있는 경우 ArgumentValidationException을 던지는지 확인한다.")
    void range_abnormalTest_nullArguments_argumentValidationException() throws Exception {

        // given
        CalendarRangeReqDto calendarRangeReqDto = new CalendarRangeReqDto(1L, null, null);

        MockHttpServletRequestBuilder request = get("/calendar/range")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(calendarRangeReqDto));


        // when - then
        mockMvc.perform(request)
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("errors.searchStart").exists())
                .andExpect(jsonPath("errors.searchEnd").exists());

    }

    @Test
    @DisplayName("검색 시작일이 검색 종료일보다 늦으면 ArgumentValidationException을 던지는지 확인한다.")
    void range_abnormalTest_invalidFormat_argumentValidationException() throws Exception {

        // given
        LocalDate searchStart = LocalDate.of(2023, 8, 30);
        LocalDate searchEnd = LocalDate.of(2023, 8, 15);

        CalendarRangeReqDto calendarRangeReqDto = new CalendarRangeReqDto(1L, searchStart, searchEnd);

        MockHttpServletRequestBuilder request = get("/calendar/range")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(calendarRangeReqDto));

        // when - then
        mockMvc.perform(request)
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("errors.searchStart").exists())
                .andExpect(jsonPath("errors.searchEnd").exists());

    }

    @Test
    @DisplayName("검색 시작일과 종료일이 같고 일정이 없을 때 시작일을 키로 가진 빈 결과값을 리턴하는지 확인한다.")
    void range_normalTest_searchStartEqualsSearchEnd_emptyResult() throws Exception {

        // given
        LocalDate searchStart = LocalDate.of(2023, 8, 15);
        LocalDate searchEnd = LocalDate.of(2023, 8, 15);

        CalendarRangeReqDto calendarRangeReqDto = new CalendarRangeReqDto(1L, searchStart, searchEnd);

        MockHttpServletRequestBuilder request = get("/calendar/range")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(calendarRangeReqDto));

        // when - then
        mockMvc.perform(request)
                .andExpect(status().isOk())
                .andExpect(jsonPath(LocalDate.of(2023, 8, 15).toString()).exists())
                .andDo(print());

    }

    @Test
    @DisplayName("검색 시작일과 종료일이 다르고 일정이 없을 때 시작일부터 종료일까지의 값을 키로 가진 빈 결과값을 리턴하는지 확인한다.")
    void range_validTest_searchStartDiffersSearchEnd_hasDateKeys_emptyResult() throws Exception {

        // given
        LocalDate searchStart = LocalDate.of(2023, 8, 15);
        LocalDate searchEnd = LocalDate.of(2023, 8, 18);

        CalendarRangeReqDto calendarRangeReqDto = new CalendarRangeReqDto(1L, searchStart, searchEnd);

        MockHttpServletRequestBuilder request = get("/calendar/range")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(calendarRangeReqDto));

        // when - then
        mockMvc.perform(request)
                .andExpect(status().isOk())
                .andExpect(jsonPath(LocalDate.of(2023, 8, 15).toString()).exists())
                .andExpect(jsonPath(LocalDate.of(2023, 8, 16).toString()).exists())
                .andExpect(jsonPath(LocalDate.of(2023, 8, 17).toString()).exists())
                .andExpect(jsonPath(LocalDate.of(2023, 8, 18).toString()).exists())
                .andDo(print());

    }

    @Test
    @DisplayName("검색 시작일과 종료일이 같고, 일정이 하나 있고, 그룹이나 일일 할일이 없을 때, 시작일을 키로 검색했을 때 일정 하나를 리턴하는지 확인한다.")
    void range_validTest_searchStartEqualsSearchEnd_onePlan_noGroup_noCheckboxes_returnOnePlan() throws Exception {

        // given
        LocalDate searchStart = LocalDate.of(2023, 8, 15);
        LocalDate searchEnd = LocalDate.of(2023, 8, 15);

        PlanResDto planResDto = new PlanResDto(1L, "planTitle",
                LocalDate.of(2023, 8, 13), LocalDate.of(2023, 8, 20), PlanStatus.NOW);
        List<PlanResDto> plans = List.of(planResDto);

        when(planService.all(any(), any())).thenReturn(plans);

        CalendarRangeReqDto calendarRangeReqDto = new CalendarRangeReqDto(1L, searchStart, searchEnd);

        MockHttpServletRequestBuilder request = get("/calendar/range")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(calendarRangeReqDto));

        // when - then
        mockMvc.perform(request)
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.['2023-08-15']").exists())
                .andExpect(jsonPath("$.['2023-08-15']").isNotEmpty())
                .andDo(print());

    }

    @Test
    @DisplayName("검색 시작일과 종료일이 같고, 일정이 하나 있고, 그룹이 없고, 일일 할일 하나가 있을 때 일정과 일일 할일 하나가 조회되는지 확인한다.")
    void range_validTest_searchStartEqualsSearchEnd_onePlan_noGroup_oneCheckbox_returnOnePlanOneCheckbox() throws Exception {

        // given
        LocalDate searchStart = LocalDate.of(2023, 8, 15);
        LocalDate searchEnd = LocalDate.of(2023, 8, 15);

        PlanResDto planResDto = new PlanResDto(1L, "planTitle",
                LocalDate.of(2023, 8, 13), LocalDate.of(2023, 8, 20), PlanStatus.NOW);
        List<PlanResDto> plans = List.of(planResDto);

        when(planService.all(any(), any())).thenReturn(plans);

        CheckboxResDto checkboxResDto = new CheckboxResDto(1L, "checkboxTitle", LocalDate.of(2023, 8, 15), true);
        List<CheckboxResDto> checkboxes = List.of(checkboxResDto);

        when(checkboxService.allByPlan(any(), any())).thenReturn(checkboxes);

        CalendarRangeReqDto calendarRangeReqDto = new CalendarRangeReqDto(1L, searchStart, searchEnd);

        MockHttpServletRequestBuilder request = get("/calendar/range")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(calendarRangeReqDto));

        // when - then
        mockMvc.perform(request)
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.['2023-08-15'].['" + planResDto + "']").isNotEmpty())
                .andDo(print());

    }

}
