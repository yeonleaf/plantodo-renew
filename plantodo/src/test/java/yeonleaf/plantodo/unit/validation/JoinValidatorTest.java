package yeonleaf.plantodo.unit.validation;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.validation.BindingResult;
import yeonleaf.plantodo.dto.MemberReqDto;
import yeonleaf.plantodo.validator.JoinFormatCheckValidator;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

public class JoinValidatorTest {
    private JoinFormatCheckValidator validator;
    private BindingResult bindingResult;

    @BeforeEach
    void setUp() {
        bindingResult = spy(BindingResult.class);
        validator = new JoinFormatCheckValidator();
    }

    void validateThrowException(String email, String password) {
        MemberReqDto member = new MemberReqDto(email, password);
        doThrow(IllegalArgumentException.class).when(bindingResult).rejectValue(anyString(), anyString(), anyString());
        assertThatThrownBy(() -> validator.validate(member, bindingResult)).isInstanceOf(IllegalArgumentException.class);
    }

    void validateNoException(String email, String password) {
        MemberReqDto member = new MemberReqDto(email, password);
        doThrow(IllegalArgumentException.class).when(bindingResult).rejectValue(anyString(), anyString(), anyString());
        assertDoesNotThrow(() -> validator.validate(member, bindingResult));
    }

    @Test
    @DisplayName("모든 조건을 만족함 (exception이 발생하면 안 됨)")
    void allClearTest() {
        validateNoException("test@abc.co.kr", "a184@45b");
    }

    /**
     * 비밀번호 validation 규정
     * 0. 8글자 이상
     * 1. 영문자, 숫자, 특수문자만 가능 (한글 안 됨)
     * 2. 영문자, 숫자, 특수문자가 모두 섞여있어야 함
     * 3. 같은 숫자가 두 개 이상 연달아 있으면 안됨 ex) 111 000
     * 4. 연속된 숫자 / 영문자의 나열이 세 개 이상 있으면 안 됨 ex) abc 123
     * 5. 키보드 자판순으로 연속된 글자의 나열이 세 개 이상 있으면 안 됨
     * 6. 다음 단어가 들어있으면 안 됨 ("password", "adobe", "qwer", "qwert", "qwerty", "love", "admin", "shadow", "sunshine", "letmein")
     */
    @Test
    @DisplayName("다른 조건을 모두 만족하고 password 길이만 8글자 미만이거나 20글자 초과")
    void passwordTest1() throws IllegalAccessException {
        validateThrowException("test@abc.co.kr", "ac16@!");
        validateThrowException("test@abc.co.kr", "1br@2c");
        validateThrowException("test@abc.co.kr", "zc1@(bqo25!AzG9@mR!_4");
        validateThrowException("test@abc.co.kr", "$1zfqA8@39Zkq$$!zhyo8");
    }

    @Test
    @DisplayName("다른 조건을 모두 만족하고 영문자/숫자/특수문자만 가능한 조건을 만족하지 못함")
    void passwordTest2() {
        validateThrowException("test@abc.co.kr", "박176a@b#");
        validateThrowException("test@abc.co.kr", "27a0a남@b");
    }

    @Test
    @DisplayName("다른 조건을 모두 만족하고 영문자/숫자/특수문자가 모두 섞여있어야 한다는 조건을 만족하지 못함 (특수문자가 없음)")
    void passwordTest3() {
        /*특수문자가 없음*/
        validateThrowException("test@abc.co.kr", "a1b2c3d4");
        validateThrowException("test@abc.co.kr", "173zyx4b");

        /*영문자가 없음*/
        validateThrowException("test@abc.co.kr", "@16!27$6");
        validateThrowException("test@abc.co.kr", "173zyx4b");

        /*숫자가 없음*/
        validateThrowException("test@abc.co.kr", "@ar#l%qo");
        validateThrowException("test@abc.co.kr", "om(*rta^");
    }

    @Test
    @DisplayName("다른 조건을 모두 만족하고 특수문자는 ([ _, !, @, #, $, %, ^, &, *, (, ) ] 중 하나 이상)만 사용해야 한다는 조건을 만족하지 못함")
    void passwordTest4() {
        validateThrowException("test@abc.co.kr", "a1-b7qz@");
        validateThrowException("test@abc.co.kr", "a1b7>qz@");
        validateThrowException("test@abc.co.kr", "a1b7qz}@");
    }

    @Test
    @DisplayName("다른 조건을 모두 만족하고 같은 영문자/숫자/특수문자가 세 개 이상 연달아 있으면 안 된다는 조건을 만족하지 못함")
    void passwordTest5() {
        validateThrowException("test@abc.co.kr", "1111m@e)");
        validateThrowException("test@abc.co.kr", "em1dddd*");
        validateThrowException("test@abc.co.kr", "6az3%%%%");
    }

    @Test
    @DisplayName("다른 조건을 모두 만족하고 연속된 영문자나 숫자의 나열이 세 개 이상 있으면 안 된다는 조건을 만족하지 못함")
    void passwordTest6() {
        /*영문자*/
        validateThrowException("test@abc.co.kr", "abcd!a$7");
        validateThrowException("test@abc.co.kr", "^!wxyz12");

        /*숫자*/
        validateThrowException("test@abc.co.kr", "a12#1234");
        validateThrowException("test@abc.co.kr", "b6789#z!");
    }

    @Test
    @DisplayName("모든 조건을 만족함 (연속된 영문자/숫자 4개 이상 사용 불가능 조건 edge case)")
    void passwordTest7() {
        /*특수문자 4개가 연달아 나오는 경우*/
        validateNoException("test@abc.co.kr", "#$%&DEL1");

        /*영소문자 -> 영소문자*/
        validateNoException("test@abc.co.kr", "xyza!a$7");

        /*영대문자 -> 영대문자*/
        validateNoException("test@abc.co.kr", "a#^ZABC1");

        /*특수문자 -> 영대문자*/
        validateNoException("test@abc.co.kr", "@ABCa!%6");
    }

    @Test
    @DisplayName("다른 조건을 만족하고 쉬운 단어를 넣을 수 없다는 규칙을 만족하지 못함")
    void passwordTest8() {
        validateThrowException("test@abc.co.kr", "qwert15!");
        validateThrowException("test@abc.co.kr", "!4azlove");
        validateThrowException("test@abc.co.kr", "1admin5#");
    }

    /**
     * 이메일 validation 규칙
     * 1. 이메일 형식을 지켜야 함 (로컬 파트@도메인 파트)
     * 2. 각 파트는 다음과 같은 규칙을 따라야 함
     * 2-1. A-Z a-z 0-9, (-), (_), (.) 만 사용 가능
     * 2-1-1. (-), (_)은 각 파트의 첫 번째나 마지막 문자에 사용할 수 없음
     * 2-1-2. (.)은 연달아서 사용할 수 없음 (ex john..doe@example.com / john.doe@example..com)
     */

    @Test
    @DisplayName("다른 규칙을 만족하고 이메일 형식을 지켜야 한다는 규칙을 만족하지 못함")
    void emailTest2() {
        /*@ 아예 없는 경우*/
        validateThrowException("testabccokr", "a1b2#3d4");
        validateThrowException("test.abc.com", "a1b2#3d4");
        validateThrowException("test_abc.com", "a1b2#3d4");

        /*@ 하나인데 위치가 이상한 경우 (맨 앞 혹은 맨 끝)*/
        validateThrowException("@testabccom", "a1b2#3d4");
        validateThrowException("testabccom@", "a1b2#3d4");

        /*@ 두 개 이상인 경우*/
        validateThrowException("test@abc@com", "a1b2#3d4");
        validateThrowException("test@@abccom", "a1b2#3d4");
        validateThrowException("test@@abc@com", "a1b2#3d4");
    }

    @Test
    @DisplayName("다른 규칙을 만족하고 각 파트에 A-Z, a-z, 0-9, -, _, .만 사용 가능하다는 규칙을 만족하지 못함")
    void emailTest3() {
        /*로컬 파트*/
        validateThrowException("tes한t@abc.co.kr", "a1b2#3d4");
        validateThrowException("tes%t@abc.co.kr", "a1b2#3d4");
        validateThrowException("t/est@abc.co.kr", "a1b2#3d4");
        validateThrowException("t☆est@abc.co.kr", "a1b2#3d4");

        /*도메인 파트*/
        validateThrowException("test@abc.한.kr", "a1b2#3d4");
        validateThrowException("test@abc#kr", "a1b2#3d4");
        validateThrowException("test@a^kr", "a1b2#3d4");
        validateThrowException("test@abc☆", "a1b2#3d4");
    }

    @Test
    @DisplayName("다른 규칙을 만족하고 각 파트에 특수문자를 맨 앞, 맨 뒤에 사용할 수 없다는 규칙을 만족하지 못함")
    void emailTest4() {
        /*로컬 파트*/
        /*앞 or 뒤*/
        validateThrowException("-test@abc.co.kr", "a1b2#3d4");
        validateThrowException("test-@abc.co.kr", "a1b2#3d4");
        validateThrowException("_test@abc.co.kr", "a1b2#3d4");
        validateThrowException("test_@abc.co.kr", "a1b2#3d4");
        validateThrowException(".test@abc.co.kr", "a1b2#3d4");
        validateThrowException("test.@abc.co.kr", "a1b2#3d4");

        /*앞 and 뒤*/
        validateThrowException(".test.@abc.co.kr", "a1b2#3d4");
        validateThrowException("-test-@abc.co.kr", "a1b2#3d4");
        validateThrowException("_test_@abc.co.kr", "a1b2#3d4");
        validateThrowException(".test-@abc.co.kr", "a1b2#3d4");
        validateThrowException("-test.@abc.co.kr", "a1b2#3d4");

        /*특수문자만 있는 경우*/
        validateThrowException(".@abc.co.kr", "a1b2#3d4");
        validateThrowException("-@abc.co.kr", "a1b2#3d4");
        validateThrowException("_@abc.co.kr", "a1b2#3d4");
        validateThrowException(".-@abc.co.kr", "a1b2#3d4");
        validateThrowException("._@abc.co.kr", "a1b2#3d4");
        validateThrowException("-_@abc.co.kr", "a1b2#3d4");

        /*도메인 파트*/
        /*앞 or 뒤*/
        validateThrowException("test@.bc.com", "a1b2#3d4");
        validateThrowException("test@-bc.com", "a1b2#3d4");
        validateThrowException("test@_bc.com", "a1b2#3d4");
        validateThrowException("test@bc.com.", "a1b2#3d4");
        validateThrowException("test@bc.com-", "a1b2#3d4");
        validateThrowException("test@bc.com_", "a1b2#3d4");

        /*앞 and 뒤*/
        validateThrowException("test@-bc.com-", "a1b2#3d4");
        validateThrowException("test@_bc.com-", "a1b2#3d4");
        validateThrowException("test@_bc.com.", "a1b2#3d4");

        /*특수문자만 있는 경우*/
        validateThrowException("test@_-", "a1b2#3d4");
        validateThrowException("test@.", "a1b2#3d4");
        validateThrowException("test@-", "a1b2#3d4");
        validateThrowException("test@_", "a1b2#3d4");

    }

    @Test
    @DisplayName("다른 규칙을 만족하고 각 파트에 .을 연달아서 쓸 수 없다는 규칙을 만족하지 않음")
    void emailTest5() {
        /*로컬 파트*/
        validateThrowException("te..st@abc.co.kr", "a1b2#3d4");
        validateThrowException("t...est@abc.co.kr", "a1b2#3d4");

        /*도메인 파트*/
        validateThrowException("test@abc.co..kr", "a1b2#3d4");
        validateThrowException("test@abc..co.kr", "a1b2#3d4");
    }
}
