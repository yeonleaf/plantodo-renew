package yeonleaf.plantodo.service;

import org.springframework.stereotype.Service;
import yeonleaf.plantodo.domain.Member;
import yeonleaf.plantodo.dto.MemberReqDto;
import yeonleaf.plantodo.dto.MemberResDto;

import java.util.Optional;

@Service
public interface MemberService {

    MemberResDto save(MemberReqDto memberReqDto);
    MemberResDto findById(Long id);
    boolean isNotNewMember(String email);
    Long login(MemberReqDto memberReqDto);
    void delete(Member member);

}
