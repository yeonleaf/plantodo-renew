package yeonleaf.plantodo.integration;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.transaction.Transactional;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;
import yeonleaf.plantodo.domain.Member;
import yeonleaf.plantodo.dto.CheckboxReqDto;
import yeonleaf.plantodo.dto.PlanReqDto;
import yeonleaf.plantodo.dto.PlanResDto;
import yeonleaf.plantodo.repository.MemberRepository;
import yeonleaf.plantodo.service.PlanService;

import java.time.LocalDate;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@Transactional
@SpringBootTest
@AutoConfigureMockMvc
public class CheckboxControllerTest {

    @Autowired
    private MemberRepository memberRepository;

    @Autowired
    private PlanService planService;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private MockMvc mockMvc;

    @Test
    @DisplayName("정상 저장")
    void saveTestNormal() throws Exception {

        Member member = memberRepository.save(new Member("test@abc.co.kr", "e1Df%4sa"));
        PlanResDto planResDto = planService.save(new PlanReqDto("plan", LocalDate.now(), LocalDate.now().plusDays(3), member.getId()));

        CheckboxReqDto checkboxReqDto = new CheckboxReqDto("checkbox", planResDto.getId(), LocalDate.now());

        MockHttpServletRequestBuilder request = post("/checkbox")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(checkboxReqDto));

        mockMvc.perform(request)
                .andExpect(status().isCreated())
                .andExpect(jsonPath("id").exists())
                .andExpect(jsonPath("_links").exists())
                .andDo(print());

    }

}
