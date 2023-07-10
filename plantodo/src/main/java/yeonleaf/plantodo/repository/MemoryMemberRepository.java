package yeonleaf.plantodo.repository;

import yeonleaf.plantodo.domain.Member;
import yeonleaf.plantodo.dto.MemberReqDto;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

public class MemoryMemberRepository {
    private Map<Long, Member> members = new HashMap<>();
    private Long id = 1L;

    public Member save(MemberReqDto memberReqDto) {
        Member member = new Member(memberReqDto);
        member.setId(id);
        members.put(id++, member);
        return member;
    }

    public List<Member> findByEmail(String email) {
        return members.values().stream().filter((m) -> m.getEmail().equals(email)).collect(Collectors.toList());
    }

    public Optional<Member> findById(Long id) {
        return Optional.ofNullable(members.get(id));
    }

    public void delete(Member member) {
        members.remove(member.getId());
    }
}
