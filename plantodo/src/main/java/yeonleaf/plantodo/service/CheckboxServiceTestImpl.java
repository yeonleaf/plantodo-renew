package yeonleaf.plantodo.service;

import lombok.RequiredArgsConstructor;
import yeonleaf.plantodo.domain.Checkbox;
import yeonleaf.plantodo.repository.MemoryCheckboxRepository;

@RequiredArgsConstructor
public class CheckboxServiceTestImpl implements CheckboxService {

    private final MemoryCheckboxRepository memoryCheckboxRepository;

    @Override
    public Checkbox save(Checkbox checkbox) {
        return memoryCheckboxRepository.save(checkbox);
    }

}
