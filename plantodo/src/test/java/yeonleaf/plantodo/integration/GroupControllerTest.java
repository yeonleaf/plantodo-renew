package yeonleaf.plantodo.integration;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.transaction.Transactional;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.hateoas.MediaTypes;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;
import yeonleaf.plantodo.domain.Member;
import yeonleaf.plantodo.domain.Plan;
import yeonleaf.plantodo.dto.GroupReqDto;
import yeonleaf.plantodo.repository.MemberRepository;
import yeonleaf.plantodo.repository.PlanRepository;

import java.time.LocalDate;
import java.util.Arrays;
import java.util.List;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * repOption = 2L, repOption = 3L 케이스는 테스트 불가 (현재 시점을 기준으로 테스트해야 하기 때문에 멱등을 보장할 수 없음)
 */
@Transactional
@SpringBootTest
@AutoConfigureMockMvc
public class GroupControllerTest {

    @Autowired
    private MemberRepository memberRepository;

    @Autowired
    private PlanRepository planRepository;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private MockMvc mockMvc;

    private List<String> makeArrToList(String... target) {
        return Arrays.asList(target);
    }

    @Test
    @DisplayName("정상 등록 - repOption = 1L")
    void saveTestNormal_RepOption1L() throws Exception {

        Member member = memberRepository.save(new Member("test@abc.co.kr", "1d%43aV"));
        Plan plan = planRepository.save(new Plan("plan", LocalDate.now(), LocalDate.now().plusDays(7), member));

        GroupReqDto groupReqDto = new GroupReqDto("group", 1L, makeArrToList(), plan.getId());

        MockHttpServletRequestBuilder request = post("/group")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(groupReqDto));

        mockMvc.perform(request)
                .andExpect(status().isCreated())
                .andExpect(content().contentType(MediaTypes.HAL_JSON))
                .andExpect(jsonPath("id").isNumber())
                .andExpect(jsonPath("uncheckedCnt").value(8))
                .andExpect(jsonPath("_links").exists())
                .andDo(print());

    }

    @Test
    @DisplayName("비정상 등록 - RepInputValidator Validation")
    void saveTestAbnormal_RepInputValidatorValidation() throws Exception {

        Member member = memberRepository.save(new Member("test@abc.co.kr", "1d%43aV"));
        Plan plan = planRepository.save(new Plan("plan", LocalDate.now(), LocalDate.now().plusDays(3), member));

        GroupReqDto groupReqDto = new GroupReqDto("group", 1L, makeArrToList("월"), plan.getId());

        MockHttpServletRequestBuilder request = post("/group")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(groupReqDto));

        mockMvc.perform(request)
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("message").value("입력값 형식 오류"));

    }

}
