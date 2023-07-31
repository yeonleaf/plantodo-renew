package yeonleaf.plantodo.integration;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.transaction.Transactional;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;
import org.springframework.web.filter.CharacterEncodingFilter;
import yeonleaf.plantodo.domain.*;
import yeonleaf.plantodo.dto.GroupReqDto;
import yeonleaf.plantodo.dto.GroupResDto;
import yeonleaf.plantodo.dto.GroupUpdateReqDto;
import yeonleaf.plantodo.exceptions.ResourceNotFoundException;
import yeonleaf.plantodo.provider.JwtBasicProvider;
import yeonleaf.plantodo.provider.JwtProvider;
import yeonleaf.plantodo.repository.CheckboxRepository;
import yeonleaf.plantodo.repository.GroupRepository;
import yeonleaf.plantodo.repository.MemberRepository;
import yeonleaf.plantodo.repository.PlanRepository;
import yeonleaf.plantodo.service.GroupService;

import javax.crypto.SecretKey;
import java.time.LocalDate;
import java.util.Arrays;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * repOption = 2, repOption = 2 케이스는 테스트 불가 (현재 시점을 기준으로 테스트해야 하기 때문에 멱등을 보장할 수 없음)
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
    private CheckboxRepository checkboxRepository;

    @Autowired
    private GroupService groupService;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private WebApplicationContext ctx;

    @Autowired
    private SecretKey jwtSecretKey;

    @Autowired
    private JwtBasicProvider jwtProvider;

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
    @DisplayName("정상 등록 - repOption = 1")
    void saveTestNormal_RepOption1() throws Exception {

        Member member = memberRepository.save(new Member("test@abc.co.kr", "1d%43aV"));
        Plan plan = planRepository.save(new Plan("plan", LocalDate.of(2023, 7, 18), LocalDate.of(2023, 7, 25), member));

        GroupReqDto groupReqDto = new GroupReqDto("group", 1, makeArrToList(), plan.getId());

        MockHttpServletRequestBuilder request = post("/group")
                .header("Authorization", "Bearer " + jwtProvider.generateToken(member.getId()))
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(groupReqDto));

        mockMvc.perform(request)
                .andExpect(status().isCreated())
                .andExpect(jsonPath("id").isNumber())
                .andExpect(jsonPath("_links").exists())
                .andDo(print());

    }

    @Test
    @DisplayName("비정상 등록 - RepInputValidator Validation")
    void saveTestAbnormal_RepInputValidatorValidation() throws Exception {

        GroupReqDto groupReqDto = new GroupReqDto("group", 1, makeArrToList("월"), Long.MAX_VALUE);

        MockHttpServletRequestBuilder request = post("/group")
                .header("Authorization", "Bearer " + jwtProvider.generateToken(Long.MAX_VALUE))
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
        Group group = groupRepository.save(new Group(plan, "group", new Repetition(3, "1010100")));

        MockHttpServletRequestBuilder request = get("/group/" + group.getId())
                .header("Authorization", "Bearer " + jwtProvider.generateToken(member.getId()));

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

        MockHttpServletRequestBuilder request = get("/group/" + Long.MAX_VALUE)
                .header("Authorization", "Bearer " + jwtProvider.generateToken(Long.MAX_VALUE));

        mockMvc.perform(request)
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("message").value("Resource not found"));

    }

    @Test
    @DisplayName("정상 수정")
    void updateTestNormal() throws Exception {

        Member member = memberRepository.save(new Member("test@abc.co.kr", "1d%43aV"));
        Plan plan = planRepository.save(new Plan("plan", LocalDate.of(2023, 7, 24), LocalDate.of(2023, 7, 27), member));
        Group group = groupRepository.save(new Group(plan, "group", new Repetition(3, "1010100")));

        GroupUpdateReqDto groupUpdateReqDto = new GroupUpdateReqDto(group.getId(), "updatedGroup", 3, makeArrToList("화", "목", "토"));
        MockHttpServletRequestBuilder request = put("/group")
                .header("Authorization", "Bearer " + jwtProvider.generateToken(member.getId()))
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(groupUpdateReqDto));

        mockMvc.perform(request)
                .andExpect(status().isOk())
                .andExpect(jsonPath("title").value("updatedGroup"))
                .andExpect(jsonPath("repOption").value(3))
                .andExpect(jsonPath("repValue").value(Matchers.containsInAnyOrder("화", "목", "토")));

        List<LocalDate> dateResult = checkboxRepository.findByGroupId(group.getId()).stream().map(Checkbox::getDate).toList();
        assertThat(dateResult).containsOnly(
                LocalDate.of(2023, 7, 25),
                LocalDate.of(2023, 7, 27)
        );

    }

    @Test
    @DisplayName("비정상 수정 - ArgumentResolver Validation")
    void updateTestAbnormal_argumentResolverValidation() throws Exception {

        Member member = memberRepository.save(new Member("test@abc.co.kr", "1d%43aV"));
        Plan plan = planRepository.save(new Plan("plan", LocalDate.now(), LocalDate.now().plusDays(3), member));
        Group group = groupRepository.save(new Group(plan, "group", new Repetition(3, "1010100")));

        GroupUpdateReqDto groupUpdateReqDto = new GroupUpdateReqDto(group.getId(), "updatedGroup", 0, makeArrToList("화", "목", "토"));
        MockHttpServletRequestBuilder request = put("/group")
                .header("Authorization", "Bearer " + jwtProvider.generateToken(member.getId()))
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(groupUpdateReqDto));

        mockMvc.perform(request)
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("message").value("입력값 타입/내용 오류"))
                .andExpect(jsonPath("errors.repOption").exists());

    }

    @Test
    @DisplayName("비정상 수정 - RepInputValidator")
    void updateTestAbnormal_repInputValidator() throws Exception {

        Member member = memberRepository.save(new Member("test@abc.co.kr", "1d%43aV"));
        Plan plan = planRepository.save(new Plan("plan", LocalDate.now(), LocalDate.now().plusDays(3), member));
        Group group = groupRepository.save(new Group(plan, "group", new Repetition(3, "1010100")));

        GroupUpdateReqDto groupUpdateReqDto = new GroupUpdateReqDto(group.getId(), "updatedGroup", 1, makeArrToList("화", "목", "토"));
        MockHttpServletRequestBuilder request = put("/group")
                .header("Authorization", "Bearer " + jwtProvider.generateToken(member.getId()))
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(groupUpdateReqDto));

        mockMvc.perform(request)
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("message").value("입력값 형식 오류"))
                .andExpect(jsonPath("errors.repValue").exists());

    }

    @Test
    @DisplayName("비정상 수정 - Resource not found")
    void updateTestAbnormal_resourceNotFound() throws Exception {

        GroupUpdateReqDto groupUpdateReqDto = new GroupUpdateReqDto(Long.MAX_VALUE, "updatedGroup", 1, makeArrToList());
        MockHttpServletRequestBuilder request = put("/group")
                .header("Authorization", "Bearer " + jwtProvider.generateToken(Long.MAX_VALUE))
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(groupUpdateReqDto));

        mockMvc.perform(request)
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("message").value("Resource not found"));

    }

    @Test
    @DisplayName("정상 삭제")
    void deleteTestNormal() throws Exception {

        Member member = memberRepository.save(new Member("test@abc.co.kr", "1d%43aV"));
        Plan plan = planRepository.save(new Plan("plan", LocalDate.now(), LocalDate.now().plusDays(3), member));
        Group group = groupRepository.save(new Group(plan, "group", new Repetition(3, "1010100")));
        Long groupId = group.getId();

        MockHttpServletRequestBuilder request = delete("/group/" + groupId)
                .header("Authorization", "Bearer " + jwtProvider.generateToken(member.getId()));

        mockMvc.perform(request)
                .andExpect(status().isNoContent());

        List<Checkbox> findCheckboxes = checkboxRepository.findByGroupId(groupId);
        assertThat(findCheckboxes).isEmpty();

    }

    @Test
    @DisplayName("비정상 삭제 - Resource not found")
    void deleteTestAbnormal_resourceNotFound() throws Exception {

        MockHttpServletRequestBuilder request = delete("/group/" + Long.MAX_VALUE)
                .header("Authorization", "Bearer " + jwtProvider.generateToken(Long.MAX_VALUE));

        mockMvc.perform(request)
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("message").value("Resource not found"));

    }

    @Test
    @DisplayName("정상 순수 컬렉션 조회")
    void allTestNormal() throws Exception {

        Member member = memberRepository.save(new Member("test@abc.co.kr", "1d%43aV"));
        Plan plan = planRepository.save(new Plan("plan", LocalDate.now(), LocalDate.now().plusDays(3), member));
        groupRepository.save(new Group(plan, "group1", new Repetition(3, "1010100")));
        groupRepository.save(new Group(plan, "group1", new Repetition(3, "1010100")));
        groupRepository.save(new Group(plan, "group1", new Repetition(3, "1010100")));

        MockHttpServletRequestBuilder request = get("/groups")
                .param("planId", plan.getId().toString());

        mockMvc.perform(request)
                .andExpect(status().isOk())
                .andExpect(jsonPath("_embedded.groupResDtoList.length()").value(3));

    }

    @Test
    @DisplayName("비정상 순수 컬렉션 조회 - Resource not found")
    void allTestAbnormal() throws Exception {

        MockHttpServletRequestBuilder request = get("/groups")
                .param("planId", String.valueOf(Long.MAX_VALUE));

        mockMvc.perform(request)
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("message").value("Resource not found"));

    }

    @Test
    @DisplayName("일별 컬렉션 정상 조회")
    void collectionFilteredByDateTestNormal() throws Exception {

        Member member = memberRepository.save(new Member("test@abc.co.kr", "1d%43aV"));
        Plan plan = planRepository.save(new Plan("plan", LocalDate.now(), LocalDate.now().plusDays(3), member));
        groupService.save(new GroupReqDto("title1", 3, makeArrToList("월", "수", "금"), plan.getId()));
        groupService.save(new GroupReqDto("title2", 3, makeArrToList("월", "일"), plan.getId()));
        groupService.save(new GroupReqDto("title1", 3, makeArrToList("화", "목", "토"), plan.getId()));

        MockHttpServletRequestBuilder request = get("/groups")
                .param("planId", String.valueOf(plan.getId()))
                .param("dateKey", LocalDate.now().toString());

        mockMvc.perform(request)
                .andExpect(status().isOk())
                .andExpect(jsonPath("_embedded.groupResDtoList.length()").value(2));

    }

    @Test
    @DisplayName("일별 컬렉션 비정상 조회")
    void collectionFilteredByDateTestAbnormal() throws Exception {

        MockHttpServletRequestBuilder request = get("/groups")
                .param("planId", String.valueOf(Long.MAX_VALUE))
                .param("dateKey", LocalDate.now().toString());

        mockMvc.perform(request)
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("message").value("Resource not found"));

    }

}
