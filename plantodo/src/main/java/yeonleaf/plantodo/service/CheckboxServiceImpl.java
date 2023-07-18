package yeonleaf.plantodo.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import yeonleaf.plantodo.domain.Checkbox;
import yeonleaf.plantodo.repository.CheckboxRepository;

@Service
@RequiredArgsConstructor
public class CheckboxServiceImpl implements CheckboxService {

    private final CheckboxRepository checkboxRepository;

    @Override
    public Checkbox save(Checkbox checkbox) {
        return checkboxRepository.save(checkbox);
    }

}
