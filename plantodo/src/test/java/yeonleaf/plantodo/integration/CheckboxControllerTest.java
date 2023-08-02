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
import yeonleaf.plantodo.domain.Checkbox;
import yeonleaf.plantodo.domain.Member;
import yeonleaf.plantodo.domain.Plan;
import yeonleaf.plantodo.domain.PlanStatus;
import yeonleaf.plantodo.dto.*;
import yeonleaf.plantodo.exceptions.ResourceNotFoundException;
import yeonleaf.plantodo.provider.JwtBasicProvider;
import yeonleaf.plantodo.provider.JwtProvider;
import yeonleaf.plantodo.repository.CheckboxRepository;
import yeonleaf.plantodo.repository.MemberRepository;
import yeonleaf.plantodo.service.CheckboxService;
import yeonleaf.plantodo.service.GroupService;
import yeonleaf.plantodo.service.PlanService;

import javax.crypto.SecretKey;
import java.time.LocalDate;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
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
    private SecretKey jwtSecretKey;

    @Autowired
    private JwtBasicProvider jwtProvider;

    @Test
    @DisplayName("정상 저장")
    void saveTestNormal() throws Exception {

        Member member = memberRepository.save(new Member("test@abc.co.kr", "e1Df%4sa"));
        PlanResDto planResDto = planService.save(new PlanReqDto("plan", LocalDate.now(), LocalDate.now().plusDays(3), member.getId()));

        CheckboxReqDto checkboxReqDto = new CheckboxReqDto("checkbox", planResDto.getId(), LocalDate.now());

        MockHttpServletRequestBuilder request = post("/checkbox")
                .header("Authorization", "Bearer " + jwtProvider.generateToken(member.getId()))
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(checkboxReqDto));

        mockMvc.perform(request)
                .andExpect(status().isCreated())
                .andExpect(jsonPath("id").exists())
                .andExpect(jsonPath("_links").exists())
                .andDo(print());

    }

    @Test
    @DisplayName("단건 정상 조회")
    void oneTestNormal() throws Exception {

        Member member = memberRepository.save(new Member("test@abc.co.kr", "e1Df%4sa"));
        PlanResDto planResDto = planService.save(new PlanReqDto("plan", LocalDate.now(), LocalDate.now().plusDays(3), member.getId()));
        CheckboxResDto checkboxResDto = checkboxService.save(new CheckboxReqDto("checkbox", planResDto.getId(), LocalDate.now()));

        MockHttpServletRequestBuilder request = get("/checkbox/" + checkboxResDto.getId())
                .header("Authorization", "Bearer " + jwtProvider.generateToken(member.getId()));

        mockMvc.perform(request)
                .andExpect(status().isOk())
                .andExpect(jsonPath("id").value(checkboxResDto.getId()))
                .andExpect(jsonPath("title").value(checkboxResDto.getTitle()))
                .andExpect(jsonPath("checked").value(checkboxResDto.isChecked()));

    }

    @Test
    @DisplayName("단건 비정상 조회")
    void oneTestAbnormal() throws Exception {

        MockHttpServletRequestBuilder request = get("/checkbox/" + Long.MAX_VALUE)
                .header("Authorization", "Bearer " + jwtProvider.generateToken(Long.MAX_VALUE));
        mockMvc.perform(request)
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("message").value("Resource not found"));

    }

    @Test
    @DisplayName("정상 수정 - checkbox not in group")
    void updateTestNormal_checkboxNotInGroup() throws Exception {

        Member member = memberRepository.save(new Member("test@abc.co.kr", "e1Df%4sa"));
        PlanResDto planResDto = planService.save(new PlanReqDto("plan", LocalDate.now(), LocalDate.now().plusDays(3), member.getId()));
        CheckboxResDto checkboxResDto = checkboxService.save(new CheckboxReqDto("title", planResDto.getId(), LocalDate.now()));

        CheckboxUpdateReqDto checkboxUpdateReqDto = new CheckboxUpdateReqDto(checkboxResDto.getId(), "updatedTitle");
        MockHttpServletRequestBuilder request = put("/checkbox")
                .header("Authorization", "Bearer " + jwtProvider.generateToken(member.getId()))
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(checkboxUpdateReqDto));

        mockMvc.perform(request)
                .andExpect(status().isOk());

        Checkbox findCheckbox = checkboxRepository.findById(checkboxResDto.getId()).orElseThrow(ResourceNotFoundException::new);
        assertThat(findCheckbox.getTitle()).isEqualTo("updatedTitle");

    }

    private List<String> makeArrList(String... target) {
        return Arrays.asList(target);
    }

    @Test
    @DisplayName("정상 수정 - checkbox in group")
    void updateTestNormal_checkboxInGroup() throws Exception {

        Member member = memberRepository.save(new Member("test@abc.co.kr", "e1Df%4sa"));
        PlanResDto planResDto = planService.save(new PlanReqDto("plan", LocalDate.now(), LocalDate.now().plusDays(3), member.getId()));
        GroupResDto groupResDto = groupService.save(new GroupReqDto("title", 1, makeArrList(), planResDto.getId()));
        Checkbox checkbox = checkboxRepository.findByGroupId(groupResDto.getId()).get(0);

        CheckboxUpdateReqDto checkboxUpdateReqDto = new CheckboxUpdateReqDto(checkbox.getId(), "updatedTitle");
        MockHttpServletRequestBuilder request = put("/checkbox")
                .header("Authorization", "Bearer " + jwtProvider.generateToken(member.getId()))
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(checkboxUpdateReqDto));
        mockMvc.perform(request)
                .andExpect(status().isOk());

        Checkbox findCheckbox = checkboxRepository.findById(checkbox.getId()).orElseThrow(ResourceNotFoundException::new);
        assertThat(findCheckbox.getTitle()).isEqualTo("updatedTitle");

    }

    @Test
    @DisplayName("비정상 수정 - ArgumentResolver validation")
    void updateTestAbnormal_argumentResolverValidation() throws Exception {

        CheckboxUpdateReqDto checkboxUpdateReqDto = new CheckboxUpdateReqDto(null, "updatedTitle");
        MockHttpServletRequestBuilder request = put("/checkbox")
                .header("Authorization", "Bearer " + jwtProvider.generateToken(null))
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(checkboxUpdateReqDto));
        mockMvc.perform(request)
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("errors.id").exists());

    }

    @Test
    @DisplayName("비정상 수정 - Resource not found")
    void updateTestAbnormal_resourceNotFound() throws Exception {

        CheckboxUpdateReqDto checkboxUpdateReqDto = new CheckboxUpdateReqDto(Long.MAX_VALUE, "updatedTitle");

        MockHttpServletRequestBuilder request = put("/checkbox")
                .header("Authorization", "Bearer " + jwtProvider.generateToken(Long.MAX_VALUE))
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(checkboxUpdateReqDto));

        mockMvc.perform(request)
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("message").value("Resource not found"));

    }

    @Test
    @DisplayName("정상 삭제 - group checkbox")
    void deleteTestNormal_groupCheckbox() throws Exception {

        Member member = memberRepository.save(new Member("test@abc.co.kr", "e1Df%4sa"));
        PlanResDto planResDto = planService.save(new PlanReqDto("plan", LocalDate.now(), LocalDate.now().plusDays(3), member.getId()));
        GroupResDto groupResDto = groupService.save(new GroupReqDto("title", 1, makeArrList(), planResDto.getId()));

        Checkbox checkbox = checkboxRepository.findByGroupId(groupResDto.getId()).get(0);
        Long checkboxId = checkbox.getId();

        MockHttpServletRequestBuilder request = delete("/checkbox/" + checkboxId)
                .header("Authorization", "Bearer " + jwtProvider.generateToken(member.getId()));


        mockMvc.perform(request)
                .andExpect(status().isNoContent());

        Optional<Checkbox> findCheckbox = checkboxRepository.findById(checkboxId);
        assertThat(findCheckbox).isEmpty();

    }

    @Test
    @DisplayName("정상 삭제 - daily checkbox")
    void deleteTestNormal_dailyCheckbox() throws Exception {

        Member member = memberRepository.save(new Member("test@abc.co.kr", "e1Df%4sa"));
        PlanResDto planResDto = planService.save(new PlanReqDto("plan", LocalDate.now(), LocalDate.now().plusDays(3), member.getId()));

        CheckboxResDto checkboxResDto = checkboxService.save(new CheckboxReqDto("title", planResDto.getId(), LocalDate.now()));
        Long checkboxId = checkboxResDto.getId();

        MockHttpServletRequestBuilder request = delete("/checkbox/" + checkboxId)
                .header("Authorization", "Bearer " + jwtProvider.generateToken(member.getId()));

        mockMvc.perform(request)
                .andExpect(status().isNoContent());

        Optional<Checkbox> findCheckbox = checkboxRepository.findById(checkboxId);
        assertThat(findCheckbox).isEmpty();

    }

    @Test
    @DisplayName("비정상 삭제 - Resource not found")
    void deleteTestAbnormal_resourceNotFound() throws Exception {

        MockHttpServletRequestBuilder request = delete("/checkbox/" + Long.MAX_VALUE)
                .header("Authorization", "Bearer " + jwtProvider.generateToken(Long.MAX_VALUE));

        mockMvc.perform(request)
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("message").value("Resource not found"));

    }

    @Test
    @DisplayName("정상 상태 변경")
    void changeStatusTestNormal() throws Exception {

        Member member = memberRepository.save(new Member("test@abc.co.kr", "e1Df%4sa"));
        PlanResDto planResDto = planService.save(new PlanReqDto("plan", LocalDate.now(), LocalDate.now().plusDays(3), member.getId()));
        CheckboxResDto checkboxResDto = checkboxService.save(new CheckboxReqDto("title", planResDto.getId(), LocalDate.now()));
        Long checkboxId = checkboxResDto.getId();

        MockHttpServletRequestBuilder request = patch("/checkbox/" + checkboxId)
                .header("Authorization", "Bearer " + jwtProvider.generateToken(member.getId()));

        mockMvc.perform(request)
                .andExpect(status().isOk())
                .andExpect(jsonPath("checked").value(true));

    }

    @Test
    @DisplayName("비정상 상태 변경")
    void changeStatusTestAbnormal() throws Exception {

        MockHttpServletRequestBuilder request = patch("/checkbox/" + Long.MAX_VALUE)
                .header("Authorization", "Bearer " + jwtProvider.generateToken(Long.MAX_VALUE));

        mockMvc.perform(request)
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("message").value("Resource not found"));

    }

    public List<String> makeArrToList(String... target) {
        return Arrays.asList(target);
    }

    @Test
    @DisplayName("정상 순수 컬렉션 조회 - by plan")
    void allTestNormal_byPlan() throws Exception {

        Member member = memberRepository.save(new Member("test@abc.co.kr", "td#4edf1@"));
        Long memberId = member.getId();
        PlanResDto planResDto = planService.save(new PlanReqDto("title", LocalDate.of(2023, 7, 18), LocalDate.of(2023, 7, 25), memberId));
        Long planId = planResDto.getId();
        groupService.save(new GroupReqDto("group", 3, makeArrToList("월", "수", "금"), planResDto.getId()));
        checkboxService.save(new CheckboxReqDto("title", planId, LocalDate.of(2023, 7, 18)));

        MockHttpServletRequestBuilder request = get("/checkboxes")
                .param("standard", "plan")
                .param("standardId", planId.toString());

        mockMvc.perform(request)
                .andExpect(status().isOk())
                .andExpect(jsonPath("_embedded.checkboxResDtoList.length()").value(4));

    }

    @Test
    @DisplayName("정상 순수 컬렉션 조회 - by group")
    void allTestNormal_byGroup() throws Exception {

        Member member = memberRepository.save(new Member("test@abc.co.kr", "a3df!#sac"));
        Long memberId = member.getId();
        PlanResDto planResDto = planService.save(new PlanReqDto("title", LocalDate.of(2023, 7, 18), LocalDate.of(2023, 7, 25), memberId));
        Long planId = planResDto.getId();
        GroupResDto groupResDto = groupService.save(new GroupReqDto("group", 3, makeArrToList("월", "수", "금"), planResDto.getId()));
        Long groupId = groupResDto.getId();
        checkboxService.save(new CheckboxReqDto("title", planId, LocalDate.of(2023, 7, 18)));

        MockHttpServletRequestBuilder request = get("/checkboxes")
                .param("standard", "group")
                .param("standardId", groupId.toString());

        mockMvc.perform(request)
                .andExpect(status().isOk())
                .andExpect(jsonPath("_embedded.checkboxResDtoList.length()").value(3));

    }

    @Test
    @DisplayName("정상 순수 컬렉션 조회 - by group - not lowercase standard")
    void allTestNormal_byGroup_notLowerCaseStandard() throws Exception {

        Member member = memberRepository.save(new Member("test@abc.co.kr", "a3df!#sac"));
        Long memberId = member.getId();
        PlanResDto planResDto = planService.save(new PlanReqDto("title", LocalDate.of(2023, 7, 18), LocalDate.of(2023, 7, 25), memberId));
        Long planId = planResDto.getId();
        GroupResDto groupResDto = groupService.save(new GroupReqDto("group", 3, makeArrToList("월", "수", "금"), planResDto.getId()));
        Long groupId = groupResDto.getId();
        checkboxService.save(new CheckboxReqDto("title", planId, LocalDate.of(2023, 7, 18)));

        MockHttpServletRequestBuilder request = get("/checkboxes")
                .param("standard", "GRoup")
                .param("standardId", groupId.toString());

        mockMvc.perform(request)
                .andExpect(status().isOk())
                .andExpect(jsonPath("_embedded.checkboxResDtoList.length()").value(3));

    }

    @Test
    @DisplayName("비정상 순수 컬렉션 조회 - by plan - Resource not found")
    void allTestAbnormal_byPlan() throws Exception {

        MockHttpServletRequestBuilder request = get("/checkboxes")
                .param("standard", "plan")
                .param("standardId", String.valueOf(Long.MAX_VALUE));


        mockMvc.perform(request)
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("message").value("Resource not found"));

    }

    @Test
    @DisplayName("비정상 순수 컬렉션 조회 - by group - Resource not found")
    void allTestAbnormal_byGroup() throws Exception {

        MockHttpServletRequestBuilder request = get("/checkboxes")
                .param("standard", "group")
                .param("standardId", String.valueOf(Long.MAX_VALUE));

        mockMvc.perform(request)
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("message").value("Resource not found"));

    }

    @Test
    @DisplayName("비정상 순수 컬렉션 조회 - invalid standard")
    void allTestAbnormal_notPlanAndNotGroup() throws Exception {

        MockHttpServletRequestBuilder request = get("/checkboxes")
                .param("standard", "member")
                .param("standardId", String.valueOf(Long.MAX_VALUE));

        mockMvc.perform(request)
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("errors.standard").exists());

    }

    @Test
    @DisplayName("정상 일별 컬렉션 조회 - by group")
    void collectionFilteredByDateTestNormal_byGroup() throws Exception {

        Member member = memberRepository.save(new Member("test@abc.co.kr", "13d^3ea#"));
        PlanResDto planResDto = planService.save(new PlanReqDto("title", LocalDate.of(2023, 7, 19), LocalDate.of(2023, 7, 31), member.getId()));
        Long planId = planResDto.getId();
        GroupResDto groupResDto1 = groupService.save(new GroupReqDto("title1", 3, makeArrToList("화", "목"), planId));
        LocalDate dateKey = LocalDate.of(2023, 7, 25);

        MockHttpServletRequestBuilder request = get("/checkboxes/date")
                .param("standard", "group")
                .param("standardId", String.valueOf(groupResDto1.getId()))
                .param("dateKey", dateKey.toString());

        mockMvc.perform(request)
                .andExpect(status().isOk())
                .andExpect(jsonPath("_embedded.checkboxResDtoList.length()").value(1));

    }

    @Test
    @DisplayName("비정상 일별 컬렉션 조회 - by group - Resource not found")
    void collectionFilteredByDateTestAbnormal_byGroup() throws Exception {

        MockHttpServletRequestBuilder request = get("/checkboxes/date")
                .param("standard", "group")
                .param("standardId", String.valueOf(Long.MAX_VALUE))
                .param("dateKey", LocalDate.of(2023, 7, 31).toString());

        mockMvc.perform(request)
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("message").value("Resource not found"));

    }

    @Test
    @DisplayName("정상 일별 컬렉션 조회 - by plan")
    void collectionFilteredByDateTestNormal_byPlan() throws Exception {

        Member member = memberRepository.save(new Member("test@abc.co.kr", "13d^3ea#"));
        PlanResDto planResDto = planService.save(new PlanReqDto("title", LocalDate.of(2023, 7, 19), LocalDate.of(2023, 7, 31), member.getId()));
        Long planId = planResDto.getId();

        groupService.save(new GroupReqDto("title1", 3, makeArrToList("화", "목"), planId));
        groupService.save(new GroupReqDto("title2", 2, makeArrToList("2"), planId));
        groupService.save(new GroupReqDto("title3", 1, makeArrToList(), planId));

        checkboxService.save(new CheckboxReqDto("title4", planId, LocalDate.of(2023, 7, 19)));
        checkboxService.save(new CheckboxReqDto("title5", planId, LocalDate.of(2023, 7, 19)));
        checkboxService.save(new CheckboxReqDto("title6", planId, LocalDate.of(2023, 7, 19)));

        LocalDate dateKey = LocalDate.of(2023, 7, 19);

        MockHttpServletRequestBuilder request = get("/checkboxes/date")
                .param("standard", "plan")
                .param("standardId", String.valueOf(planId))
                .param("dateKey", dateKey.toString());

        mockMvc.perform(request)
                .andExpect(status().isOk())
                .andExpect(jsonPath("_embedded.checkboxResDtoList.length()").value(5));

    }

    @Test
    @DisplayName("비정상 일별 컬렉션 조회 - by group - Resource not found")
    void collectionFilteredByDateTestAbnormal_byPlan() throws Exception {

        MockHttpServletRequestBuilder request = get("/checkboxes/date")
                .param("standard", "plan")
                .param("standardId", String.valueOf(Long.MAX_VALUE))
                .param("dateKey", LocalDate.of(2023, 7, 31).toString());

        mockMvc.perform(request)
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("message").value("Resource not found"));

    }

    @Test
    @DisplayName("정상 기간 컬렉션 조회 - by group")
    void collectionFilteredByDateRangeTestNormal_byGroup() throws Exception {

        Member member = memberRepository.save(new Member("test@abc.co.kr", "13d^3ea#"));
        PlanResDto planResDto = planService.save(new PlanReqDto("title", LocalDate.of(2023, 7, 19), LocalDate.of(2023, 7, 31), member.getId()));
        Long planId = planResDto.getId();

        GroupResDto groupResDto = groupService.save(new GroupReqDto("title1", 3, makeArrToList("화", "목", "일"), planId));
        Long groupId = groupResDto.getId();

        LocalDate searchStart = LocalDate.of(2023, 7, 19);
        LocalDate searchEnd = LocalDate.of(2023, 7, 22);

        MockHttpServletRequestBuilder request = get("/checkboxes/range")
                .param("standard", "group")
                .param("standardId", String.valueOf(groupId))
                .param("searchStart", searchStart.toString())
                .param("searchEnd", searchEnd.toString());

        mockMvc.perform(request)
                .andExpect(status().isOk())
                .andExpect(jsonPath("_embedded.checkboxResDtoList.length()").value(1));

    }

    @Test
    @DisplayName("비정상 기간 컬렉션 조회 - by group - invalid query string")
    void collectionFilteredByDateRangeTestAbnormal_byGroup_invalidQueryString() throws Exception {

        LocalDate searchStart = LocalDate.of(2023, 7, 22);
        LocalDate searchEnd = LocalDate.of(2023, 7, 20);

        MockHttpServletRequestBuilder request = get("/checkboxes/range")
                .param("standard", "group")
                .param("standardId", "1")
                .param("searchStart", searchStart.toString())
                .param("searchEnd", searchEnd.toString());

        mockMvc.perform(request)
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("errors.searchStart").exists())
                .andExpect(jsonPath("errors.searchEnd").exists());

    }

    @Test
    @DisplayName("정상 기간 컬렉션 조회 - by plan")
    void collectionFilteredByDateRangeTestNormal_byPlan() throws Exception {

        Member member = memberRepository.save(new Member("test@abc.co.kr", "13d^3ea#"));
        PlanResDto planResDto = planService.save(new PlanReqDto("title", LocalDate.of(2023, 7, 19), LocalDate.of(2023, 7, 31), member.getId()));
        Long planId = planResDto.getId();

        groupService.save(new GroupReqDto("title1", 3, makeArrToList("화", "목", "일"), planId));
        groupService.save(new GroupReqDto("title2", 2, makeArrToList("2"), planId));

        checkboxService.save(new CheckboxReqDto("title3", planId, LocalDate.of(2023, 7, 19)));
        checkboxService.save(new CheckboxReqDto("title4", planId, LocalDate.of(2023, 7, 23)));
        checkboxService.save(new CheckboxReqDto("title5", planId, LocalDate.of(2023, 7, 27)));

        LocalDate searchStart = LocalDate.of(2023, 7, 19);
        LocalDate searchEnd = LocalDate.of(2023, 7, 22);

        MockHttpServletRequestBuilder request = get("/checkboxes/range")
                .param("standard", "plan")
                .param("standardId", String.valueOf(planId))
                .param("searchStart", searchStart.toString())
                .param("searchEnd", searchEnd.toString());

        mockMvc.perform(request)
                .andExpect(status().isOk())
                .andExpect(jsonPath("_embedded.checkboxResDtoList.length()").value(4));

    }

    @Test
    @DisplayName("비정상 기간 컬렉션 조회 - by plan - invalid query string")
    void collectionFilteredByDateRangeTestAbnormal_byPlan_invalidQueryString() throws Exception {

        LocalDate searchStart = LocalDate.of(2023, 7, 22);
        LocalDate searchEnd = LocalDate.of(2023, 7, 20);

        MockHttpServletRequestBuilder request = get("/checkboxes/range")
                .param("standard", "plan")
                .param("standardId", "1")
                .param("searchStart", searchStart.toString())
                .param("searchEnd", searchEnd.toString());

        mockMvc.perform(request)
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("errors.searchStart").exists())
                .andExpect(jsonPath("errors.searchEnd").exists());

    }

}
