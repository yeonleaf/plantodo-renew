package yeonleaf.plantodo.service;

import org.springframework.stereotype.Service;
import yeonleaf.plantodo.domain.Member;
import yeonleaf.plantodo.domain.Plan;
import yeonleaf.plantodo.dto.PlanReqDto;
import yeonleaf.plantodo.dto.PlanResDto;
import yeonleaf.plantodo.dto.PlanUpdateReqDto;
import yeonleaf.plantodo.wrapper.PlanResDtoWrap;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface PlanService {

    PlanResDto save(PlanReqDto planReqDto);
    PlanResDto one(Long id);
    PlanResDto update(PlanUpdateReqDto planUpdateReqDto);
    void delete(Long id);
    PlanResDtoWrap all(Long memberId);
    PlanResDtoWrap all(Long memberId, LocalDate dateKey);
    PlanResDtoWrap all(Long memberId, LocalDate searchStart, LocalDate searchEnd);
    PlanResDto change(Long id);

}
