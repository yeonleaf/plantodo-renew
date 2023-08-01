package yeonleaf.plantodo.service;

import org.springframework.stereotype.Service;
import yeonleaf.plantodo.dto.CheckboxReqDto;
import yeonleaf.plantodo.dto.CheckboxResDto;
import yeonleaf.plantodo.dto.CheckboxUpdateReqDto;

import java.time.LocalDate;
import java.util.List;

@Service
public interface CheckboxService {

    CheckboxResDto save(CheckboxReqDto checkboxReqDto);
    CheckboxResDto one(Long id);
    CheckboxResDto update(CheckboxUpdateReqDto checkboxUpdateReqDto);
    void delete(Long id);
    CheckboxResDto change(Long id);
    List<CheckboxResDto> allByGroup(Long groupId);
    List<CheckboxResDto> allByGroup(Long groupId, LocalDate dateKey);
    List<CheckboxResDto> allByPlan(Long planId);
    List<CheckboxResDto> allByPlan(Long planId, LocalDate dateKey);

}
