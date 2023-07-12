package yeonleaf.plantodo.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import yeonleaf.plantodo.domain.Member;
import yeonleaf.plantodo.dto.MemberReqDto;
import yeonleaf.plantodo.exceptions.ArgumentValidationException;
import yeonleaf.plantodo.exceptions.DuplicatedMemberException;
import yeonleaf.plantodo.exceptions.ResourceNotFoundException;
import yeonleaf.plantodo.repository.MemberRepository;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class MemberServiceImpl implements MemberService {

    private final MemberRepository memberRepository;

    @Override
    public Member save(MemberReqDto memberReqDto) {
        if (isNotNewMember(memberReqDto.getEmail())) {
            throw new DuplicatedMemberException("이미 이메일이 있음");
        }
        return memberRepository.save(new Member(memberReqDto));
    }

    @Override
    public boolean isNotNewMember(String email) {
        return memberRepository.findByEmail(email).size() > 0;
    }

    @Override
    public Long login(MemberReqDto memberReqDto) {
        List<Member> candidates = memberRepository.findByEmail(memberReqDto.getEmail());
        if (candidates.isEmpty()) {
            throw new ResourceNotFoundException();
        }
        if (!candidates.get(0).getPassword().equals(memberReqDto.getPassword())) {
            throw new ArgumentValidationException("password", "password가 일치하지 않습니다.");
        }
        return candidates.get(0).getId();
    }

    @Override
    public Optional<Member> findById(Long id) {
        return memberRepository.findById(id);
    }

    @Override
    public void delete(Member member) {
        memberRepository.delete(member);
    }

}
