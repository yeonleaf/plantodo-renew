package yeonleaf.plantodo.validator;

import org.springframework.stereotype.Component;
import org.springframework.validation.Errors;
import org.springframework.validation.Validator;
import yeonleaf.plantodo.dto.MemberReqDto;

import java.util.*;

@Component
public class JoinFormatCheckValidator implements Validator {

    @Override
    public boolean supports(Class<?> clazz) {
        return false;
    }

    @Override
    public void validate(Object target, Errors errors) {
        MemberReqDto memberReqDto = (MemberReqDto) target;

        /*비밀번호 검증*/
        String password = memberReqDto.getPassword();
        validatePassword(errors, password);

        /*이메일 검증*/
        String email = memberReqDto.getEmail();
        String[] split = email.split("@");

        if (split.length != 2 || split[0].equals("") || split[1].equals("")) {
            errors.rejectValue("email", "format","email 형식을 지켜야 합니다.");
        }

        String local = split[0];
        validateEmailPart(errors, local);

        String domain = split[1];
        validateEmailPart(errors, domain);
    }

    private void validateEmailPart(Errors errors, String part) {
        boolean containsInvalidCharsets = part.matches(".*[^A-Za-z0-9._\\-]+.*");
        if (containsInvalidCharsets) {
            errors.rejectValue("email", "format","email에서 @ 앞뒤에는 영문 대소문자, 숫자, 특수문자(-, _, .)만 사용할 수 있습니다.");
        }

        List<Character> exMarks = Arrays.asList('.', '-', '_');
        char firstChar = part.charAt(0);
        char lastChar = part.charAt(part.length() - 1);
        if (exMarks.contains(firstChar) || exMarks.contains(lastChar)) {
            errors.rejectValue("email", "format","email에서 @를 기준으로 나뉜 각 부분에 특수문자를 맨 앞, 혹은 맨 뒤에 넣을 수 없습니다.");
        }
        if (containsConsecutiveDots(part)) {
            errors.rejectValue("email", "format", "email에서 @를 기준으로 나뉜 각 부분에 특수기호 .을 연속해서 사용할 수 없습니다.");
        }
    }

    private boolean containsConsecutiveDots(String word) {
        int cnt = 0;
        for (int i = 0; i < word.length(); i++) {
            if (word.charAt(i) == '.') {
                cnt++;
                if (cnt >= 2) {
                    return true;
                }
            } else {
                cnt = 0;
            }
        }
        return false;
    }

    private void validatePassword(Errors errors, String password) {
        if (password.length() < 8 || password.length() > 20) {
            errors.rejectValue("password", "length", "password는 8글자 이상 20글자 이하여야 합니다.");
        }

        if (password.matches(".*[ㄱ-ㅎㅏ-ㅣ가-힣]+.*")) {
            errors.rejectValue("password", "format", "password에는 영문자/숫자/특수문자만 사용해야 합니다.");
        }

        boolean containsAlphabets = password.matches(".*[A-Za-z]+.*");
        boolean containsNumbers = password.matches(".*[0-9]+.*");
        boolean containsExMarks = password.matches(".*[_!@#$%^&*()]+.*");
        if (!containsAlphabets || !containsNumbers || !containsExMarks) {
            errors.rejectValue("password", "format", "password에는 영문자, 숫자, 특수문자를 모두 사용해야 합니다.");
        }

        boolean containsForbiddenExMarks = password.matches(".*[^A-Za-z0-9ㄱ-ㅎㅏ-ㅣ가-힣_!@#$%^&*()]+.*");
        if (containsForbiddenExMarks) {
            errors.rejectValue("password", "format", "password에는 특수문자로 _, !, @, #, $, %, ^, &, *, (, ) 외의 글자를 사용할 수 없습니다.");
        }

        if (containsMultipleSameChars(password)) {
            errors.rejectValue("password", "format", "password에는 같은 영문자/숫자/특수문자를 네 개 이상 사용할 수가 없습니다.");
        }

        if (containsConsecutiveChars(password)) {
            errors.rejectValue("password", "format", "password에는 연속된 숫자나 영문자의 나열을 네 개 이상 사용할 수 없습니다.");
        }

        if (containsForbiddenWords(password)) {
            errors.rejectValue("password", "forbidden", "password에는 너무 쉬운 단어를 사용할 수 없습니다.");
        }
    }

    private boolean containsForbiddenWords(String password) {
        String[] forbiddenWords = new String[]{"password", "adobe", "qwer", "qwert", "qwerty", "love", "admin", "shadow", "sunshine", "letmein"};
        for (String word : forbiddenWords) {
            if (password.contains(word)) {
                return true;
            }
        }
        return false;
    }

    private boolean containsConsecutiveChars(String password) {
        char[] chars = password.toCharArray();
        char pv = ' ';
        int cnt = 0;
        for (int i = 0; i < chars.length; i++) {
            if (i == 0) {
                pv = chars[i];
                cnt = 1;
                continue;
            }
            char cv = (char) (pv + 1);
            if (isAlphabetOrNumber(pv) && isAlphabetOrNumber(cv) && cv == chars[i]) {
                cnt++;
                if (cnt > 3) {
                    return true;
                }
            } else {
                cnt = 1;
            }
            pv = chars[i];
        }
        return false;
    }

    private boolean isAlphabetOrNumber(char c) {
        boolean isNumber = (48 <= c && c <= 57);
        boolean isUpperAlphabet = (65 <= c && c <= 90);
        boolean isLowerAlphabet = (97 <= c && c <= 122);
        return isNumber || isUpperAlphabet || isLowerAlphabet;
    }

    private boolean containsMultipleSameChars(String password) {
        char[] chars = password.toCharArray();
        char pv = ' ';
        int cnt = 0;
        for (int i = 0; i < chars.length; i++) {
            if (i == 0) {
                pv = chars[i];
                cnt = 1;
                continue;
            }
            if (pv == chars[i]) {
                cnt++;
                if (cnt > 3) {
                    return true;
                }
            } else {
                cnt = 1;
                pv = chars[i];
            }
        }
        return false;
    }

}
