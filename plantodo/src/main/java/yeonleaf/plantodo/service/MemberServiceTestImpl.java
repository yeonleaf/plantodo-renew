package yeonleaf.plantodo.service;

import lombok.RequiredArgsConstructor;
import yeonleaf.plantodo.domain.Member;
import yeonleaf.plantodo.dto.MemberReqDto;
import yeonleaf.plantodo.dto.MemberResDto;
import yeonleaf.plantodo.exceptions.ArgumentValidationException;
import yeonleaf.plantodo.exceptions.DuplicatedMemberException;
import yeonleaf.plantodo.exceptions.ResourceNotFoundException;
import yeonleaf.plantodo.repository.MemoryMemberRepository;
import yeonleaf.plantodo.repository.MemoryRepository;

import java.util.List;
import java.util.Optional;

@RequiredArgsConstructor
public class MemberServiceTestImpl implements MemberService {

    private final MemoryMemberRepository memberRepository;

    @Override
    public MemberResDto save(MemberReqDto memberReqDto) {
        if (isNotNewMember(memberReqDto.getEmail())) {
            throw new DuplicatedMemberException();
        }
        Member member = memberRepository.save(new Member(memberReqDto));
        return new MemberResDto(member);
    }

    @Override
    public boolean isNotNewMember(String email) {
        return !memberRepository.findByEmail(email).isEmpty();
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
    public MemberResDto findById(Long id) {
        Member member = memberRepository.findById(id).orElseThrow(ResourceNotFoundException::new);
        return new MemberResDto(member);
    }

    @Override
    public void delete(Member member) {
        memberRepository.delete(member);
    }

}
