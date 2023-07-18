package yeonleaf.plantodo.unit.service;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import yeonleaf.plantodo.domain.Member;
import yeonleaf.plantodo.domain.Plan;
import yeonleaf.plantodo.dto.RepInputDto;
import yeonleaf.plantodo.util.CheckboxDateCreator;

import java.time.LocalDate;
import java.util.Arrays;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

public class CheckboxDateCreatorTest {

    private List<String> makeArrToList(String... target) {
        return Arrays.asList(target);
    }

    private void makeCreateTestRepOption(LocalDate start, LocalDate end, Long repOption, List<String> repValue, int expectedCnt) {

        Member member = new Member("test@abc.co.kr", "3d^$a2df");
        member.setId(1L);
        Plan plan = new Plan("plan", start, end, member);
        RepInputDto repInputDto = new RepInputDto(repOption, repValue);

        List<LocalDate> dates = CheckboxDateCreator.create(plan, repInputDto);

        assertThat(dates.size()).isEqualTo(expectedCnt);

    }

    @Test
    @DisplayName("repOption = 1L, start < end")
    void createTestRepOption1L_EndGreaterThanStart() {

        makeCreateTestRepOption(LocalDate.of(2023, 7, 18),
                LocalDate.of(2023, 7, 31), 1L, makeArrToList(), 14);

    }

    @Test
    @DisplayName("repOption = 1L, start = end")
    void createTestRepOption1L_EndEqualToStart() {

        makeCreateTestRepOption(LocalDate.of(2023, 7, 18),
                LocalDate.of(2023, 7, 18), 1L, makeArrToList(), 1);

    }


    @Test
    @DisplayName("repOption = 2L, start < end")
    void createTestRepOption2L_EndGreaterThanStart() {

        makeCreateTestRepOption(LocalDate.of(2023, 7, 18),
                LocalDate.of(2023, 7, 31), 2L, makeArrToList("2"), 7);
        makeCreateTestRepOption(LocalDate.of(2023, 7, 18),
                LocalDate.of(2023, 7, 20), 2L, makeArrToList("2"), 2);
        makeCreateTestRepOption(LocalDate.of(2023, 7, 18),
                LocalDate.of(2023, 7, 19), 2L, makeArrToList("2"), 1);

    }

    @Test
    @DisplayName("repOption = 2L, start = end")
    void createTestRepOption2L_EndEqualToStart() {

        makeCreateTestRepOption(LocalDate.of(2023, 7, 18),
                LocalDate.of(2023, 7, 18), 2L, makeArrToList("2"), 1);

    }

    @Test
    @DisplayName("repOption = 3L, start < end")
    void createTestRepOption3L_EndGreaterThanStart() {

        makeCreateTestRepOption(LocalDate.of(2023, 7, 18),
                LocalDate.of(2023, 7, 31), 3L, makeArrToList("월", "수", "금"), 6);
        makeCreateTestRepOption(LocalDate.of(2023, 7, 18),
                LocalDate.of(2023, 7, 31), 3L, makeArrToList("월", "화", "수", "목", "금", "토", "일"), 14);
        makeCreateTestRepOption(LocalDate.of(2023, 7, 18),
                LocalDate.of(2023, 7, 31), 3L, makeArrToList("월"), 2);

    }

    @Test
    @DisplayName("repOption = 3L, start = end")
    void createTestRepOption3L_EndEqualToStart() {

        makeCreateTestRepOption(LocalDate.of(2023, 7, 18),
                LocalDate.of(2023, 7, 18), 3L, makeArrToList("월", "수", "금"), 0);

    }

}
