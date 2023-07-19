package yeonleaf.plantodo.service;

import org.springframework.stereotype.Service;
import yeonleaf.plantodo.domain.Plan;
import yeonleaf.plantodo.dto.GroupReqDto;
import yeonleaf.plantodo.dto.GroupResDto;
import yeonleaf.plantodo.dto.RepInputDto;

import java.util.List;

@Service
public interface GroupService {
    GroupResDto save(GroupReqDto groupReqDto);

    GroupResDto one(Long id);
    List<GroupResDto> findAllByPlanId(Long planId);
}
