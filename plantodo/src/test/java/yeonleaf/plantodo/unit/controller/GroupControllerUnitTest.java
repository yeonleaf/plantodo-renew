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
import yeonleaf.plantodo.controller.GroupController;
import yeonleaf.plantodo.dto.GroupReqDto;
import yeonleaf.plantodo.dto.GroupResDto;
import yeonleaf.plantodo.dto.GroupUpdateReqDto;
import yeonleaf.plantodo.exceptions.ResourceNotFoundException;
import yeonleaf.plantodo.service.GroupService;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@Import(TestConfig.class)
@WebMvcTest(GroupController.class)
public class GroupControllerUnitTest {

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private GroupService groupService;

    private List<String> makeArrToList(String... target) {
        return Arrays.asList(target);
    }

    @Test
    @DisplayName("정상 등록")
    void saveTestNormal() throws Exception {

        GroupReqDto groupReqDto = new GroupReqDto("title", 3, makeArrToList("월", "화"), 1L);
        String requestData = objectMapper.writeValueAsString(groupReqDto);

        MockHttpServletRequestBuilder request = post("/group")
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestData);

        when(groupService.save(any())).thenReturn(new GroupResDto(1L, "title", 3, makeArrToList("월", "화")));

        mockMvc.perform(request)
                .andExpect(status().isCreated())
                .andExpect(jsonPath("id").value(1));

    }

    @Test
    @DisplayName("비정상 등록 - ArgumentResolver Validation")
    void saveTestAbnormal_ArgumentResolverValidation() throws Exception {

        GroupReqDto groupReqDto = new GroupReqDto(null, 4, makeArrToList("월", "화"), 1L);
        String requestData = objectMapper.writeValueAsString(groupReqDto);

        MockHttpServletRequestBuilder request = post("/group")
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestData);

        mockMvc.perform(request)
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("message").value("입력값 타입/내용 오류"))
                .andExpect(jsonPath("errors.title").isNotEmpty())
                .andExpect(jsonPath("errors.repOption").isNotEmpty());

    }

    @Test
    @DisplayName("비정상 등록 - RepInputValidator")
    void saveTestAbnormal_RepInputValidator() throws Exception {

        GroupReqDto groupReqDto = new GroupReqDto("title", 2, makeArrToList(), 1L);
        String requestData = objectMapper.writeValueAsString(groupReqDto);

        MockHttpServletRequestBuilder request = post("/group")
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestData);

        mockMvc.perform(request)
                .andExpect(jsonPath("message").value("입력값 형식 오류"))
                .andExpect(jsonPath("errors.repValue").isNotEmpty());

    }

    @Test
    @DisplayName("비정상 등록 - Resource not found")
    void saveTestAbnormal_ResourceNotFound() throws Exception {

        GroupReqDto groupReqDto = new GroupReqDto("title", 3, makeArrToList("월", "수"), 1L);
        String requestData = objectMapper.writeValueAsString(groupReqDto);

        MockHttpServletRequestBuilder request = post("/group")
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestData);

        when(groupService.save(any())).thenThrow(new ResourceNotFoundException("Resource not found"));

        mockMvc.perform(request)
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("message").value("Resource not found"));

    }

    @Test
    @DisplayName("정상 단건 조회")
    void oneTestNormal() throws Exception {

        MockHttpServletRequestBuilder request = get("/group/1");

        when(groupService.one(any())).thenReturn(new GroupResDto(1L, "group", 1, makeArrToList()));

        mockMvc.perform(request)
                .andExpect(status().isOk())
                .andExpect(jsonPath("id").value(1L))
                .andExpect(content().contentType(MediaTypes.HAL_JSON))
                .andExpect(jsonPath("_links").exists())
                .andExpect(jsonPath("_links.lower-collection").exists());

    }

    @Test
    @DisplayName("비정상 단건 조회")
    void oneTestAbnormal() throws Exception {

        MockHttpServletRequestBuilder request = get("/group/1");
        when(groupService.one(any())).thenThrow(new ResourceNotFoundException());
        mockMvc.perform(request)
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("message").value("Resource not found"));

    }

    @Test
    @DisplayName("정상 수정")
    void updateTestNormal() throws Exception {

        GroupUpdateReqDto groupUpdateReqDto = new GroupUpdateReqDto(1L, "updatedTitle", 1, makeArrToList());
        MockHttpServletRequestBuilder request = put("/group")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(groupUpdateReqDto));

        when(groupService.update(any())).thenReturn(new GroupResDto(groupUpdateReqDto.getId(), groupUpdateReqDto.getTitle(), groupUpdateReqDto.getRepOption(), groupUpdateReqDto.getRepValue()));

        mockMvc.perform(request)
                .andExpect(status().isOk())
                .andExpect(jsonPath("title").value("updatedTitle"))
                .andExpect(jsonPath("_links.self").exists());

    }

    @Test
    @DisplayName("비정상 수정 - ArgumentResolver Validation")
    void updateTestAbnormal_argumentResolverValidation() throws Exception {

        GroupUpdateReqDto groupUpdateReqDto = new GroupUpdateReqDto(1L, "updatedTitle", 0, makeArrToList());
        MockHttpServletRequestBuilder request = put("/group")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(groupUpdateReqDto));

        when(groupService.update(any())).thenReturn(new GroupResDto(groupUpdateReqDto.getId(), groupUpdateReqDto.getTitle(), groupUpdateReqDto.getRepOption(), groupUpdateReqDto.getRepValue()));

        mockMvc.perform(request)
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("message").value("입력값 타입/내용 오류"));

    }

    @Test
    @DisplayName("비정상 수정 - repInputValidator")
    void updateTestAbnormal_repInputValidator() throws Exception {

        GroupUpdateReqDto groupUpdateReqDto = new GroupUpdateReqDto(1L, "updatedTitle", 2, makeArrToList("월", "수", "금"));
        MockHttpServletRequestBuilder request = put("/group")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(groupUpdateReqDto));

        when(groupService.update(any())).thenReturn(new GroupResDto(groupUpdateReqDto.getId(), groupUpdateReqDto.getTitle(), groupUpdateReqDto.getRepOption(), groupUpdateReqDto.getRepValue()));

        mockMvc.perform(request)
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("message").value("입력값 형식 오류"));

    }

    @Test
    @DisplayName("정상 삭제")
    void deleteTestNormal() throws Exception {

        MockHttpServletRequestBuilder request = delete("/group/1");

        doNothing().when(groupService).delete(any());

        mockMvc.perform(request)
                .andExpect(status().isNoContent());

    }

    @Test
    @DisplayName("비정상 삭제")
    void deleteTestAbnormal() throws Exception {

        MockHttpServletRequestBuilder request = delete("/group/1");

        doThrow(ResourceNotFoundException.class).when(groupService).delete(any());

        mockMvc.perform(request)
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("message").value("Resource not found"));

    }

    @Test
    @DisplayName("정상 순수 컬렉션 조회")
    void allTestNormal() throws Exception {

        List<GroupResDto> groups = new ArrayList<>();
        groups.add(new GroupResDto(1L, "title1", 1, makeArrToList()));
        groups.add(new GroupResDto(2L, "title1", 1, makeArrToList()));
        groups.add(new GroupResDto(3L, "title1", 1, makeArrToList()));

        when(groupService.all(any())).thenReturn(groups);

        MockHttpServletRequestBuilder request = get("/groups")
                .param("planId", "1");

        mockMvc.perform(request)
                .andExpect(status().isOk())
                .andExpect(jsonPath("_embedded.groupResDtoList.length()").value(3));

    }

    @Test
    @DisplayName("비정상 순수 컬렉션 조회")
    void allTestAbnormal() throws Exception {

        doThrow(ResourceNotFoundException.class).when(groupService).all(any());

        MockHttpServletRequestBuilder request = get("/groups")
                .param("planId", "1");

        mockMvc.perform(request)
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("message").value("Resource not found"));

    }

    @Test
    @DisplayName("정상 일별 컬렉션 조회")
    void collectionFilteredByDateTestNormal() throws Exception {

        List<GroupResDto> groups = new ArrayList<>();
        groups.add(new GroupResDto(1L, "title1", 1, makeArrToList()));
        groups.add(new GroupResDto(2L, "title1", 1, makeArrToList()));
        groups.add(new GroupResDto(3L, "title1", 1, makeArrToList()));

        when(groupService.all(any(), any())).thenReturn(groups);

        MockHttpServletRequestBuilder request = get("/groups")
                .param("planId", "1")
                .param("dateKey", LocalDate.of(2023, 7, 19).toString());

        mockMvc.perform(request)
                .andExpect(status().isOk())
                .andExpect(jsonPath("_embedded.groupResDtoList.length()").value(3));

    }

    @Test
    @DisplayName("비정상 일별 컬렉션 조회")
    void collectionFilteredByDateTestAbnormal() throws Exception {

        MockHttpServletRequestBuilder request = get("/groups")
                .param("planId", "1")
                .param("dateKey", LocalDate.of(2023, 7, 19).toString());

        doThrow(ResourceNotFoundException.class).when(groupService).all(any(), any());

        mockMvc.perform(request)
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("message").value("Resource not found"));

    }
}
