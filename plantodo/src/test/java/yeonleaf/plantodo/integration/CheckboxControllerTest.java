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
import org.springframework.validation.BindingResult;
import yeonleaf.plantodo.domain.Checkbox;
import yeonleaf.plantodo.domain.Group;
import yeonleaf.plantodo.domain.Member;
import yeonleaf.plantodo.domain.Plan;
import yeonleaf.plantodo.dto.*;
import yeonleaf.plantodo.exceptions.ApiBindingError;
import yeonleaf.plantodo.exceptions.ApiSimpleError;
import yeonleaf.plantodo.exceptions.ResourceNotFoundException;
import yeonleaf.plantodo.provider.JwtBasicProvider;
import yeonleaf.plantodo.repository.CheckboxRepository;
import yeonleaf.plantodo.repository.MemberRepository;
import yeonleaf.plantodo.service.CheckboxService;
import yeonleaf.plantodo.service.GroupService;
import yeonleaf.plantodo.service.PlanService;
import yeonleaf.plantodo.controller.CheckboxController;

import java.time.LocalDate;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * target : {@link CheckboxController}의 모든 메소드
 * target description : {@link CheckboxReqDto}나 {@link CheckboxUpdateReqDto}를 받아 {@link Checkbox}에 대한 등록, 수정, 조회, 삭제, 상태 변경을 수행하는 API
 *                      조회 API의 경우 조회 기준(plan 기준 조회, group 기준 조회)과 필터링(날짜, 기간)을 제공함
 *
 * test description : 인터셉터까지 포함하는 통합 테스트
 *                    모든 요청의 Authorization 헤더에 Jwt 토큰을 포함시켜야 한다.
 *
 *                    삭제 API를 제외하고 정상적으로 요청이 수행된 경우 {@link CheckboxResDto} 혹은 List<CheckboxResDto>를 리턴하는지 확인
 *                    쿼리 스트링이나 argument에 대한 validation을 통과하지 못한 경우 {@link ApiBindingError}를 리턴하는지 확인
 *                    기준(plan, group)을 조회할 수 없는 경우 {@link ApiSimpleError}를 리턴하는지 확인
 *
 * ※ 용어 정리
 * 일일 할일 : 할일 그룹 없이 일정에 직접 추가되는 반복되지 않는 할일
 * 그룹 할일 : 할일 그룹에 속한 반복되는 할일
 * 일일 할일과 그룹 할일은 같은 할일 {@link Checkbox} 객체로, {@link Plan}에 직접 속해 있으면 일일 할일, {@link Group}이 생성하고 관리하면 그룹 할일로 분류한다.
 */
@Transactional
@SpringBootTest
@AutoConfigureMockMvc
public class CheckboxControllerTest {

    @Autowired
    private MemberRepository memberRepository;

    @Autowired
    private CheckboxRepository checkboxRepository;

    @Autowired
    private PlanService planService;

    @Autowired
    private CheckboxService checkboxService;

    @Autowired
    private GroupService groupService;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private JwtBasicProvider jwtProvider;

    /**
     * 할일 등록 API 관련 테스트
     * @see CheckboxController#save(CheckboxReqDto, BindingResult)
     * @throws Exception mockMvc.perform();
     */
    @Test
    @DisplayName("정상 등록 - 리턴된 MemberResDto 객체에 id값이 있는지 확인한다.")
    void saveTestNormal() throws Exception {

        // given
        Member member = memberRepository.save(new Member("test@abc.co.kr", "e1Df%4sa"));
        PlanResDto planResDto = planService.save(new PlanReqDto("plan", LocalDate.now(), LocalDate.now().plusDays(3), member.getId()));

        CheckboxReqDto checkboxReqDto = new CheckboxReqDto("checkbox", planResDto.getId(), LocalDate.now());

        MockHttpServletRequestBuilder request = post("/checkbox")
                .header("Authorization", "Bearer " + jwtProvider.generateToken(member.getId()))
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(checkboxReqDto));

        // when - then
        mockMvc.perform(request)
                .andExpect(status().isCreated())
                .andExpect(jsonPath("id").exists())
                .andExpect(jsonPath("_links").exists())
                .andDo(print());

    }


    /**
     * 할일 단건 조회 API 관련 테스트
     * @see CheckboxController#one(Long)
     * @throws Exception mockMvc.perform();
     */
    @Test
    @DisplayName("단건 정상 조회 - 조회한 할일의 내용이 DB에 저장된 할일의 내용과 동일한지 확인한다.")
    void oneTestNormal() throws Exception {

        // given
        Member member = memberRepository.save(new Member("test@abc.co.kr", "e1Df%4sa"));
        PlanResDto planResDto = planService.save(new PlanReqDto("plan", LocalDate.now(), LocalDate.now().plusDays(3), member.getId()));
        CheckboxResDto checkboxResDto = checkboxService.save(new CheckboxReqDto("checkbox", planResDto.getId(), LocalDate.now()));

        MockHttpServletRequestBuilder request = get("/checkbox/" + checkboxResDto.getId())
                .header("Authorization", "Bearer " + jwtProvider.generateToken(member.getId()));

        // when - then
        mockMvc.perform(request)
                .andExpect(status().isOk())
                .andExpect(jsonPath("id").value(checkboxResDto.getId()))
                .andExpect(jsonPath("title").value(checkboxResDto.getTitle()))
                .andExpect(jsonPath("checked").value(checkboxResDto.isChecked()));

    }

    @Test
    @DisplayName("단건 비정상 조회 - 조회할 할일이 없다면 ApiSimpleError의 message 내용이 Resource not found인지 확인한다.")
    void oneTestAbnormal() throws Exception {

        // given
        MockHttpServletRequestBuilder request = get("/checkbox/" + Long.MAX_VALUE)
                .header("Authorization", "Bearer " + jwtProvider.generateToken(Long.MAX_VALUE));

        // when - then
        mockMvc.perform(request)
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("message").value("Resource not found"));

    }


    /**
     * 할일 수정 API 관련 테스트
     * 할일의 종류(일일, 그룹)와 상관 없이 로직은 동일함
     * @see CheckboxController#update(CheckboxUpdateReqDto, BindingResult)
     * @throws Exception mockMvc.perform();
     */
    @Test
    @DisplayName("정상 수정 - 일일 할일 - 할일 수정 후 다시 조회한 결과의 타이틀이 정상적으로 수정되었는지 확인한다.")
    void updateTestNormal_checkboxNotInGroup() throws Exception {

        // given
        Member member = memberRepository.save(new Member("test@abc.co.kr", "e1Df%4sa"));
        PlanResDto planResDto = planService.save(new PlanReqDto("plan", LocalDate.now(), LocalDate.now().plusDays(3), member.getId()));
        CheckboxResDto checkboxResDto = checkboxService.save(new CheckboxReqDto("title", planResDto.getId(), LocalDate.now()));

        CheckboxUpdateReqDto checkboxUpdateReqDto = new CheckboxUpdateReqDto(checkboxResDto.getId(), "updatedTitle");
        MockHttpServletRequestBuilder request = put("/checkbox")
                .header("Authorization", "Bearer " + jwtProvider.generateToken(member.getId()))
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(checkboxUpdateReqDto));

        // when - then
        mockMvc.perform(request)
                .andExpect(status().isOk());

        Checkbox findCheckbox = checkboxRepository.findById(checkboxResDto.getId()).orElseThrow(ResourceNotFoundException::new);
        assertThat(findCheckbox.getTitle()).isEqualTo("updatedTitle");

    }

    /**
     * repValue 입력을 위한 보조 메소드
     */
    private List<String> makeArrList(String... target) {
        return Arrays.asList(target);
    }

    @Test
    @DisplayName("정상 수정 - 그룹 할일 - 할일 수정 후 다시 조회한 결과의 title이 정상적으로 변경되었는지 확인한다.")
    void updateTestNormal_checkboxInGroup() throws Exception {

        // given
        Member member = memberRepository.save(new Member("test@abc.co.kr", "e1Df%4sa"));
        PlanResDto planResDto = planService.save(new PlanReqDto("plan", LocalDate.now(), LocalDate.now().plusDays(3), member.getId()));
        GroupResDto groupResDto = groupService.save(new GroupReqDto("title", 1, makeArrList(), planResDto.getId()));
        Checkbox checkbox = checkboxRepository.findByGroupId(groupResDto.getId()).get(0);

        CheckboxUpdateReqDto checkboxUpdateReqDto = new CheckboxUpdateReqDto(checkbox.getId(), "updatedTitle");
        MockHttpServletRequestBuilder request = put("/checkbox")
                .header("Authorization", "Bearer " + jwtProvider.generateToken(member.getId()))
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(checkboxUpdateReqDto));

        // when - then
        mockMvc.perform(request)
                .andExpect(status().isOk());

        Checkbox findCheckbox = checkboxRepository.findById(checkbox.getId()).orElseThrow(ResourceNotFoundException::new);
        assertThat(findCheckbox.getTitle()).isEqualTo("updatedTitle");

    }

    @Test
    @DisplayName("비정상 수정 - id에 null값이 들어가는 경우 ApiBindingError를 반환한다. errors 객체에 id 필드가 있는지 확인한다.")
    void updateTestAbnormal_argumentResolverValidation() throws Exception {

        // given
        CheckboxUpdateReqDto checkboxUpdateReqDto = new CheckboxUpdateReqDto(null, "updatedTitle");
        MockHttpServletRequestBuilder request = put("/checkbox")
                .header("Authorization", "Bearer " + jwtProvider.generateToken(null))
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(checkboxUpdateReqDto));

        // when - then
        mockMvc.perform(request)
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("errors.id").exists());

    }

    @Test
    @DisplayName("비정상 수정 - 수정할 할일이 DB에 없는 경우 ApiSimpleError를 리턴한다. message 필드의 내용을 확인한다.")
    void updateTestAbnormal_resourceNotFound() throws Exception {

        // given
        CheckboxUpdateReqDto checkboxUpdateReqDto = new CheckboxUpdateReqDto(Long.MAX_VALUE, "updatedTitle");

        MockHttpServletRequestBuilder request = put("/checkbox")
                .header("Authorization", "Bearer " + jwtProvider.generateToken(Long.MAX_VALUE))
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(checkboxUpdateReqDto));

        // when - then
        mockMvc.perform(request)
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("message").value("Resource not found"));

    }


    /**
     * 할일 삭제 API 관련 테스트
     * 할일의 종류(일일, 그룹)와 관계없이 삭제 로직은 동일
     * @see CheckboxController#delete(Long)
     * @throws Exception mockMvc.perform();
     */
    @Test
    @DisplayName("정상 삭제 - 그룹 할일 - 삭제 후 같은 id로 조회하면 빈 결과값을 반환해야 한다.")
    void deleteTestNormal_groupCheckbox() throws Exception {

        // given
        Member member = memberRepository.save(new Member("test@abc.co.kr", "e1Df%4sa"));
        PlanResDto planResDto = planService.save(new PlanReqDto("plan", LocalDate.now(), LocalDate.now().plusDays(3), member.getId()));
        GroupResDto groupResDto = groupService.save(new GroupReqDto("title", 1, makeArrList(), planResDto.getId()));

        Checkbox checkbox = checkboxRepository.findByGroupId(groupResDto.getId()).get(0);
        Long checkboxId = checkbox.getId();

        MockHttpServletRequestBuilder request = delete("/checkbox/" + checkboxId)
                .header("Authorization", "Bearer " + jwtProvider.generateToken(member.getId()));

        // when - then
        mockMvc.perform(request)
                .andExpect(status().isNoContent());

        Optional<Checkbox> findCheckbox = checkboxRepository.findById(checkboxId);
        assertThat(findCheckbox).isEmpty();

    }

    @Test
    @DisplayName("정상 삭제 - 일일 할일 - 삭제 후 같은 id로 조회하면 빈 결과값을 반환해야 한다.")
    void deleteTestNormal_dailyCheckbox() throws Exception {

        // given
        Member member = memberRepository.save(new Member("test@abc.co.kr", "e1Df%4sa"));
        PlanResDto planResDto = planService.save(new PlanReqDto("plan", LocalDate.now(), LocalDate.now().plusDays(3), member.getId()));

        CheckboxResDto checkboxResDto = checkboxService.save(new CheckboxReqDto("title", planResDto.getId(), LocalDate.now()));
        Long checkboxId = checkboxResDto.getId();

        MockHttpServletRequestBuilder request = delete("/checkbox/" + checkboxId)
                .header("Authorization", "Bearer " + jwtProvider.generateToken(member.getId()));

        // when - then
        mockMvc.perform(request)
                .andExpect(status().isNoContent());

        Optional<Checkbox> findCheckbox = checkboxRepository.findById(checkboxId);
        assertThat(findCheckbox).isEmpty();

    }

    @Test
    @DisplayName("비정상 삭제 - 삭제할 할일이 DB에 없는 경우 ApiSimpleError를 리턴한다. message 필드의 내용을 확인한다.")
    void deleteTestAbnormal_resourceNotFound() throws Exception {

        // given
        MockHttpServletRequestBuilder request = delete("/checkbox/" + Long.MAX_VALUE)
                .header("Authorization", "Bearer " + jwtProvider.generateToken(Long.MAX_VALUE));

        // when - then
        mockMvc.perform(request)
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("message").value("Resource not found"));

    }

    /**
     * 할일 상태 변경 API 관련 테스트
     * checked ↔ unchecked
     * 할일의 종류(일일, 그룹)와 상관 없이 변경 로직은 동일함
     * @see CheckboxController#change(Long)
     * @throws Exception mockMvc.perform();
     */
    @Test
    @DisplayName("정상 상태 변경 - 상태 변경 후 반환받은 객체의 checked값이 true여야 한다.")
    void changeStatusTestNormal() throws Exception {

        // given
        Member member = memberRepository.save(new Member("test@abc.co.kr", "e1Df%4sa"));
        PlanResDto planResDto = planService.save(new PlanReqDto("plan", LocalDate.now(), LocalDate.now().plusDays(3), member.getId()));
        CheckboxResDto checkboxResDto = checkboxService.save(new CheckboxReqDto("title", planResDto.getId(), LocalDate.now()));
        Long checkboxId = checkboxResDto.getId();

        MockHttpServletRequestBuilder request = patch("/checkbox/" + checkboxId)
                .header("Authorization", "Bearer " + jwtProvider.generateToken(member.getId()));

        // when - then
        mockMvc.perform(request)
                .andExpect(status().isOk())
                .andExpect(jsonPath("checked").value(true));

    }

    @Test
    @DisplayName("비정상 상태 변경 - 상태를 변경할 할일이 없는 경우 ApiSimpleError를 리턴한다. message 필드의 내용을 확인한다.")
    void changeStatusTestAbnormal() throws Exception {

        // given
        MockHttpServletRequestBuilder request = patch("/checkbox/" + Long.MAX_VALUE)
                .header("Authorization", "Bearer " + jwtProvider.generateToken(Long.MAX_VALUE));

        // when - then
        mockMvc.perform(request)
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("message").value("Resource not found"));

    }


    /**
     * 할일 순수 컬렉션 조회 API 관련 테스트
     * 순수 컬렉션이란 필터링이 걸려 있지 않은 컬렉션을 의미함
     *
     * 일정을 기준으로 조회하는 경우 : 일정과 연관된 일일 할일과 그룹 할일을 모두 조회한다.
     * @see CheckboxController#allByPlan(Long)
     *
     * 그룹을 기준으로 조회하는 경우 : 그룹 할일을 모두 조회한다.
     * @see CheckboxController#allByGroup(Long)
     *
     * @throws Exception mockMvc.perform();
     */
    @Test
    @DisplayName("정상 순수 컬렉션 조회 - 일정 기준 - 일정과 연관된 일일 할일과 그룹 할일을 모두 조회한다.")
    void allTestNormal_byPlan() throws Exception {

        // given
        Member member = memberRepository.save(new Member("test@abc.co.kr", "td#4edf1@"));
        Long memberId = member.getId();
        PlanResDto planResDto = planService.save(new PlanReqDto("title", LocalDate.of(2023, 7, 18), LocalDate.of(2023, 7, 25), memberId));
        Long planId = planResDto.getId();

        // 그룹 할일 등록
        groupService.save(new GroupReqDto("group", 3, List.of("월", "수", "금"), planResDto.getId()));

        // 일일 할일 등록
        checkboxService.save(new CheckboxReqDto("title", planId, LocalDate.of(2023, 7, 18)));

        MockHttpServletRequestBuilder request = get("/checkboxes/plan")
                .param("planId", planId.toString());

        // when - then
        mockMvc.perform(request)
                .andExpect(status().isOk())
                .andExpect(jsonPath("_embedded.checkboxResDtoList.length()").value(4))
                .andExpect(jsonPath("_links.plan").exists());

    }

    @Test
    @DisplayName("정상 순수 컬렉션 조회 - 그룹 기준 - 그룹 할일을 모두 조회한다.")
    void allTestNormal_byGroup() throws Exception {

        // given
        Member member = memberRepository.save(new Member("test@abc.co.kr", "a3df!#sac"));
        Long memberId = member.getId();
        PlanResDto planResDto = planService.save(new PlanReqDto("title", LocalDate.of(2023, 7, 18), LocalDate.of(2023, 7, 25), memberId));
        Long planId = planResDto.getId();
        GroupResDto groupResDto = groupService.save(new GroupReqDto("group", 3, List.of("월", "수", "금"), planResDto.getId()));
        Long groupId = groupResDto.getId();
        checkboxService.save(new CheckboxReqDto("title", planId, LocalDate.of(2023, 7, 18)));

        MockHttpServletRequestBuilder request = get("/checkboxes/group")
                .param("groupId", groupId.toString());

        // when - then
        mockMvc.perform(request)
                .andExpect(status().isOk())
                .andExpect(jsonPath("_embedded.checkboxResDtoList.length()").value(3))
                .andExpect(jsonPath("_links.group").exists());

    }

    @Test
    @DisplayName("비정상 순수 컬렉션 조회 - 일정 기준 - 기준으로 삼을 일정이 없는 경우 ApiSimpleError를 리턴한다. message 필드의 내용을 확인한다.")
    void allTestAbnormal_byPlan() throws Exception {

        // given
        MockHttpServletRequestBuilder request = get("/checkboxes/plan")
                .param("planId", String.valueOf(Long.MAX_VALUE));

        // when - then
        mockMvc.perform(request)
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("message").value("Resource not found"));

    }

    @Test
    @DisplayName("비정상 순수 컬렉션 조회 - 그룹 기준 - 기준으로 삼을 그룹이 없는 경우 ApiSimpleError를 리턴한다. message 필드의 내용을 확인한다.")
    void allTestAbnormal_byGroup() throws Exception {

        // given
        MockHttpServletRequestBuilder request = get("/checkboxes/group")
                .param("groupId", String.valueOf(Long.MAX_VALUE));

        // when - then
        mockMvc.perform(request)
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("message").value("Resource not found"));

    }


    /**
     * 할일 일별 컬렉션 조회 API 관련 테스트
     * 일별 컬렉션이란 검색일을 date로 두고 있는 모든 할일을 의미한다.
     *
     * 일정을 기준으로 조회하는 경우 : 일정에 연관된 일일 할일과 그룹 할일 중 date 필드의 값이 검색일인 할일을 모두 조회한다.
     * @see CheckboxController#allByPlan(Long, LocalDate)
     *
     * 그룹을 기준으로 조회하는 경우 : 그룹 할일 중 date필드의 값이 검색일인 할일을 모두 조회한다.
     * @see CheckboxController#allByGroup(Long)
     *
     * @throws Exception mockMvc.perform();
     */
    @Test
    @DisplayName("정상 일별 컬렉션 조회 - 그룹 기준 - 그룹 할일 중 date 필드의 값이 검색일과 동일한 그룹 할일을 모두 조회한다.")
    void collectionFilteredByDateTestNormal_byGroup() throws Exception {

        // given
        Member member = memberRepository.save(new Member("test@abc.co.kr", "13d^3ea#"));
        PlanResDto planResDto = planService.save(new PlanReqDto("title", LocalDate.of(2023, 7, 19), LocalDate.of(2023, 7, 31), member.getId()));
        Long planId = planResDto.getId();
        GroupResDto groupResDto1 = groupService.save(new GroupReqDto("title1", 3, List.of("화", "목"), planId));
        LocalDate dateKey = LocalDate.of(2023, 7, 25);

        MockHttpServletRequestBuilder request = get("/checkboxes/group/date")
                .param("groupId", String.valueOf(groupResDto1.getId()))
                .param("dateKey", dateKey.toString());

        // when - then
        mockMvc.perform(request)
                .andExpect(status().isOk())
                .andExpect(jsonPath("_embedded.checkboxResDtoList.length()").value(1))
                .andDo(print());

    }

    @Test
    @DisplayName("비정상 일별 컬렉션 조회 - 그룹 기준 - 기준으로 삼을 그룹이 없는 경우 ApiSimpleError를 리턴한다. message 필드의 내용을 확인한다.")
    void collectionFilteredByDateTestAbnormal_byGroup() throws Exception {

        // given
        MockHttpServletRequestBuilder request = get("/checkboxes/group/date")
                .param("groupId", String.valueOf(Long.MAX_VALUE))
                .param("dateKey", LocalDate.of(2023, 7, 31).toString());

        // when - then
        mockMvc.perform(request)
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("message").value("Resource not found"));

    }

    @Test
    @DisplayName("정상 일별 컬렉션 조회 - 일정 기준 - 일정과 연결된 일일 할일과 그룹 할일 중 date 필드의 값이 검색일인 일정을 모두 조회한다.")
    void collectionFilteredByDateTestNormal_byPlan() throws Exception {

        // given
        Member member = memberRepository.save(new Member("test@abc.co.kr", "13d^3ea#"));
        PlanResDto planResDto = planService.save(new PlanReqDto("title", LocalDate.of(2023, 7, 19), LocalDate.of(2023, 7, 31), member.getId()));
        Long planId = planResDto.getId();

        // 그룹 할일 등록
        groupService.save(new GroupReqDto("title1", 3, List.of("화", "목"), planId));
        groupService.save(new GroupReqDto("title2", 2, List.of("2"), planId));
        groupService.save(new GroupReqDto("title3", 1, List.of(), planId));

        // 일일 할일 등록
        checkboxService.save(new CheckboxReqDto("title4", planId, LocalDate.of(2023, 7, 19)));
        checkboxService.save(new CheckboxReqDto("title5", planId, LocalDate.of(2023, 7, 19)));
        checkboxService.save(new CheckboxReqDto("title6", planId, LocalDate.of(2023, 7, 19)));

        LocalDate dateKey = LocalDate.of(2023, 7, 19);

        MockHttpServletRequestBuilder request = get("/checkboxes/plan/date")
                .param("planId", String.valueOf(planId))
                .param("dateKey", dateKey.toString());


        // when - then
        mockMvc.perform(request)
                .andExpect(status().isOk())
                .andExpect(jsonPath("_embedded.checkboxResDtoList.length()").value(5));

    }

    @Test
    @DisplayName("비정상 일별 컬렉션 조회 - 그룹 기준 - 기준으로 삼을 그룹이 없는 경우 ApiSimpleError를 리턴한다. message 필드의 내용을 확인한다.")
    void collectionFilteredByDateTestAbnormal_byPlan() throws Exception {

        // given
        MockHttpServletRequestBuilder request = get("/checkboxes/group/date")
                .param("groupId", String.valueOf(Long.MAX_VALUE))
                .param("dateKey", LocalDate.of(2023, 7, 31).toString());

        // when - then
        mockMvc.perform(request)
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("message").value("Resource not found"));

    }


    /**
     * 할일 기간 컬렉션 조회 API 관련 테스트
     * 기간 컬렉션이란 시작일부터 종료일 사이의 범위에 date 필드가 들어 있는 모든 할일을 조회한 것이다.
     *
     * 일정을 기준으로 조회하는 경우 : 일정과 연관된 일일 할일과 그룹 할일 중 검색 범위에 date 필드가 있는 모든 할일을 조회한다.
     * @see CheckboxController#allByPlan(Long, LocalDate, LocalDate)
     *
     * 그룹을 기준으로 조회하는 경우 : 그룹 할일 중 검색 범위에 date필드가 있는 모든 할일을 조회한다.
     * @see CheckboxController#allByGroup(Long, LocalDate, LocalDate)
     *
     * @throws Exception mockMvc.perform();
     */
    @Test
    @DisplayName("정상 기간 컬렉션 조회 - 그룹 기준 - 그룹 할일 중 검색 범위에 date필드가 있는 모든 할일을 조회한다.")
    void collectionFilteredByDateRangeTestNormal_byGroup() throws Exception {

        // given
        Member member = memberRepository.save(new Member("test@abc.co.kr", "13d^3ea#"));
        PlanResDto planResDto = planService.save(new PlanReqDto("title", LocalDate.of(2023, 7, 19), LocalDate.of(2023, 7, 31), member.getId()));
        Long planId = planResDto.getId();

        GroupResDto groupResDto = groupService.save(new GroupReqDto("title1", 3, List.of("화", "목", "일"), planId));
        Long groupId = groupResDto.getId();

        LocalDate searchStart = LocalDate.of(2023, 7, 19);
        LocalDate searchEnd = LocalDate.of(2023, 7, 22);

        MockHttpServletRequestBuilder request = get("/checkboxes/group/range")
                .param("groupId", String.valueOf(groupId))
                .param("searchStart", searchStart.toString())
                .param("searchEnd", searchEnd.toString());

        // when - then
        mockMvc.perform(request)
                .andExpect(status().isOk())
                .andExpect(jsonPath("_embedded.checkboxResDtoList.length()").value(1))
                .andDo(print());

    }

    @Test
    @DisplayName("비정상 기간 컬렉션 조회 - 그룹 기준 - 검색 시작일이 검색 종료일보다 늦은 경우 ApiBindingError를 리턴한다. " +
            "errors 객체에 searchStart, searchEnd 키가 있는지 확인한다.")
    void collectionFilteredByDateRangeTestAbnormal_byGroup_invalidQueryString() throws Exception {

        // given
        LocalDate searchStart = LocalDate.of(2023, 7, 22);
        LocalDate searchEnd = LocalDate.of(2023, 7, 20);

        MockHttpServletRequestBuilder request = get("/checkboxes/group/range")
                .param("groupId", "1")
                .param("searchStart", searchStart.toString())
                .param("searchEnd", searchEnd.toString());

        // when - then
        mockMvc.perform(request)
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("errors.searchStart").exists())
                .andExpect(jsonPath("errors.searchEnd").exists());

    }

    @Test
    @DisplayName("정상 기간 컬렉션 조회 - 일정 기준 - 일정과 연관된 일일 할일과 그룹 할일 중 검색 범위에 date 필드가 있는 모든 할일을 조회한다.")
    void collectionFilteredByDateRangeTestNormal_byPlan() throws Exception {

        // given
        Member member = memberRepository.save(new Member("test@abc.co.kr", "13d^3ea#"));
        PlanResDto planResDto = planService.save(new PlanReqDto("title", LocalDate.of(2023, 7, 19), LocalDate.of(2023, 7, 31), member.getId()));
        Long planId = planResDto.getId();

        // 그룹 할일 등록
        groupService.save(new GroupReqDto("title1", 3, List.of("화", "목", "일"), planId));
        groupService.save(new GroupReqDto("title2", 2, List.of("2"), planId));

        // 일일 할일 등록
        checkboxService.save(new CheckboxReqDto("title3", planId, LocalDate.of(2023, 7, 19)));
        checkboxService.save(new CheckboxReqDto("title4", planId, LocalDate.of(2023, 7, 23)));
        checkboxService.save(new CheckboxReqDto("title5", planId, LocalDate.of(2023, 7, 27)));

        LocalDate searchStart = LocalDate.of(2023, 7, 19);
        LocalDate searchEnd = LocalDate.of(2023, 7, 22);

        MockHttpServletRequestBuilder request = get("/checkboxes/plan/range")
                .param("planId", String.valueOf(planId))
                .param("searchStart", searchStart.toString())
                .param("searchEnd", searchEnd.toString());

        // when - then
        mockMvc.perform(request)
                .andExpect(status().isOk())
                .andExpect(jsonPath("_embedded.checkboxResDtoList.length()").value(4));

    }

    @Test
    @DisplayName("비정상 기간 컬렉션 조회 - 일정 기준 - 검색 시작일이 종료일보다 늦는 경우 ApiBindingError를 리턴한다. " +
            "errors 객체에 searchStart, searchEnd 키가 있는지 확인한다.")
    void collectionFilteredByDateRangeTestAbnormal_byPlan_invalidQueryString() throws Exception {

        // given
        LocalDate searchStart = LocalDate.of(2023, 7, 22);
        LocalDate searchEnd = LocalDate.of(2023, 7, 20);

        MockHttpServletRequestBuilder request = get("/checkboxes/plan/range")
                .param("planId", "1")
                .param("searchStart", searchStart.toString())
                .param("searchEnd", searchEnd.toString());

        // when - then
        mockMvc.perform(request)
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("errors.searchStart").exists())
                .andExpect(jsonPath("errors.searchEnd").exists());

    }

}
