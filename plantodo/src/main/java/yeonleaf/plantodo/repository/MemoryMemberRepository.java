package yeonleaf.plantodo.repository;

import yeonleaf.plantodo.domain.Member;
import yeonleaf.plantodo.dto.MemberReqDto;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

public class MemoryMemberRepository extends MemoryRepository<Member> {

    private Map<Long, Member> data = new HashMap<>();
    private Long id = 1L;

    @Override
    public Member save(Member member) {
        member.setId(id);
        data.put(id++, member);
        return member;
    }

    public List<Member> findByEmail(String email) {
        return data.values().stream().filter((m) -> m.getEmail().equals(email)).collect(Collectors.toList());
    }

    public Optional<Member> findById(Long id) {
        return Optional.ofNullable(data.get(id));
    }

    public void delete(Member member) {
        data.remove(member.getId());
    }

}
