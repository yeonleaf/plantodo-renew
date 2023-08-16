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
import yeonleaf.plantodo.controller.GroupController;
import yeonleaf.plantodo.dto.GroupReqDto;
import yeonleaf.plantodo.dto.GroupResDto;
import yeonleaf.plantodo.dto.GroupUpdateReqDto;
import yeonleaf.plantodo.exceptions.ResourceNotFoundException;
import yeonleaf.plantodo.service.GroupService;
import yeonleaf.plantodo.exceptions.ApiBindingError;
import yeonleaf.plantodo.exceptions.ApiSimpleError;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * target : {@link GroupController}에 있는 모든 메소드
 * target description : 주기적으로 반복되는 할 일을 생성, 조회, 삭제, 수정하는 API
 *
 * test description : 컨트롤러 단일 테스트 (인터셉터 포함하지 않음)
 *                    삭제 API를 제외하고 정상적으로 API가 수행된 경우 {@link GroupResDto}나 컬렉션 조회의 경우 List<GroupResDto>를 리턴하는지 확인한다.
 *                    query string이나 argument validation을 통과하지 못한 경우 {@link ApiBindingError}를 리턴하는지 확인한다.
 *                    대상이 존재하지 않을 경우 {@link ApiSimpleError}를 리턴하는지 확인한다.
 */
@Import(TestConfig.class)
@WebMvcTest(GroupController.class)
public class GroupControllerUnitTest {

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private GroupService groupService;


    /**
     * 등록 API 관련 테스트
     * @see GroupController#save(GroupReqDto, BindingResult)
     * @throws Exception mockMvc.perform()
     */
    @Test
    @DisplayName("정상 등록")
    void saveTestNormal() throws Exception {

        // given
        GroupReqDto groupReqDto = new GroupReqDto("title", 3, List.of("월", "화"), 1L);
        String requestData = objectMapper.writeValueAsString(groupReqDto);
        MockHttpServletRequestBuilder request = post("/group")
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestData);

        // when
        when(groupService.save(any())).thenReturn(new GroupResDto(1L, "title", 3, List.of("월", "화")));

        // then
        mockMvc.perform(request)
                .andExpect(status().isCreated())
                .andExpect(jsonPath("id").value(1));

    }

    @Test
    @DisplayName("비정상 등록 - ArgumentResolver Validation")
    void saveTestAbnormal_ArgumentResolverValidation() throws Exception {

        // given
        GroupReqDto groupReqDto = new GroupReqDto(null, 4, List.of("월", "화"), 1L);
        String requestData = objectMapper.writeValueAsString(groupReqDto);
        MockHttpServletRequestBuilder request = post("/group")
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestData);

        // when - then
        mockMvc.perform(request)
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("message").value("입력값 타입/내용 오류"))
                .andExpect(jsonPath("errors.title").isNotEmpty())
                .andExpect(jsonPath("errors.repOption").isNotEmpty());

    }

    @Test
    @DisplayName("비정상 등록 - RepInputValidator")
    void saveTestAbnormal_RepInputValidator() throws Exception {

        // given
        GroupReqDto groupReqDto = new GroupReqDto("title", 2, List.of(), 1L);
        String requestData = objectMapper.writeValueAsString(groupReqDto);
        MockHttpServletRequestBuilder request = post("/group")
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestData);

        // when - then
        mockMvc.perform(request)
                .andExpect(jsonPath("message").value("입력값 형식 오류"))
                .andExpect(jsonPath("errors.repValue").isNotEmpty());

    }

    @Test
    @DisplayName("비정상 등록 - Resource not found")
    void saveTestAbnormal_ResourceNotFound() throws Exception {

        // given
        GroupReqDto groupReqDto = new GroupReqDto("title", 3, List.of("월", "수"), 1L);
        String requestData = objectMapper.writeValueAsString(groupReqDto);
        MockHttpServletRequestBuilder request = post("/group")
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestData);

        // when
        when(groupService.save(any())).thenThrow(new ResourceNotFoundException("Resource not found"));

        // then
        mockMvc.perform(request)
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("message").value("Resource not found"));

    }


    /**
     * 단건 조회 API 관련 테스트
     * @see GroupController#one(Long)
     * @throws Exception mockMvc.perform()
     */
    @Test
    @DisplayName("정상 단건 조회")
    void oneTestNormal() throws Exception {

        // given
        MockHttpServletRequestBuilder request = get("/group/1");

        // when
        when(groupService.one(any())).thenReturn(new GroupResDto(1L, "group", 1, List.of()));

        // then
        mockMvc.perform(request)
                .andExpect(status().isOk())
                .andExpect(jsonPath("id").value(1L))
                .andExpect(content().contentType(MediaTypes.HAL_JSON))
                .andExpect(jsonPath("_links").exists())
                .andExpect(jsonPath("_links.checkboxes").exists());

    }

    @Test
    @DisplayName("비정상 단건 조회")
    void oneTestAbnormal() throws Exception {

        // given
        MockHttpServletRequestBuilder request = get("/group/1");

        // when
        when(groupService.one(any())).thenThrow(new ResourceNotFoundException());

        // then
        mockMvc.perform(request)
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("message").value("Resource not found"));

    }


    /**
     * 수정 API 관련 테스트
     * @see GroupController#update(GroupUpdateReqDto, BindingResult)
     * @throws Exception mockMvc.perform()
     */
    @Test
    @DisplayName("정상 수정")
    void updateTestNormal() throws Exception {

        // given
        GroupUpdateReqDto groupUpdateReqDto = new GroupUpdateReqDto(1L, "updatedTitle", 1, List.of());
        MockHttpServletRequestBuilder request = put("/group")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(groupUpdateReqDto));

        // when
        when(groupService.update(any())).thenReturn(new GroupResDto(groupUpdateReqDto.getId(), groupUpdateReqDto.getTitle(), groupUpdateReqDto.getRepOption(), groupUpdateReqDto.getRepValue()));

        // then
        mockMvc.perform(request)
                .andExpect(status().isOk())
                .andExpect(jsonPath("title").value("updatedTitle"))
                .andExpect(jsonPath("_links.self").exists());

    }

    @Test
    @DisplayName("비정상 수정 - ArgumentResolver Validation")
    void updateTestAbnormal_argumentResolverValidation() throws Exception {

        // given
        GroupUpdateReqDto groupUpdateReqDto = new GroupUpdateReqDto(1L, "updatedTitle", 0, List.of());
        MockHttpServletRequestBuilder request = put("/group")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(groupUpdateReqDto));

        // when
        when(groupService.update(any())).thenReturn(new GroupResDto(groupUpdateReqDto.getId(), groupUpdateReqDto.getTitle(), groupUpdateReqDto.getRepOption(), groupUpdateReqDto.getRepValue()));

        // then
        mockMvc.perform(request)
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("message").value("입력값 타입/내용 오류"));

    }

    @Test
    @DisplayName("비정상 수정 - repInputValidator")
    void updateTestAbnormal_repInputValidator() throws Exception {

        // given
        GroupUpdateReqDto groupUpdateReqDto = new GroupUpdateReqDto(1L, "updatedTitle", 2, List.of("월", "수", "금"));
        MockHttpServletRequestBuilder request = put("/group")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(groupUpdateReqDto));

        // when
        when(groupService.update(any())).thenReturn(new GroupResDto(groupUpdateReqDto.getId(), groupUpdateReqDto.getTitle(), groupUpdateReqDto.getRepOption(), groupUpdateReqDto.getRepValue()));

        // then
        mockMvc.perform(request)
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("message").value("입력값 형식 오류"));

    }


    /**
     * 삭제 API 관련 테스트
     * @see GroupController#delete(Long)
     * @throws Exception mockMvc.perform()
     */
    @Test
    @DisplayName("정상 삭제")
    void deleteTestNormal() throws Exception {

        // given
        MockHttpServletRequestBuilder request = delete("/group/1");

        // when
        doNothing().when(groupService).delete(any());

        // then
        mockMvc.perform(request)
                .andExpect(status().isNoContent());

    }

    @Test
    @DisplayName("비정상 삭제")
    void deleteTestAbnormal() throws Exception {

        // given
        MockHttpServletRequestBuilder request = delete("/group/1");

        // when
        doThrow(ResourceNotFoundException.class).when(groupService).delete(any());

        // then
        mockMvc.perform(request)
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("message").value("Resource not found"));

    }

    /**
     * 컬렉션 조회 API의 더미 리턴값을 생성하는 메소드
     * GroupService의 메소드가 호출될 경우 더미 리턴값을 반환함
     */
    private List<GroupResDto> makeSampleGroups() {
        List<GroupResDto> groups = new ArrayList<>();
        groups.add(new GroupResDto(1L, "title1", 1, List.of()));
        groups.add(new GroupResDto(2L, "title1", 1, List.of()));
        groups.add(new GroupResDto(3L, "title1", 1, List.of()));
        return groups;
    }


    /**
     * 순수 컬렉션 조회 API 관련 테스트
     * 순수 컬렉션이란 필터링이 걸려 있지 않은 컬렉션 조회 API를 의미함
     * @see GroupController#all(Long)
     * @throws Exception mockMvc.perform()
     */
    @Test
    @DisplayName("정상 순수 컬렉션 조회")
    void allTestNormal() throws Exception {

        // given
        List<GroupResDto> groups = makeSampleGroups();
        MockHttpServletRequestBuilder request = get("/groups")
                .param("planId", "1");

        // when
        when(groupService.all(any())).thenReturn(groups);

        // then
        mockMvc.perform(request)
                .andExpect(status().isOk())
                .andExpect(jsonPath("_embedded.groupResDtoList.length()").value(3));

    }

    @Test
    @DisplayName("비정상 순수 컬렉션 조회")
    void allTestAbnormal() throws Exception {

        // given
        MockHttpServletRequestBuilder request = get("/groups")
                .param("planId", "1");

        // when
        doThrow(ResourceNotFoundException.class).when(groupService).all(any());

        // then
        mockMvc.perform(request)
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("message").value("Resource not found"));

    }


    /**
     * 일별 컬렉션 조회 API 관련 테스트
     * 일별 컬렉션이란 날짜(하루)를 기준으로 조회한 컬렉션을 의미함
     * @see GroupController#all(Long, LocalDate)
     * @throws Exception mockMvc.perform()
     */
    @Test
    @DisplayName("정상 일별 컬렉션 조회")
    void collectionFilteredByDateTestNormal() throws Exception {

        // given
        List<GroupResDto> groups = makeSampleGroups();
        MockHttpServletRequestBuilder request = get("/groups/date")
                .param("planId", "1")
                .param("dateKey", LocalDate.of(2023, 7, 19).toString());

        // when
        when(groupService.all(any(), any())).thenReturn(groups);

        // then
        mockMvc.perform(request)
                .andExpect(status().isOk())
                .andExpect(jsonPath("_embedded.groupResDtoList.length()").value(3));

    }

    @Test
    @DisplayName("비정상 일별 컬렉션 조회")
    void collectionFilteredByDateTestAbnormal() throws Exception {

        // given
        MockHttpServletRequestBuilder request = get("/groups/date")
                .param("planId", "1")
                .param("dateKey", LocalDate.of(2023, 7, 19).toString());

        // when
        doThrow(ResourceNotFoundException.class).when(groupService).all(any(), any());

        // then
        mockMvc.perform(request)
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("message").value("Resource not found"));

    }


    /**
     * 기간 컬렉션 조회 API 관련 테스트
     * 기간 컬렉션이란 시작일 ~ 종료일 사이에 Checkbox를 하나라도 가지고 있는 컬렉션을 의미한다.
     * @see GroupController#all(Long, LocalDate, LocalDate)
     * @throws Exception mockMvc.perform()
     */
    @Test
    @DisplayName("정상 기간 컬렉션 조회")
    void collectionFilteredByDateRangeTestNormal() throws Exception {

        // given
        List<GroupResDto> groups = makeSampleGroups();
        MockHttpServletRequestBuilder request = get("/groups/range")
                .param("planId", "1")
                .param("searchStart", LocalDate.of(2023, 7, 19).toString())
                .param("searchEnd", LocalDate.of(2023, 7, 23).toString());

        // when
        when(groupService.all(any(), any(), any())).thenReturn(groups);

        // then
        mockMvc.perform(request)
                .andExpect(status().isOk())
                .andExpect(jsonPath("_embedded.groupResDtoList.length()").value(3));

    }

    @Test
    @DisplayName("비정상 기간 컬렉션 조회 - Invalid query string")
    void collectionFilteredByDateRangeTestAbnormal_invalidQueryString() throws Exception {

        // given
        MockHttpServletRequestBuilder request = get("/groups/range")
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
    @DisplayName("비정상 기간 컬렉션 조회 - Resource not found")
    void collectionFilteredByDateRangeTestAbnormal_resourceNotFound() throws Exception {

        // given
        MockHttpServletRequestBuilder request = get("/groups/range")
                .param("planId", String.valueOf(Long.MAX_VALUE))
                .param("searchStart", LocalDate.of(2023, 7, 19).toString())
                .param("searchEnd", LocalDate.of(2023, 7, 25).toString());

        // when
        doThrow(ResourceNotFoundException.class).when(groupService).all(any(), any(), any());

        // then
        mockMvc.perform(request)
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("message").value("Resource not found"));

    }

}
