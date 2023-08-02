package yeonleaf.plantodo.util;

import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Spliterator;
import java.util.function.Consumer;
import java.util.stream.Stream;

@Setter
public class DateRange implements Iterable<LocalDate> {

    private LocalDate startDate;
    private LocalDate endDate;

    @Override
    public void forEach(Consumer<? super LocalDate> action) {
        Iterable.super.forEach(action);
    }

    @Override
    public Spliterator<LocalDate> spliterator() {
        return Iterable.super.spliterator();
    }

    @Override
    public Iterator<LocalDate> iterator() {
        return stream().iterator();
    }

    public Stream<LocalDate> stream() {
        return Stream.iterate(startDate, d -> d.plusDays(1))
                .limit(ChronoUnit.DAYS.between(startDate, endDate) + 1);
    }

    public List<LocalDate> between(LocalDate startDate, LocalDate endDate) {
        setStartDate(startDate);
        setEndDate(endDate);
        return stream().toList();
    }

}
