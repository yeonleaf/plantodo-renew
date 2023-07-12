package yeonleaf.plantodo.unit.service.member;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import yeonleaf.plantodo.domain.Member;
import yeonleaf.plantodo.dto.MemberReqDto;
import yeonleaf.plantodo.exceptions.ArgumentValidationException;
import yeonleaf.plantodo.exceptions.ResourceNotFoundException;
import yeonleaf.plantodo.repository.MemoryMemberRepository;
import yeonleaf.plantodo.service.MemberServiceTestImpl;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

public class MemberServiceLoginUnitTest {

    private MemoryMemberRepository memberRepository;
    private MemberServiceTestImpl memberService;

    @BeforeEach
    void setUp() {
        memberRepository = new MemoryMemberRepository();
        memberService = new MemberServiceTestImpl(memberRepository);
    }

    /*
    * 컨트롤러에서 타입 / 형식 validation을 모두 통과했다고 전제함
    * */

    @Test
    @DisplayName("정상 로그인")
    void loginTest_normal() {
        MemberReqDto memberReqDto = new MemberReqDto("test@abc.co.kr", "13az$@fq");
        Member member = memberService.save(memberReqDto);
        assertThat(memberService.login(memberReqDto)).isEqualTo(member.getId());
    }

    @Test
    @DisplayName("비정상 로그인 (이메일이 일치하는 멤버가 없음)")
    void loginTest_abnormal_notFoundMemberByEmail() {
        MemberReqDto memberReqDto = new MemberReqDto("test@abc.co.kr", "13az$@fq");
        assertThrows(ResourceNotFoundException.class, () -> memberService.login(memberReqDto));
    }

    @Test
    @DisplayName("비정상 로그인 (이메일이 일치하는 멤버가 있지만 패스워드가 일치하지 않음)")
    void loginTest_abnormal_passwordNotMatch() {
        MemberReqDto memberReqDto1 = new MemberReqDto("test@abc.co.kr", "13az$@fq");
        memberService.save(memberReqDto1);

        MemberReqDto memberReqDto2 = new MemberReqDto("test@abc.co.kr", "rs%!az1q");
        assertThrows(ArgumentValidationException.class, () -> memberService.login(memberReqDto2));
    }

}
