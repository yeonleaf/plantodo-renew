package yeonleaf.plantodo.service;

import org.springframework.stereotype.Service;
import yeonleaf.plantodo.domain.Checkbox;

@Service
public interface CheckboxService {
    Checkbox save(Checkbox checkbox);
}
