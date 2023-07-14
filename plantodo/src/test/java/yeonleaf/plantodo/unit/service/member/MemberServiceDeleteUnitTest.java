package yeonleaf.plantodo.unit.service.member;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import yeonleaf.plantodo.domain.Member;
import yeonleaf.plantodo.dto.MemberReqDto;
import yeonleaf.plantodo.repository.MemoryMemberRepository;
import yeonleaf.plantodo.service.MemberServiceTestImpl;

import static org.junit.jupiter.api.Assertions.assertFalse;

public class MemberServiceDeleteUnitTest {

    private MemoryMemberRepository memberRepository;
    private MemberServiceTestImpl memberService;

    @BeforeEach
    void setUp() {

        memberRepository = new MemoryMemberRepository();
        memberService = new MemberServiceTestImpl(memberRepository);

    }

    @Test
    @DisplayName("정상 삭제")
    void deleteTestNormal() {

        MemberReqDto memberReqDto = new MemberReqDto("test@abc.co.kr", "1d$%saf2");
        Member member = memberService.save(memberReqDto);

        memberService.delete(member);

        assertFalse(memberService.findById(member.getId()).isPresent());

    }

}
