package yeonleaf.plantodo.service;

import org.springframework.stereotype.Service;
import yeonleaf.plantodo.domain.Plan;
import yeonleaf.plantodo.dto.GroupReqDto;
import yeonleaf.plantodo.dto.GroupResDto;
import yeonleaf.plantodo.dto.GroupUpdateReqDto;
import yeonleaf.plantodo.dto.RepInputDto;

import java.time.LocalDate;
import java.util.List;

@Service
public interface GroupService {

    GroupResDto save(GroupReqDto groupReqDto);

    GroupResDto one(Long id);
    List<GroupResDto> all(Long planId);
    List<GroupResDto> all(Long planId, LocalDate dateKey);
    GroupResDto update(GroupUpdateReqDto groupUpdateReqDto);
    void delete(Long id);

}
