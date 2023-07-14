package yeonleaf.plantodo.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import yeonleaf.plantodo.dto.GroupReqDto;
import yeonleaf.plantodo.dto.GroupResDto;
import yeonleaf.plantodo.dto.RepInputDto;

@Service
@RequiredArgsConstructor
public class GroupServiceImpl implements GroupService {
    @Override
    public GroupResDto save(GroupReqDto groupReqDto, RepInputDto repInputDto) {
        return null;
    }
}
