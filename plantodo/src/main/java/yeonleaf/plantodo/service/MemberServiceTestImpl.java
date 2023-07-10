package yeonleaf.plantodo.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.validation.BindingResult;
import yeonleaf.plantodo.domain.Member;
import yeonleaf.plantodo.dto.MemberReqDto;
import yeonleaf.plantodo.exceptions.ArgumentValidationException;
import yeonleaf.plantodo.exceptions.DuplicatedMemberException;
import yeonleaf.plantodo.exceptions.ResourceNotFoundException;
import yeonleaf.plantodo.repository.MemoryMemberRepository;
import yeonleaf.plantodo.validator.JoinFormatCheckValidator;

import java.util.List;

@RequiredArgsConstructor
public class MemberServiceTestImpl implements MemberService {

    private final MemoryMemberRepository memberRepository;

    @Override
    public Member save(MemberReqDto memberReqDto) {
        if (isNotNewMember(memberReqDto.getEmail())) {
            throw new DuplicatedMemberException();
        }
        return memberRepository.save(memberReqDto);
    }

    @Override
    public boolean isNotNewMember(String email) {
        return memberRepository.findByEmail(email).size() > 0;
    }

    @Override
    public boolean login(MemberReqDto memberReqDto) {
        List<Member> candidates = memberRepository.findByEmail(memberReqDto.getEmail());
        if (candidates.isEmpty()) {
            throw new ResourceNotFoundException();
        }
        if (!candidates.get(0).getPassword().equals(memberReqDto.getPassword())) {
            throw new ArgumentValidationException("password", "password가 일치하지 않습니다.");
        }
        return true;
    }
}
