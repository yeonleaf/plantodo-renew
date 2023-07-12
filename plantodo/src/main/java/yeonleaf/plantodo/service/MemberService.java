package yeonleaf.plantodo.service;

import org.springframework.stereotype.Service;
import yeonleaf.plantodo.domain.Member;
import yeonleaf.plantodo.dto.MemberReqDto;

import java.util.Optional;

@Service
public interface MemberService {

    Member save(MemberReqDto memberReqDto);
    Optional<Member> findById(Long id);
    boolean isNotNewMember(String email);
    Long login(MemberReqDto memberReqDto);
    void delete(Member member);

}
