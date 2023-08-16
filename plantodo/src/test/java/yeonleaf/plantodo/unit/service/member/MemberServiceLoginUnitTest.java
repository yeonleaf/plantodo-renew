package yeonleaf.plantodo.unit.service.member;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.validation.BindingResult;
import yeonleaf.plantodo.ServiceTestConfig;
import yeonleaf.plantodo.dto.MemberReqDto;
import yeonleaf.plantodo.dto.MemberResDto;
import yeonleaf.plantodo.exceptions.ArgumentValidationException;
import yeonleaf.plantodo.exceptions.ResourceNotFoundException;
import yeonleaf.plantodo.repository.MemoryMemberRepository;
import yeonleaf.plantodo.service.MemberServiceTestImpl;
import yeonleaf.plantodo.controller.MemberController;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

/**
 * target : {@link MemberServiceTestImpl#login(MemberReqDto)}
 * target description : 입력받은 회원 정보가 DB에 있는 회원 정보와 같은지 확인하고 같다면 회원 id를 리턴한다.
 *                      리턴한 회원 id는 {@link MemberController#login(MemberReqDto, BindingResult)}에서 Jwt 토큰을 발행하기 위해 사용된다.
 * test description : 로그인 후 리턴받은 회원 id가 등록한 회원의 id와 같은지 확인한다.
 *                    입력받은 회원 정보가 컨트롤러에서 타입 체크를 거쳤다는 것을 전제로 한다.
 */
@ExtendWith(SpringExtension.class)
@ContextConfiguration(classes = ServiceTestConfig.class)
public class MemberServiceLoginUnitTest {

    @Autowired
    private MemoryMemberRepository memberRepository;

    @Autowired
    private MemberServiceTestImpl memberService;

    /**
     * 테스트 종료 후 데이터를 모두 삭제해서 롤백
     * (DuplicatedMemberException 발생 방지)
     */
    @AfterEach
    void clear() {
        memberRepository.clear();
    }

    @Test
    @DisplayName("정상 로그인의 경우 리턴받은 회원 id와 DB에 있는 회원의 실제 id가 일치한다.")
    void loginTest_normal() {

        MemberReqDto memberReqDto = new MemberReqDto("test@abc.co.kr", "13az$@fq");
        MemberResDto memberResDto = memberService.save(memberReqDto);
        assertThat(memberService.login(memberReqDto)).isEqualTo(memberResDto.getId());

    }

    @Test
    @Disabled
    @DisplayName("비정상 로그인 - 이메일이 일치하는 회원이 없는 경우 ResourceNotFoundException을 던지는지 확인한다.")
    void loginTest_abnormal_notFoundMemberByEmail() {

        MemberReqDto memberReqDto = new MemberReqDto("test@abc.co.kr", "13az$@fq");
        assertThrows(ResourceNotFoundException.class, () -> memberService.login(memberReqDto));

    }

    @Test
    @DisplayName("비정상 로그인 - 이메일이 일치하지만 패스워드가 일치하지 않는 경우 ArgumentValidationException을 던지는지 확인한다.")
    void loginTest_abnormal_passwordNotMatch() {

        MemberReqDto memberReqDto1 = new MemberReqDto("test@abc.co.kr", "13az$@fq");
        memberService.save(memberReqDto1);

        MemberReqDto memberReqDto2 = new MemberReqDto("test@abc.co.kr", "rs%!az1q");
        assertThrows(ArgumentValidationException.class, () -> memberService.login(memberReqDto2));

    }

}
