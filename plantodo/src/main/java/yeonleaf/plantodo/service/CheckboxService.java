package yeonleaf.plantodo.service;

import org.springframework.stereotype.Service;
import yeonleaf.plantodo.dto.CheckboxReqDto;
import yeonleaf.plantodo.dto.CheckboxResDto;
import yeonleaf.plantodo.dto.CheckboxUpdateReqDto;

@Service
public interface CheckboxService {

    CheckboxResDto save(CheckboxReqDto checkboxReqDto);
    CheckboxResDto one(Long id);
    CheckboxResDto update(CheckboxUpdateReqDto checkboxUpdateReqDto);

}
