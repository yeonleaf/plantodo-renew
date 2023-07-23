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

public class PlanDateRangeRevisionMakerTest {

    private PlanDateRangeRevisionMaker revisionMaker;

    @BeforeEach
    void setUp() {
        revisionMaker = new PlanDateRangeRevisionMaker();
    }

    HashMap<LocalDate, Integer> makeRevisionMakingTest(LocalDate oldStart, LocalDate oldEnd, LocalDate newStart, LocalDate newEnd) {
        Plan oldPlan = new Plan(1L, "title", oldStart, oldEnd, new Member(1L, "test@abc.co.kr", "d2dsc$e2"));
        PlanUpdateReqDto newPlan = new PlanUpdateReqDto(1L, "title", newStart, newEnd);
        return revisionMaker.revise(newPlan, oldPlan);
    }

    @Test
    @DisplayName("newStart == oldStart && newEnd == oldEnd")
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
    @DisplayName("oldStart = newStart && oldEnd < newEnd")
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
    @DisplayName("oldStart = newStart, oldEnd > newEnd")
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
    @DisplayName("oldStart < newStart, oldEnd = newEnd")
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
    @DisplayName("oldStart > newStart, oldEnd = newEnd")
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

    /**
     * oldStart != newStart && oldEnd != newEnd
     */
    @Test
    @DisplayName("oldStart < newStart < newEnd < oldEnd")
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
    @DisplayName("newStart < oldStart < newEnd < oldEnd")
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
    @DisplayName("newStart < oldStart < oldEnd < newEnd")
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
    @DisplayName("oldStart < newStart < oldEnd < newEnd")
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
    @DisplayName("newStart < newEnd < oldStart < oldEnd")
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
    @DisplayName("oldStart < oldEnd < newStart < newEnd")
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
