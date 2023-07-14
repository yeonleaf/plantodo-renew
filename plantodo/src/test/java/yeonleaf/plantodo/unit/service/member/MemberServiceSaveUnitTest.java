package yeonleaf.plantodo.unit.service.member;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import yeonleaf.plantodo.dto.MemberReqDto;
import yeonleaf.plantodo.exceptions.DuplicatedMemberException;
import yeonleaf.plantodo.repository.MemoryMemberRepository;
import yeonleaf.plantodo.service.MemberServiceTestImpl;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;

public class MemberServiceSaveUnitTest {

    private MemoryMemberRepository memberRepository;
    private MemberServiceTestImpl memberService;

    @BeforeEach
    void setUp() {

        memberRepository = new MemoryMemberRepository();
        memberService = new MemberServiceTestImpl(memberRepository);

    }

    @Test
    @DisplayName("멤버 등록 - 정상 케이스")
    void saveTestNormal() {

        MemberReqDto memberReqDto = new MemberReqDto("test@abc.co.kr", "41zsa@#z");
        assertDoesNotThrow(() -> memberService.save(memberReqDto));

    }

    @Test
    @DisplayName("멤버 등록 - 비정상 케이스 (중복 회원)")
    void saveTestDuplicatedMember() {

        MemberReqDto memberReqDto1 = new MemberReqDto("test@abc.co.kr", "41zsa@#z");
        memberService.save(memberReqDto1);
        MemberReqDto memberReqDto2 = new MemberReqDto("test@abc.co.kr", "6s0@0aq%");
        assertThrows(DuplicatedMemberException.class, () -> memberService.save(memberReqDto2));

    }

}
