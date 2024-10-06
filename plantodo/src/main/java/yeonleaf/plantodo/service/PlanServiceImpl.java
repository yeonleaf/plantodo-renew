package yeonleaf.plantodo.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import org.hibernate.tool.schema.internal.exec.ScriptTargetOutputToFile;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.CachePut;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.redis.core.ClusterOperations;
import org.springframework.data.redis.core.HashOperations;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import yeonleaf.plantodo.converter.RepInToOutConverter;
import yeonleaf.plantodo.converter.RepOutToInConverter;
import yeonleaf.plantodo.domain.Checkbox;
import yeonleaf.plantodo.domain.Group;
import yeonleaf.plantodo.domain.Member;
import yeonleaf.plantodo.domain.Plan;
import yeonleaf.plantodo.dto.MemberResDto;
import yeonleaf.plantodo.dto.PlanReqDto;
import yeonleaf.plantodo.dto.PlanResDto;
import yeonleaf.plantodo.dto.PlanUpdateReqDto;
import yeonleaf.plantodo.exceptions.ResourceNotFoundException;
import yeonleaf.plantodo.repository.CheckboxRepository;
import yeonleaf.plantodo.repository.GroupRepository;
import yeonleaf.plantodo.repository.MemberRepository;
import yeonleaf.plantodo.repository.PlanRepository;
import yeonleaf.plantodo.util.CheckboxDateCreator;
import yeonleaf.plantodo.util.PlanDateRangeRevisionMaker;
import yeonleaf.plantodo.wrapper.PlanResDtoWrap;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

@Service
@Transactional
@RequiredArgsConstructor
public class PlanServiceImpl implements PlanService {

    private final MemberRepository memberRepository;
    private final PlanRepository planRepository;
    private final GroupRepository groupRepository;
    private final CheckboxRepository checkboxRepository;
    private final RepOutToInConverter repOutToInConverter;
    private final GroupService groupService;

    @Override
    @CacheEvict(cacheNames = "plan", key = "#result.id", cacheManager = "cacheManager")
    public PlanResDto save(PlanReqDto planReqDto) {
        Member member = memberRepository.findById(planReqDto.getMemberId()).orElseThrow(ResourceNotFoundException::new);
        Plan plan = planRepository.save(new Plan(planReqDto, member));
        groupRepository.save(new Group(plan, "DailyGroup"));
        return new PlanResDto(plan);

    }

    @Override
    @Transactional(readOnly = true)
    @Cacheable(cacheNames = "plan", key="#id", cacheManager = "cacheManager")
    public PlanResDto one(Long id) {

        Plan plan = planRepository.findById(id).orElseThrow(ResourceNotFoundException::new);
        checkPlanOutdated(plan);
        return new PlanResDto(plan);

    }

    private void checkPlanOutdated(Plan plan) {
        if (plan.getEnd().isBefore(LocalDate.now())) {
            plan.changeToPast();
            planRepository.save(plan);
        }
    }

    @Override
    @CachePut(cacheNames = "plan", key = "#planUpdateReqDto.id", cacheManager = "cacheManager")
    public PlanResDto update(PlanUpdateReqDto planUpdateReqDto) {

        PlanDateRangeRevisionMaker planDateRangeRevisionMaker = new PlanDateRangeRevisionMaker();

        Long id = planUpdateReqDto.getId();
        Plan oldPlan = planRepository.findById(id).orElseThrow(ResourceNotFoundException::new);
        if (onlyTitleDifferent(planUpdateReqDto, oldPlan)) {
            oldPlan.setTitle(planUpdateReqDto.getTitle());
            return new PlanResDto(planRepository.save(oldPlan));
        }

        HashMap<LocalDate, Integer> revisedDateRange = planDateRangeRevisionMaker.revise(planUpdateReqDto, oldPlan);

        oldPlan.setTitle(planUpdateReqDto.getTitle());
        oldPlan.setStart(planUpdateReqDto.getStart());
        oldPlan.setEnd(planUpdateReqDto.getEnd());
        Plan updatedPlan = planRepository.save(oldPlan);

        List<Group> groups = groupRepository.findByPlanIdEntityGraph(updatedPlan.getId());
        for (Group group : groups) {
            if (needResetMode(group)) {
                updateResetMode(group, updatedPlan);
            } else {
                updatePreserveMode(revisedDateRange, group, updatedPlan);
            }
        }

        return new PlanResDto(updatedPlan);

    }

    private boolean needResetMode(Group group) {
        return group.getRepetition().getRepOption() == 2;
    }

    private void updatePreserveMode(HashMap<LocalDate, Integer> revisedDateRange, Group group, Plan updatedPlan) {

        // checkbox 삭제
        List<Checkbox> checkboxes = checkboxRepository.findByGroupId(group.getId());
        checkboxes.stream().filter(checkbox -> revisedDateRange.containsKey(checkbox.getDate()) && revisedDateRange.get(checkbox.getDate()).equals(2))
                .forEach(checkboxRepository::delete);

        // checkbox 생성
        List<LocalDate> revisedDates = CheckboxDateCreator.create(updatedPlan, Objects.requireNonNull(repOutToInConverter.convert(group.getRepetition())));
        revisedDateRange.keySet().stream().filter(date -> revisedDateRange.get(date).equals(3))
                .filter(revisedDates::contains)
                .forEach(date -> {
                    checkboxRepository.save(new Checkbox(group, group.getTitle(), date, false));
                });

    }

    private void updateResetMode(Group group, Plan updatedPlan) {

        checkboxRepository.findByGroupId(group.getId())
                .stream().forEach(checkboxRepository::delete);
        List<LocalDate> dates = CheckboxDateCreator.create(updatedPlan, repOutToInConverter.convert(group.getRepetition()));
        dates.forEach(date -> checkboxRepository.save(new Checkbox(group, group.getTitle(), date, false)));

    }

    private boolean onlyTitleDifferent(PlanUpdateReqDto planUpdateReqDto, Plan plan) {
        boolean titleDifferent = !planUpdateReqDto.getTitle().equals(plan.getTitle());
        boolean startDifferent = !planUpdateReqDto.getStart().isEqual(plan.getStart());
        boolean endDifferent = !planUpdateReqDto.getEnd().isEqual(plan.getEnd());
        return (titleDifferent && !startDifferent && !endDifferent);
    }


    @Override
    @CacheEvict(cacheNames = "plan", key = "#id", cacheManager = "cacheManager")
    public void delete(Long id) {

        Plan plan = planRepository.findById(id).orElseThrow(ResourceNotFoundException::new);
        groupRepository.findByPlanIdEntityGraph(plan.getId()).forEach(group -> groupService.delete(group.getId()));
        planRepository.delete(plan);

    }

    @Override
    @Transactional(readOnly = true)
    @Cacheable(cacheNames = "plansByMemberId", key = "#memberId", cacheManager = "cacheManager")
    public PlanResDtoWrap all(Long memberId) {

        memberRepository.findById(memberId).orElseThrow(ResourceNotFoundException::new);
        List<PlanResDto> planResDtoList = planRepository.findByMemberId(memberId).stream().map(PlanResDto::new).toList();
        return new PlanResDtoWrap(planResDtoList);

    }

    @Override
    @CacheEvict(cacheNames = "plan", key = "#id", cacheManager = "cacheManager")
    public PlanResDto change(Long id) {

        Plan plan = planRepository.findById(id).orElseThrow(ResourceNotFoundException::new);
        plan.changeStatus();
        return new PlanResDto(planRepository.save(plan));

    }

    @Override
    @Transactional(readOnly = true)
    @Cacheable(cacheNames = "plansByMemberIdAndDate", cacheManager = "cacheManager")
    public PlanResDtoWrap all(Long memberId, LocalDate dateKey) {

        memberRepository.findById(memberId).orElseThrow(ResourceNotFoundException::new);
        List<PlanResDto> planResDtoList = planRepository.findByMemberId(memberId).stream()
                .filter(plan -> hasDateKey(plan, dateKey))
                .map(PlanResDto::new).toList();
        return new PlanResDtoWrap(planResDtoList);

    }

    private boolean hasDateKey(Plan plan, LocalDate dateKey) {

        return !(dateKey.isBefore(plan.getStart()) || dateKey.isAfter(plan.getEnd()));

    }

    @Override
    @Transactional(readOnly = true)
    @Cacheable(cacheNames = "plansByMemberIdAndRange", cacheManager = "cacheManager")
    public PlanResDtoWrap all(Long memberId, LocalDate searchStart, LocalDate searchEnd) {

        List<PlanResDto> planResDtoList = all(memberId).getWrap().stream()
                .filter(planResDto -> new PlanDateRangeRevisionMaker().isInRange(searchStart, searchEnd, planResDto.getStart(), planResDto.getEnd()))
                .toList();
        return new PlanResDtoWrap(planResDtoList);

    }

}
