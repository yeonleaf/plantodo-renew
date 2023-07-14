package yeonleaf.plantodo.service;

import org.springframework.stereotype.Service;
import yeonleaf.plantodo.dto.GroupReqDto;
import yeonleaf.plantodo.dto.GroupResDto;
import yeonleaf.plantodo.dto.RepInputDto;

@Service
public interface GroupService {
    GroupResDto save(GroupReqDto groupReqDto, RepInputDto repInputDto);
}
