package yeonleaf.plantodo.unit.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.jsonwebtoken.JwtBuilder;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;
import yeonleaf.plantodo.controller.MemberController;
import yeonleaf.plantodo.domain.Member;
import yeonleaf.plantodo.dto.MemberReqDto;
import yeonleaf.plantodo.dto.MemberResDto;
import yeonleaf.plantodo.exceptions.ApiSimpleError;
import yeonleaf.plantodo.service.MemberService;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(MemberController.class)
public class MemberControllerDeleteUnitTest {
    @Autowired
    private MockMvc mockMvc;

    @MockBean
    JwtBuilder jwtBuilder;

    @MockBean
    MemberService memberService;

    private final ObjectMapper objectMapper = new ObjectMapper();

    private MemberResDto joinOneMember(MemberReqDto memberReqDto) throws Exception {
        String requestData = objectMapper.writeValueAsString(memberReqDto);

        MockHttpServletRequestBuilder request = post("/member")
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestData);

        MvcResult mvcResult = mockMvc.perform(request)
                .andReturn();
        String resultString = mvcResult.getResponse().getContentAsString();
        return objectMapper.readValue(resultString, MemberResDto.class);
    }

    @Test
    @DisplayName("정상 삭제")
    void deleteTestNormal() throws Exception {
        Member member = new Member("test@abc.co.kr", "18zdf@$d");
        member.setId(1L);

        when(memberService.findById(any())).thenReturn(Optional.of(member));
        doNothing().when(memberService).delete(member);

        MockHttpServletRequestBuilder request = delete("/member/1");
        mockMvc.perform(request)
                .andExpect(status().isNoContent());
    }

    @Test
    @DisplayName("비정상 삭제 - Resource not found")
    void deleteTestAbnormalResourceNotFound() throws Exception {
        Member member = new Member("test@abc.co.kr", "18zdf@$d");
        member.setId(1L);

        when(memberService.findById(any())).thenReturn(Optional.empty());
        doNothing().when(memberService).delete(member);

        MockHttpServletRequestBuilder request = delete("/member/1");
        MvcResult mvcResult = mockMvc.perform(request)
                .andExpect(status().isNotFound())
                .andReturn();

        ApiSimpleError apiSimpleError = objectMapper.readValue(mvcResult.getResponse().getContentAsString(), ApiSimpleError.class);
        assertThat(apiSimpleError.getMessage()).isEqualTo("Resource not found");
    }
}
