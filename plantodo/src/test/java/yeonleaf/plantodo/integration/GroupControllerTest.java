package yeonleaf.plantodo.integration;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.transaction.Transactional;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.hateoas.MediaTypes;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;
import org.springframework.web.filter.CharacterEncodingFilter;
import yeonleaf.plantodo.domain.Group;
import yeonleaf.plantodo.domain.Member;
import yeonleaf.plantodo.domain.Plan;
import yeonleaf.plantodo.domain.Repetition;
import yeonleaf.plantodo.dto.GroupReqDto;
import yeonleaf.plantodo.repository.GroupRepository;
import yeonleaf.plantodo.repository.MemberRepository;
import yeonleaf.plantodo.repository.PlanRepository;

import java.time.LocalDate;
import java.util.Arrays;
import java.util.List;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
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
    private GroupRepository groupRepository;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private WebApplicationContext ctx;

    @BeforeEach
    void setUp() {
        this.mockMvc = MockMvcBuilders.webAppContextSetup(ctx)
                .addFilter(new CharacterEncodingFilter("UTF-8", true))
                .alwaysDo(print())
                .build();
    }

    private List<String> makeArrToList(String... target) {
        return Arrays.asList(target);
    }

    @Test
    @DisplayName("정상 등록 - repOption = 1L")
    void saveTestNormal_RepOption1L() throws Exception {

        Member member = memberRepository.save(new Member("test@abc.co.kr", "1d%43aV"));
        Plan plan = planRepository.save(new Plan("plan", LocalDate.of(2023, 7, 18), LocalDate.of(2023, 7, 25), member));

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

        GroupReqDto groupReqDto = new GroupReqDto("group", 1L, makeArrToList("월"), Long.MAX_VALUE);

        MockHttpServletRequestBuilder request = post("/group")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(groupReqDto));

        mockMvc.perform(request)
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("message").value("입력값 형식 오류"));

    }

    @Test
    @DisplayName("단건 정상 조회")
    void oneTestNormal() throws Exception {

        Member member = memberRepository.save(new Member("test@abc.co.kr", "1d%43aV"));
        Plan plan = planRepository.save(new Plan("plan", LocalDate.now(), LocalDate.now().plusDays(3), member));
        Group group = groupRepository.save(new Group(plan, "group", new Repetition(3L, "1010100")));

        MockHttpServletRequestBuilder request = get("/group/" + group.getId());

        mockMvc.perform(request)
                .andExpect(status().isOk())
                .andExpect(jsonPath("id").value(group.getId()))
                .andExpect(jsonPath("title").value(group.getTitle()))
                .andExpect(jsonPath("repOption").value(group.getRepetition().getRepOption()))
                .andExpect(jsonPath("repValue").value(Matchers.containsInAnyOrder("월", "수", "금")))
                .andExpect(jsonPath("_links.self").exists());

    }

    @Test
    @DisplayName("단건 비정상 조회")
    void oneTestAbnormal() throws Exception {

        MockHttpServletRequestBuilder request = get("/group/" + Long.MAX_VALUE);

        mockMvc.perform(request)
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("message").value("Resource not found"));

    }

}
