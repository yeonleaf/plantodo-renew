package yeonleaf.plantodo.unit.controller.member;

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
import yeonleaf.plantodo.provider.JwtProvider;
import yeonleaf.plantodo.service.MemberService;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(MemberController.class)
public class MemberControllerDeleteUnitTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private JwtProvider jwtProvider;

    @MockBean
    private MemberService memberService;

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
        mockMvc.perform(request)
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("message").value("Resource not found"));

    }

}
