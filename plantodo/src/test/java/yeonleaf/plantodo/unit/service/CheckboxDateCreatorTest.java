package yeonleaf.plantodo.unit.service;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import yeonleaf.plantodo.domain.Member;
import yeonleaf.plantodo.domain.Plan;
import yeonleaf.plantodo.domain.Checkbox;
import yeonleaf.plantodo.dto.RepInputDto;
import yeonleaf.plantodo.util.CheckboxDateCreator;

import java.time.LocalDate;
import java.util.Arrays;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * target : {@link CheckboxDateCreator#create(Plan, RepInputDto)}
 * target description : {@link Plan}, {@link RepInputDto} 정보를 바탕으로 {@link Checkbox}를 생성할 수 있는 날짜 리스트를 리턴
 *                      {@see GroupService#save}에서 사용
 *
 * test description : 날짜 리스트의 사이즈가 예측한 것과 같은지 검증
 */
public class CheckboxDateCreatorTest {

    private List<String> makeArrToList(String... target) {
        return Arrays.asList(target);
    }

    /**
     * 테스트 메이커
     * @param start (LocalDate) 일정 시작일
     * @param end (LocalDate) 일정 종료일
     * @param repOption (int) 할 일이 반복되는 양상을 설정하는 옵션
     * @param repValue (List<String>) 할 일이 반복되는 주기를 설정하는 옵션
     * @param expectedCnt 결과 리스트의 사이즈를 예측한 값
     */
    private void makeCreateTestRepOption(LocalDate start, LocalDate end, int repOption, List<String> repValue, int expectedCnt) {

        // given
        Member member = new Member("test@abc.co.kr", "3d^$a2df");
        member.setId(1L);
        Plan plan = new Plan("plan", start, end, member);
        RepInputDto repInputDto = new RepInputDto(repOption, repValue);

        // when
        List<LocalDate> dates = CheckboxDateCreator.create(plan, repInputDto);

        // then
        assertThat(dates.size()).isEqualTo(expectedCnt);

    }

    @Test
    @DisplayName("repOption == 1, start < end인 경우 start ~ end 사이의 모든 날짜를 리턴한다.")
    void createTestRepOption1_EndGreaterThanStart() {

        makeCreateTestRepOption(LocalDate.of(2023, 7, 18),
                LocalDate.of(2023, 7, 31), 1, makeArrToList(), 14);

    }

    @Test
    @DisplayName("repOption = 1, start = end인 경우 start 하나를 리턴한다.")
    void createTestRepOption1_EndEqualToStart() {

        makeCreateTestRepOption(LocalDate.of(2023, 7, 18),
                LocalDate.of(2023, 7, 18), 1, makeArrToList(), 1);

    }


    @Test
    @DisplayName("repOption = 2, start < end인 경우 start ~ end 사이의 날짜 중에서 지정한 간격에 해당하는 날짜만 리턴한다.")
    void createTestRepOption2_EndGreaterThanStart() {

        makeCreateTestRepOption(LocalDate.of(2023, 7, 18),
                LocalDate.of(2023, 7, 31), 2, makeArrToList("2"), 7);
        makeCreateTestRepOption(LocalDate.of(2023, 7, 18),
                LocalDate.of(2023, 7, 20), 2, makeArrToList("2"), 2);
        makeCreateTestRepOption(LocalDate.of(2023, 7, 18),
                LocalDate.of(2023, 7, 19), 2, makeArrToList("2"), 1);

    }

    @Test
    @DisplayName("repOption = 2, start = end인 경우 start 하나를 리턴한다.")
    void createTestRepOption2L_EndEqualToStart() {

        makeCreateTestRepOption(LocalDate.of(2023, 7, 18),
                LocalDate.of(2023, 7, 18), 2, makeArrToList("2"), 1);

    }

    @Test
    @DisplayName("repOption = 3, start < end인 경우 start ~ end 사이의 날짜 중에서 지정한 요일에 해당하는 날짜만 리턴한다.")
    void createTestRepOption3_EndGreaterThanStart() {

        makeCreateTestRepOption(LocalDate.of(2023, 7, 18),
                LocalDate.of(2023, 7, 31), 3, makeArrToList("월", "수", "금"), 6);
        makeCreateTestRepOption(LocalDate.of(2023, 7, 18),
                LocalDate.of(2023, 7, 31), 3, makeArrToList("월", "화", "수", "목", "금", "토", "일"), 14);
        makeCreateTestRepOption(LocalDate.of(2023, 7, 18),
                LocalDate.of(2023, 7, 31), 3, makeArrToList("월"), 2);

    }

    @Test
    @DisplayName("repOption = 3, start = end인 경우 start의 요일이 지정한 요일에 포함되면 리턴하고 아니면 리턴하지 않는다.")
    void createTestRepOption3L_EndEqualToStart() {

        makeCreateTestRepOption(LocalDate.of(2023, 7, 18),
                LocalDate.of(2023, 7, 18), 3, makeArrToList("월", "수", "금"), 0);

    }

}
