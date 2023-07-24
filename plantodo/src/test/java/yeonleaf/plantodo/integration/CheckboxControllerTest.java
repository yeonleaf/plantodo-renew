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
import yeonleaf.plantodo.dto.*;
import yeonleaf.plantodo.exceptions.ResourceNotFoundException;
import yeonleaf.plantodo.repository.CheckboxRepository;
import yeonleaf.plantodo.repository.MemberRepository;
import yeonleaf.plantodo.service.CheckboxService;
import yeonleaf.plantodo.service.GroupService;
import yeonleaf.plantodo.service.PlanService;

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

    @Test
    @DisplayName("단건 정상 조회")
    void oneTestNormal() throws Exception {

        Member member = memberRepository.save(new Member("test@abc.co.kr", "e1Df%4sa"));
        PlanResDto planResDto = planService.save(new PlanReqDto("plan", LocalDate.now(), LocalDate.now().plusDays(3), member.getId()));
        CheckboxResDto checkboxResDto = checkboxService.save(new CheckboxReqDto("checkbox", planResDto.getId(), LocalDate.now()));

        MockHttpServletRequestBuilder request = get("/checkbox/" + checkboxResDto.getId());

        mockMvc.perform(request)
                .andExpect(status().isOk())
                .andExpect(jsonPath("id").value(checkboxResDto.getId()))
                .andExpect(jsonPath("title").value(checkboxResDto.getTitle()))
                .andExpect(jsonPath("checked").value(checkboxResDto.isChecked()));

    }

    @Test
    @DisplayName("단건 비정상 조회")
    void oneTestAbnormal() throws Exception {

        MockHttpServletRequestBuilder request = get("/checkbox/" + Long.MAX_VALUE);
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

        MockHttpServletRequestBuilder request = delete("/checkbox/" + checkboxId);

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

        MockHttpServletRequestBuilder request = delete("/checkbox/" + checkboxId);

        mockMvc.perform(request)
                .andExpect(status().isNoContent());

        Optional<Checkbox> findCheckbox = checkboxRepository.findById(checkboxId);
        assertThat(findCheckbox).isEmpty();

    }

    @Test
    @DisplayName("비정상 삭제 - Resource not found")
    void deleteTestAbnormal_resourceNotFound() throws Exception {

        MockHttpServletRequestBuilder request = delete("/checkbox/" + Long.MAX_VALUE);

        mockMvc.perform(request)
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("message").value("Resource not found"));

    }

}
