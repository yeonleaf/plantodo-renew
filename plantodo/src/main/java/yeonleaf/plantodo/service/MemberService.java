package yeonleaf.plantodo.service;

import org.springframework.stereotype.Service;
import org.springframework.validation.BindingResult;
import yeonleaf.plantodo.domain.Member;
import yeonleaf.plantodo.dto.MemberReqDto;

@Service
public interface MemberService {
    Member save(MemberReqDto memberReqDto);
    boolean isNotNewMember(String email);
    boolean login(MemberReqDto memberReqDto);
}
