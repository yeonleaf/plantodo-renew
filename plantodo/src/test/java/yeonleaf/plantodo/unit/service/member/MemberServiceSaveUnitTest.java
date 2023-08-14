package yeonleaf.plantodo.unit.service.member;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import yeonleaf.plantodo.ServiceTestConfig;
import yeonleaf.plantodo.dto.MemberReqDto;
import yeonleaf.plantodo.exceptions.DuplicatedMemberException;
import yeonleaf.plantodo.repository.MemoryMemberRepository;
import yeonleaf.plantodo.service.MemberServiceTestImpl;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;

/**
 * target : {@link MemberServiceTestImpl#save(MemberReqDto)}
 * target description : 중복된 회원이 없는 경우 회원을 저장하고 저장된 객체를 리턴한다.
 */
@ExtendWith(SpringExtension.class)
@ContextConfiguration(classes = ServiceTestConfig.class)
public class MemberServiceSaveUnitTest {

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
    @DisplayName("멤버 정상 등록 - 리턴받은 MemberResDto 객체가 회원 id를 포함하고 있는지 확인한다.")
    void saveTestNormal() {

        MemberReqDto memberReqDto = new MemberReqDto("test@abc.co.kr", "41zsa@#z");
        assertThat(memberService.save(memberReqDto).getId()).isNotNull();

    }

    @Test
    @DisplayName("멤버 비정상 등록 - 중복 회원이 있는 경우 DuplicatedMemberException을 던지는지 확인한다.")
    void saveTestDuplicatedMember() {

        MemberReqDto memberReqDto1 = new MemberReqDto("test@abc.co.kr", "41zsa@#z");
        memberService.save(memberReqDto1);
        MemberReqDto memberReqDto2 = new MemberReqDto("test@abc.co.kr", "6s0@0aq%");
        assertThrows(DuplicatedMemberException.class, () -> memberService.save(memberReqDto2));

    }

}
