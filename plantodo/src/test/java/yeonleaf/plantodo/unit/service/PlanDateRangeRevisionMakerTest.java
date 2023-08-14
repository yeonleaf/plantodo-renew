package yeonleaf.plantodo.unit.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import yeonleaf.plantodo.domain.Member;
import yeonleaf.plantodo.domain.Plan;
import yeonleaf.plantodo.dto.PlanUpdateReqDto;
import yeonleaf.plantodo.util.PlanDateRangeRevisionMaker;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.IntStream;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * target : {@link PlanDateRangeRevisionMaker#revise(PlanUpdateReqDto, Plan)}
 * target description : 수정 전 일정 범위와 수정 후 일정 범위를 마킹한 결과를 HashMap<LocalDate, Integer> 형태로 반환한다.
 *                      마킹한 결과는 일정 수정 로직{@link yeonleaf.plantodo.service.PlanService#update(PlanUpdateReqDto)}에서
 *                      범위에 벗어난 할 일을 삭제하거나 추가되어야 할 할 일을 생성하는 용도로 사용된다.
 *
 * test description :
 *                    1. 수정 전 일정 범위와 수정 후 일정 범위가 동일한지 확인한다. -> Empty Result
 *                    2. 범위가 서로 다르다면 마킹이 정상적으로 되었는지 확인한다.
 *                       min(oldStart, oldEnd, newStart, newEnd) ~ max(oldStart, oldEnd, newStart, newEnd) 사이의 LocalDate 중에서
 *                       값 2 : 수정 전 범위에만 속함
 *                       값 3 : 수정 후 범위에만 속함
 *                       값 5 : 수정 전, 수정 후 범위에 모두 속함
 */
public class PlanDateRangeRevisionMakerTest {

    private PlanDateRangeRevisionMaker revisionMaker;

    @BeforeEach
    void setUp() {
        revisionMaker = new PlanDateRangeRevisionMaker();
    }

    /**
     * 테스트 메이커
     * @param oldStart 수정 전 일정의 시작일
     * @param oldEnd 수정 전 일정의 종료일
     * @param newStart 수정 후 일정의 시작일
     * @param newEnd 수정 후 일정의 종료일
     * @return 수정 전 일정과 수정 후 일정의 범위를 마킹한 결과
     */
    HashMap<LocalDate, Integer> makeRevisionMakingTest(LocalDate oldStart, LocalDate oldEnd, LocalDate newStart, LocalDate newEnd) {
        Plan oldPlan = new Plan(1L, "title", oldStart, oldEnd, new Member(1L, "test@abc.co.kr", "d2dsc$e2"));
        PlanUpdateReqDto newPlan = new PlanUpdateReqDto(1L, "title", newStart, newEnd);
        return revisionMaker.revise(newPlan, oldPlan);
    }

    @Test
    @DisplayName("newStart == oldStart && newEnd == oldEnd일 경우 빈 HashMap을 반환한다.")
    void revisionMakingTest_allNewEqualToOld() {;

        Map<LocalDate, Integer> resultMap = makeRevisionMakingTest(
                LocalDate.of(2023, 7, 18),
                LocalDate.of(2023, 7, 25),
                LocalDate.of(2023, 7, 18),
                LocalDate.of(2023, 7, 25)
        );
        assertThat(resultMap).isEmpty();

    }

    @Test
    @DisplayName("oldStart = newStart && oldEnd < newEnd일 경우 " +
            "oldStart ~ oldEnd 범위는 5, oldEnd+1 ~ newEnd 범위는 3이다.")
    void revisionMakingTest_newStartEqualToOldStart_NewEndGreaterThanOldEnd() {

        Map<LocalDate, Integer> resultMap = makeRevisionMakingTest(
                LocalDate.of(2023, 7, 18),
                LocalDate.of(2023, 7, 25),
                LocalDate.of(2023, 7, 18),
                LocalDate.of(2023, 7, 29)
        );

        assertThat(resultMap).containsKeys(
                LocalDate.of(2023, 7, 26),
                LocalDate.of(2023, 7, 27),
                LocalDate.of(2023, 7, 28),
                LocalDate.of(2023, 7, 29)
        );

        IntStream.rangeClosed(18, 25).forEach(i -> assertThat(resultMap.get(LocalDate.of(2023, 7, i))).isEqualTo(5));
        IntStream.rangeClosed(26, 29).forEach(i -> assertThat(resultMap.get(LocalDate.of(2023, 7, i))).isEqualTo(3));

    }

    @Test
    @DisplayName("oldStart = newStart, oldEnd > newEnd일 경우 " +
            "oldStart ~ newEnd 범위는 5, newEnd+1 ~ oldEnd 범위는 2이다.")
    void revisionMakingTest_newStartEqualToNewStart_newEndGreaterThanOldEnd() {

        Map<LocalDate, Integer> resultMap = makeRevisionMakingTest(
                LocalDate.of(2023, 7, 18),
                LocalDate.of(2023, 7, 25),
                LocalDate.of(2023, 7, 18),
                LocalDate.of(2023, 7, 23)
        );

        IntStream.rangeClosed(18, 23).forEach(i -> assertThat(resultMap.get(LocalDate.of(2023, 7, i))).isEqualTo(5));
        IntStream.rangeClosed(24, 25).forEach(i -> assertThat(resultMap.get(LocalDate.of(2023, 7, i))).isEqualTo(2));

    }

    @Test
    @DisplayName("oldStart < newStart, oldEnd = newEnd일 경우 " +
            "oldStart ~ newStart-1 범위는 2, newStart ~ newEnd 범위는 5이다.")
    void revisionMakingTest_oldEndEqualToOldStart_newStartGreaterThanOldStart() {

        Map<LocalDate, Integer> resultMap = makeRevisionMakingTest(
                LocalDate.of(2023, 7, 18),
                LocalDate.of(2023, 7, 25),
                LocalDate.of(2023, 7, 20),
                LocalDate.of(2023, 7, 25)
        );

        IntStream.rangeClosed(18, 19).forEach(i -> assertThat(resultMap.get(LocalDate.of(2023, 7, i))).isEqualTo(2));
        IntStream.rangeClosed(20, 25).forEach(i -> assertThat(resultMap.get(LocalDate.of(2023, 7, i))).isEqualTo(5));

    }

    @Test
    @DisplayName("oldStart > newStart, oldEnd = newEnd일 경우 " +
            "newStart ~ oldStart-1 범위는 3, oldStart ~ oldEnd 범위는 5이다.")
    void revisionMakingTest_oldEndEqualToOldStart_oldStartGreaterThanNewStart() {

        Map<LocalDate, Integer> resultMap = makeRevisionMakingTest(
                LocalDate.of(2023, 7, 18),
                LocalDate.of(2023, 7, 25),
                LocalDate.of(2023, 7, 16),
                LocalDate.of(2023, 7, 25)
        );

        IntStream.rangeClosed(16, 17).forEach(i -> assertThat(resultMap.get(LocalDate.of(2023, 7, i))).isEqualTo(3));
        IntStream.rangeClosed(18, 25).forEach(i -> assertThat(resultMap.get(LocalDate.of(2023, 7, i))).isEqualTo(5));

    }

    @Test
    @DisplayName("oldStart < newStart < newEnd < oldEnd일 경우 " +
            "oldStart ~ newStart-1 범위와 newEnd+1 ~ oldEnd 범위는 2, newStart ~ newEnd 범위는 5")
    void revisionMakingTest_allLocalDatesAreDifferent_1() {

        Map<LocalDate, Integer> resultMap = makeRevisionMakingTest(
                LocalDate.of(2023, 7, 18),
                LocalDate.of(2023, 7, 25),
                LocalDate.of(2023, 7, 20),
                LocalDate.of(2023, 7, 23)
        );
        IntStream.rangeClosed(18, 19).forEach(i -> assertThat(resultMap.get(LocalDate.of(2023, 7, i))).isEqualTo(2));
        IntStream.rangeClosed(20, 23).forEach(i -> assertThat(resultMap.get(LocalDate.of(2023, 7, i))).isEqualTo(5));
        IntStream.rangeClosed(24, 25).forEach(i -> assertThat(resultMap.get(LocalDate.of(2023, 7, i))).isEqualTo(2));

    }

    @Test
    @DisplayName("newStart < oldStart < newEnd < oldEnd일 경우 " +
            "newStart ~ oldStart-1 범위는 3, oldStart ~ newEnd-1 범위는 5, newEnd+1 ~ oldEnd 범위는 2")
    void revisionMakingTest_allLocalDatesAreDifferent_2() {

        Map<LocalDate, Integer> resultMap = makeRevisionMakingTest(
                LocalDate.of(2023, 7, 18),
                LocalDate.of(2023, 7, 25),
                LocalDate.of(2023, 7, 16),
                LocalDate.of(2023, 7, 23)
        );
        IntStream.rangeClosed(16, 17).forEach(i -> assertThat(resultMap.get(LocalDate.of(2023, 7, i))).isEqualTo(3));
        IntStream.rangeClosed(18, 23).forEach(i -> assertThat(resultMap.get(LocalDate.of(2023, 7, i))).isEqualTo(5));
        IntStream.rangeClosed(24, 25).forEach(i -> assertThat(resultMap.get(LocalDate.of(2023, 7, i))).isEqualTo(2));

    }

    @Test
    @DisplayName("newStart < oldStart < oldEnd < newEnd일 경우 " +
            "newStart ~ oldStart-1 범위는 3, oldStart ~ oldEnd 범위는 5, oldEnd+1 ~ newEnd 범위는 3")
    void revisionMakingTest_allLocalDatesAreDifferent_3() {

        Map<LocalDate, Integer> resultMap = makeRevisionMakingTest(
                LocalDate.of(2023, 7, 18),
                LocalDate.of(2023, 7, 25),
                LocalDate.of(2023, 7, 16),
                LocalDate.of(2023, 7, 29)
        );
        IntStream.rangeClosed(16, 17).forEach(i -> assertThat(resultMap.get(LocalDate.of(2023, 7, i))).isEqualTo(3));
        IntStream.rangeClosed(18, 25).forEach(i -> assertThat(resultMap.get(LocalDate.of(2023, 7, i))).isEqualTo(5));
        IntStream.rangeClosed(26, 29).forEach(i -> assertThat(resultMap.get(LocalDate.of(2023, 7, i))).isEqualTo(3));

    }

    @Test
    @DisplayName("oldStart < newStart < oldEnd < newEnd일 경우 " +
            "oldStart ~ newStart-1 범위는 2, newStart ~ oldEnd 범위는 5, oldEnd+1 ~ newEnd 범위는 3")
    void revisionMakingTest_allLocalDatesAreDifferent_4() {

        Map<LocalDate, Integer> resultMap = makeRevisionMakingTest(
                LocalDate.of(2023, 7, 18),
                LocalDate.of(2023, 7, 25),
                LocalDate.of(2023, 7, 23),
                LocalDate.of(2023, 7, 29)
        );
        IntStream.rangeClosed(18, 22).forEach(i -> assertThat(resultMap.get(LocalDate.of(2023, 7, i))).isEqualTo(2));
        IntStream.rangeClosed(23, 25).forEach(i -> assertThat(resultMap.get(LocalDate.of(2023, 7, i))).isEqualTo(5));
        IntStream.rangeClosed(26, 29).forEach(i -> assertThat(resultMap.get(LocalDate.of(2023, 7, i))).isEqualTo(3));

    }

    @Test
    @DisplayName("newStart < newEnd < oldStart < oldEnd일 경우 " +
            "newStart ~ newEnd 범위는 3, newEnd+1 ~ oldStart-1 범위는 0, oldStart ~ oldEnd 범위는 2")
    void revisionMakingTest_allLocalDatesAreDifferent_5() {

        Map<LocalDate, Integer> resultMap = makeRevisionMakingTest(
                LocalDate.of(2023, 7, 18),
                LocalDate.of(2023, 7, 25),
                LocalDate.of(2023, 7, 13),
                LocalDate.of(2023, 7, 16)
        );
        IntStream.rangeClosed(13, 16).forEach(i -> assertThat(resultMap.get(LocalDate.of(2023, 7, i))).isEqualTo(3));
        IntStream.rangeClosed(17, 17).forEach(i -> assertThat(resultMap.get(LocalDate.of(2023, 7, i))).isEqualTo(0));
        IntStream.rangeClosed(18, 25).forEach(i -> assertThat(resultMap.get(LocalDate.of(2023, 7, i))).isEqualTo(2));

    }

    @Test
    @DisplayName("oldStart < oldEnd < newStart < newEnd일 경우 " +
            "oldStart ~ oldEnd 범위는 2, oldEnd+1 ~ newStart-1 범위는 0, newStart ~ newEnd 범위는 3")
    void revisionMakingTest_allLocalDatesAreDifferent_6() {

        Map<LocalDate, Integer> resultMap = makeRevisionMakingTest(
                LocalDate.of(2023, 7, 18),
                LocalDate.of(2023, 7, 25),
                LocalDate.of(2023, 7, 29),
                LocalDate.of(2023, 7, 31)
        );
        IntStream.rangeClosed(18, 25).forEach(i -> assertThat(resultMap.get(LocalDate.of(2023, 7, i))).isEqualTo(2));
        IntStream.rangeClosed(26, 28).forEach(i -> assertThat(resultMap.get(LocalDate.of(2023, 7, i))).isEqualTo(0));
        IntStream.rangeClosed(29, 31).forEach(i -> assertThat(resultMap.get(LocalDate.of(2023, 7, i))).isEqualTo(3));

    }

}
