package yeonleaf.plantodo.service;

import org.springframework.stereotype.Service;
import yeonleaf.plantodo.domain.Checkbox;
import yeonleaf.plantodo.dto.CheckboxReqDto;
import yeonleaf.plantodo.dto.CheckboxResDto;

@Service
public interface CheckboxService {
    CheckboxResDto save(CheckboxReqDto checkboxReqDto);
}
