package yeonleaf.plantodo.unit.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
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
import yeonleaf.plantodo.controller.GroupController;
import yeonleaf.plantodo.dto.GroupReqDto;
import yeonleaf.plantodo.dto.GroupResDto;
import yeonleaf.plantodo.service.GroupService;

import java.util.Arrays;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

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

        GroupReqDto groupReqDto = new GroupReqDto("title", 3L, makeArrToList("월", "화"));
        String requestData = objectMapper.writeValueAsString(groupReqDto);

        MockHttpServletRequestBuilder request = post("/group")
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestData);

        when(groupService.save(any(), any())).thenReturn(new GroupResDto(1L, "title", 3L, makeArrToList("월", "화")));

        mockMvc.perform(request)
                .andExpect(status().isCreated());

    }

    @Test
    @DisplayName("비정상 등록 - ArgumentResolver Validation")
    void saveTestAbnormal() throws Exception {

        GroupReqDto groupReqDto = new GroupReqDto(null, 4L, makeArrToList("월", "화"));
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
    void saveTestRepInputValidator() throws Exception {
        GroupReqDto groupReqDto = new GroupReqDto("title", 3L, makeArrToList());
        String requestData = objectMapper.writeValueAsString(groupReqDto);

        MockHttpServletRequestBuilder request = post("/group")
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestData);

        mockMvc.perform(request)
                .andExpect(jsonPath("message").value("입력값 형식 오류"))
                .andExpect(jsonPath("errors.repValue").isNotEmpty());
    }

}
