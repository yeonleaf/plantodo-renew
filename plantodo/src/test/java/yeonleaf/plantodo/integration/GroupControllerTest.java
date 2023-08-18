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
import org.springframework.validation.BindingResult;
import org.springframework.web.context.WebApplicationContext;
import org.springframework.web.filter.CharacterEncodingFilter;
import yeonleaf.plantodo.domain.*;
import yeonleaf.plantodo.dto.GroupReqDto;
import yeonleaf.plantodo.dto.GroupResDto;
import yeonleaf.plantodo.dto.GroupUpdateReqDto;
import yeonleaf.plantodo.exceptions.ApiBindingError;
import yeonleaf.plantodo.exceptions.ApiSimpleError;
import yeonleaf.plantodo.provider.JwtBasicProvider;
import yeonleaf.plantodo.repository.CheckboxRepository;
import yeonleaf.plantodo.repository.GroupRepository;
import yeonleaf.plantodo.repository.MemberRepository;
import yeonleaf.plantodo.repository.PlanRepository;
import yeonleaf.plantodo.service.GroupService;
import yeonleaf.plantodo.controller.GroupController;

import java.time.LocalDate;
import java.util.Arrays;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * target : {@link GroupController}
 * target description : 주기적으로 반복되는 할 일을 생성, 조회, 삭제, 수정하는 API
 *
 * test description : 인터셉터를 포함한 통합 테스트
 *
 *                    모든 요청의 Authorization 헤더에 Jwt 토큰을 포함시켜야 한다.
 *                    임의로 과거 시점의 데이터를 입력하지 않는다.
 *                    현재 시점에서 테스트했을 때 항상 통과하는 테스트만 작성한다.
 *                    (repOption = 2, repOption = 3인 경우는 테스트하지 않는다.)
 *
 *                    삭제 API를 제외하고 정상적으로 API가 수행된 경우 {@link GroupResDto}나 컬렉션 조회의 경우 List<GroupResDto>를 리턴하는지 확인한다.
 *                    query string이나 argument validation을 통과하지 못한 경우 {@link ApiBindingError}를 리턴하는지 확인한다.
 *                    대상이 존재하지 않을 경우 {@link ApiSimpleError}를 리턴하는지 확인한다.
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
    private JwtBasicProvider jwtProvider;

    /**
     * jsonPath로 결과를 검증할 때 한글이 깨지는 현상을 막아주는 세팅 메소드
     */
    @BeforeEach
    void setUp() {
        this.mockMvc = MockMvcBuilders.webAppContextSetup(ctx)
                .addFilter(new CharacterEncodingFilter("UTF-8", true))
                .alwaysDo(print())
                .build();
    }
    

    /**
     * 그룹 등록 API 관련 테스트
     * @see GroupController#save(GroupReqDto, BindingResult)
     *
     * repOption (int) 할 일이 반복되는 양상을 설정하는 옵션
     *           1(매일 반복), 2(기간 반복), 3(요일 반복)
     * repValue  (List<String>) 할 일이 반복되는 주기를 설정하는 옵션
     *
     * @throws Exception mockMvc.perform();
     */
    @Test
    @DisplayName("정상 등록 - repOption = 1 - 리턴받은 GroupResDto에 id가 있는지 확인한다.")
    void saveTestNormal_RepOption1() throws Exception {

        // given
        Member member = memberRepository.save(new Member("test@abc.co.kr", "1d%43aV"));
        Plan plan = planRepository.save(new Plan("plan", LocalDate.of(2023, 7, 18), LocalDate.of(2023, 7, 25), member));

        GroupReqDto groupReqDto = new GroupReqDto("group", 1, List.of(), plan.getId());

        MockHttpServletRequestBuilder request = post("/group")
                .header("Authorization", "Bearer " + jwtProvider.generateToken(member.getId()))
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(groupReqDto));

        // when - then
        mockMvc.perform(request)
                .andExpect(status().isCreated())
                .andExpect(jsonPath("id").isNumber())
                .andExpect(jsonPath("_links").exists())
                .andDo(print());

    }

    @Test
    @DisplayName("비정상 등록 - RepInputValidator로 repOption, repValue를 검증한 결과 통과하지 못한 경우 ApiBindingError를 리턴한다. errors 객체에 repValue 키가 있는지 확인한다.")
    void saveTestAbnormal_RepInputValidatorValidation() throws Exception {

        // given
        GroupReqDto groupReqDto = new GroupReqDto("group", 1, List.of("월"), Long.MAX_VALUE);

        MockHttpServletRequestBuilder request = post("/group")
                .header("Authorization", "Bearer " + jwtProvider.generateToken(Long.MAX_VALUE))
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(groupReqDto));

        // when - then
        mockMvc.perform(request)
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("errors.repValue").exists());

    }


    /**
     * 단건 그룹 조회 API 관련 테스트
     * @see GroupController#one(Long)
     * @throws Exception mockMvc.perform();
     */
    @Test
    @DisplayName("단건 정상 조회 - 조회한 그룹의 내용이 저장한 그룹의 내용과 동일한지 확인한다.")
    void oneTestNormal() throws Exception {

        // given
        Member member = memberRepository.save(new Member("test@abc.co.kr", "1d%43aV"));
        Plan plan = planRepository.save(new Plan("plan", LocalDate.of(2023, 7, 19), LocalDate.of(2023, 7, 31), member));
        Group group = groupRepository.save(new Group(plan, "group", new Repetition(3, "1010100")));

        MockHttpServletRequestBuilder request = get("/group/" + group.getId())
                .header("Authorization", "Bearer " + jwtProvider.generateToken(member.getId()));

        // when - then
        mockMvc.perform(request)
                .andExpect(status().isOk())
                .andExpect(jsonPath("id").value(group.getId()))
                .andExpect(jsonPath("title").value(group.getTitle()))
                .andExpect(jsonPath("repOption").value(group.getRepetition().getRepOption()))
                .andExpect(jsonPath("repValue").value(Matchers.containsInAnyOrder("월", "수", "금")))
                .andExpect(jsonPath("_links.self").exists());

    }

    @Test
    @DisplayName("단건 비정상 조회 - 조회할 그룹이 없는 경우 ApiSimpleError를 리턴한다. message 필드의 내용을 확인한다.")
    void oneTestAbnormal() throws Exception {

        // given
        MockHttpServletRequestBuilder request = get("/group/" + Long.MAX_VALUE)
                .header("Authorization", "Bearer " + jwtProvider.generateToken(Long.MAX_VALUE));

        // when - then
        mockMvc.perform(request)
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("message").value("Resource not found"));

    }


    /**
     * 그룹 수정 API 관련 테스트
     * @see GroupController#update(GroupUpdateReqDto, BindingResult)
     *
     * 수정 후 조회를 다시 해서 예상한 날짜에만 할일이 생성되어 있는지 확인한다.
     *
     * @throws Exception mockMvc.perform();
     */
    @Test
    @DisplayName("정상 수정 - 수정 API 실행 후 group을 기준으로 모든 할일을 조회해 예상한 날짜에만 할일이 생성되어 있는지 확인한다.")
    void updateTestNormal() throws Exception {

        // given
        Member member = memberRepository.save(new Member("test@abc.co.kr", "1d%43aV"));
        Plan plan = planRepository.save(new Plan("plan", LocalDate.of(2023, 7, 24), LocalDate.of(2023, 7, 27), member));
        Group group = groupRepository.save(new Group(plan, "group", new Repetition(3, "1010100")));

        GroupUpdateReqDto groupUpdateReqDto = new GroupUpdateReqDto(group.getId(), "updatedGroup", 3, List.of("화", "목", "토"));
        MockHttpServletRequestBuilder request = put("/group")
                .header("Authorization", "Bearer " + jwtProvider.generateToken(member.getId()))
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(groupUpdateReqDto));

        // when - then
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
    @DisplayName("비정상 수정 - ArgumentResolver Validation - repOption에 0이 들어간 경우 ApiBindingError를 리턴한다. errors 객체에 repOption 키가 있는지 확인한다.")
    void updateTestAbnormal_argumentResolverValidation() throws Exception {

        // given
        Member member = memberRepository.save(new Member("test@abc.co.kr", "1d%43aV"));
        Plan plan = planRepository.save(new Plan("plan", LocalDate.of(2023, 7, 19), LocalDate.of(2023, 7, 31), member));
        Group group = groupRepository.save(new Group(plan, "group", new Repetition(3, "1010100")));

        GroupUpdateReqDto groupUpdateReqDto = new GroupUpdateReqDto(group.getId(), "updatedGroup", 0, List.of("화", "목", "토"));
        MockHttpServletRequestBuilder request = put("/group")
                .header("Authorization", "Bearer " + jwtProvider.generateToken(member.getId()))
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(groupUpdateReqDto));

        // when - then
        mockMvc.perform(request)
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("message").value("입력값 타입/내용 오류"))
                .andExpect(jsonPath("errors.repOption").exists());

    }

    @Test
    @DisplayName("비정상 수정 - RepInputValidator로 repOption과 repValue의 형식을 검증했을 때 통과하지 못하는 경우 ApiBindingError를 리턴한다. " +
            "errors 겍체에 repValue 키가 있는지 확인한다.")
    void updateTestAbnormal_repInputValidator() throws Exception {

        // given
        Member member = memberRepository.save(new Member("test@abc.co.kr", "1d%43aV"));
        Plan plan = planRepository.save(new Plan("plan", LocalDate.of(2023, 7, 19), LocalDate.of(2023, 7, 31), member));
        Group group = groupRepository.save(new Group(plan, "group", new Repetition(3, "1010100")));

        GroupUpdateReqDto groupUpdateReqDto = new GroupUpdateReqDto(group.getId(), "updatedGroup", 1, List.of("화", "목", "토"));
        MockHttpServletRequestBuilder request = put("/group")
                .header("Authorization", "Bearer " + jwtProvider.generateToken(member.getId()))
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(groupUpdateReqDto));

        // when - then
        mockMvc.perform(request)
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("message").value("입력값 형식 오류"))
                .andExpect(jsonPath("errors.repValue").exists());

    }

    @Test
    @DisplayName("비정상 수정 - 수정할 그룹이 없는 경우 ApiSimpleError를 리턴한다. message 필드의 내용을 확인한다.")
    void updateTestAbnormal_resourceNotFound() throws Exception {

        // given
        GroupUpdateReqDto groupUpdateReqDto = new GroupUpdateReqDto(Long.MAX_VALUE, "updatedGroup", 1, List.of());
        MockHttpServletRequestBuilder request = put("/group")
                .header("Authorization", "Bearer " + jwtProvider.generateToken(Long.MAX_VALUE))
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(groupUpdateReqDto));

        // when - then
        mockMvc.perform(request)
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("message").value("Resource not found"));

    }


    /**
     * 그룹 삭제 API 관련 테스트
     * @see GroupController#delete(Long)
     * @throws Exception mockMvc.perform();
     */
    @Test
    @DisplayName("정상 삭제 - 삭제 후 같은 id로 다시 그룹을 조회했을 때 빈 결과값을 리턴하는지 확인한다.")
    void deleteTestNormal() throws Exception {

        // given
        Member member = memberRepository.save(new Member("test@abc.co.kr", "1d%43aV"));
        Plan plan = planRepository.save(new Plan("plan", LocalDate.of(2023, 7, 19), LocalDate.of(2023, 7, 31), member));
        Group group = groupRepository.save(new Group(plan, "group", new Repetition(3, "1010100")));
        Long groupId = group.getId();

        MockHttpServletRequestBuilder request = delete("/group/" + groupId)
                .header("Authorization", "Bearer " + jwtProvider.generateToken(member.getId()));

        // when - then
        mockMvc.perform(request)
                .andExpect(status().isNoContent());

        List<Checkbox> findCheckboxes = checkboxRepository.findByGroupId(groupId);
        assertThat(findCheckboxes).isEmpty();

    }

    @Test
    @DisplayName("비정상 삭제 - 삭제할 그룹이 없는 경우 ApiSimpleError를 리턴한다. message 필드의 내용을 확인한다.")
    void deleteTestAbnormal_resourceNotFound() throws Exception {

        // given
        MockHttpServletRequestBuilder request = delete("/group/" + Long.MAX_VALUE)
                .header("Authorization", "Bearer " + jwtProvider.generateToken(Long.MAX_VALUE));

        // when - then
        mockMvc.perform(request)
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("message").value("Resource not found"));

    }


    /**
     * 그룹 순수 컬렉션 조회 API 관련 테스트
     * 순수 컬렉션 조회 API는 필터링이 걸려 있지 않은 컬렉션 조회 API를 의미한다.
     *
     * @see GroupController#all(Long)
     * 일정을 기준으로 일정과 연관된 모든 그룹을 조회한다.
     *
     * @throws Exception mockMvc.perform();
     */
    @Test
    @DisplayName("정상 순수 컬렉션 조회 - 세 개의 그룹을 저장한 후 일정을 기준으로 그룹 순수 컬렉션을 조회하면 세 개의 그룹이 조회되어야 한다.")
    void allTestNormal() throws Exception {

        // given
        Member member = memberRepository.save(new Member("test@abc.co.kr", "1d%43aV"));
        Long memberId = member.getId();
        Plan plan = planRepository.save(new Plan("plan", LocalDate.of(2023, 7, 19), LocalDate.of(2023, 7, 31), member));
        groupRepository.save(new Group(plan, "group1", new Repetition(3, "1010100")));
        groupRepository.save(new Group(plan, "group1", new Repetition(3, "1010100")));
        groupRepository.save(new Group(plan, "group1", new Repetition(3, "1010100")));

        // when - then
        MockHttpServletRequestBuilder request = get("/groups")
                .header("Authorization", "Bearer " + jwtProvider.generateToken(memberId))
                .param("planId", plan.getId().toString());

        mockMvc.perform(request)
                .andExpect(status().isOk())
                .andExpect(jsonPath("_embedded.groupResDtoList.length()").value(3));

    }

    @Test
    @DisplayName("비정상 순수 컬렉션 조회 - 기준으로 할 일정이 없는 경우 ApiSimpleError를 리턴한다. message 필드의 내용을 확인한다.")
    void allTestAbnormal() throws Exception {

        // given
        MockHttpServletRequestBuilder request = get("/groups")
                .header("Authorization", "Bearer " + jwtProvider.generateToken(1L))
                .param("planId", String.valueOf(Long.MAX_VALUE));

        // when - then
        mockMvc.perform(request)
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("message").value("Resource not found"));

    }


    /**
     * 그룹 일별 컬렉션 조회 API 관련 테스트
     *
     * @see GroupController#all(Long, LocalDate)
     * 그룹 일별 컬렉션 조회 API란 검색일에 일정을 기준으로 할일을 하나라도 가지고 있는 모든 그룹을 조회한 것이다.
     *
     * @throws Exception mockMvc.perform();
     */
    @Test
    @DisplayName("일별 컬렉션 정상 조회 - 저장한 그룹 중 검색일에 할일이 하나라도 생성되어 있는 그룹의 개수가 예측값과 같은지 확인한다.")
    void collectionFilteredByDateTestNormal() throws Exception {

        // given
        Member member = memberRepository.save(new Member("test@abc.co.kr", "1d%43aV"));
        Long memberId = member.getId();
        Plan plan = planRepository.save(new Plan("plan", LocalDate.of(2023, 7, 19), LocalDate.of(2023, 7, 31), member));
        groupService.save(new GroupReqDto("title1", 3, List.of("월", "수", "금"), plan.getId()));
        groupService.save(new GroupReqDto("title2", 3, List.of("월", "일"), plan.getId()));
        groupService.save(new GroupReqDto("title1", 3, List.of("화", "목", "토"), plan.getId()));

        MockHttpServletRequestBuilder request = get("/groups/date")
                .header("Authorization", "Bearer " + jwtProvider.generateToken(memberId))
                .param("planId", String.valueOf(plan.getId()))
                .param("dateKey", LocalDate.of(2023, 7, 19).toString());

        // when - then
        mockMvc.perform(request)
                .andExpect(status().isOk())
                .andExpect(jsonPath("_embedded.groupResDtoList.length()").value(1));

    }

    @Test
    @DisplayName("일별 컬렉션 비정상 조회 - 기준으로 삼을 일정이 없는 경우 ApiSimpleError를 리턴한다. message 필드의 내용을 확인한다.")
    void collectionFilteredByDateTestAbnormal() throws Exception {

        // given
        MockHttpServletRequestBuilder request = get("/groups/date")
                .header("Authorization", "Bearer " + jwtProvider.generateToken(1L))
                .param("planId", String.valueOf(Long.MAX_VALUE))
                .param("dateKey", LocalDate.now().toString());

        // when - then
        mockMvc.perform(request)
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("message").value("Resource not found"));

    }


    /**
     * 그룹 기간 컬렉션 조회 API 관련 테스트
     *
     * @see GroupController#all(Long, LocalDate, LocalDate)
     * 그룹 기간 컬렉션 조회 API란 일정을 기준으로 검색 기간 내에 할일이 하나라도 생성되어 있는 그룹을 모두 조회한 것이다.
     *
     * @throws Exception mockMvc.perform();
     */
    @Test
    @DisplayName("기간 컬렉션 정상 조회 - 일정을 기준으로 검색 기간 내에 할일이 하나라도 생성되어 있는 그룹을 모두 조회한 결과의 개수가 예측값과 같은지 확인한다.")
    void collectionFilteredByDateRangeTestNormal() throws Exception {

        // given
        Member member = memberRepository.save(new Member("test@abc.co.kr", "1d%43aV"));
        Long memberId = member.getId();
        Plan plan = planRepository.save(new Plan("plan", LocalDate.of(2023, 7, 19), LocalDate.of(2023, 7, 31), member));
        Long planId = plan.getId();
        groupService.save(new GroupReqDto("title1", 3, List.of("화"), planId));
        groupService.save(new GroupReqDto("title2", 2, List.of("2"), planId));
        groupService.save(new GroupReqDto("title3", 2, List.of("3"), planId));

        LocalDate searchStart = LocalDate.of(2023, 7, 26);
        LocalDate searchEnd = LocalDate.of(2023, 7, 29);

        MockHttpServletRequestBuilder request = get("/groups/range")
                .header("Authorization", "Bearer " + jwtProvider.generateToken(memberId))
                .param("planId", String.valueOf(planId))
                .param("searchStart", searchStart.toString())
                .param("searchEnd", searchEnd.toString());

        // when - then
        mockMvc.perform(request)
                .andExpect(status().isOk())
                .andExpect(jsonPath("_embedded.groupResDtoList.length()").value(2));

    }

    @Test
    @DisplayName("기간 컬렉션 비정상 조회 - 검색 시작일이 검색 종료일보다 늦는 경우 ApiBindingError를 리턴한다. " +
            "errors 객체에 searchStart, searchEnd 키가 있는지 확인한다.")
    void collectionFilteredByDateRangeTestAbnormal() throws Exception {

        // given
        MockHttpServletRequestBuilder request = get("/groups/range")
                .header("Authorization", "Bearer " + jwtProvider.generateToken(1L))
                .param("planId", "1")
                .param("searchStart", LocalDate.of(2023, 7, 16).toString())
                .param("searchEnd", LocalDate.of(2023, 7, 13).toString());

        // when - then
        mockMvc.perform(request)
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("errors.searchStart").exists())
                .andExpect(jsonPath("errors.searchEnd").exists());

    }

}
