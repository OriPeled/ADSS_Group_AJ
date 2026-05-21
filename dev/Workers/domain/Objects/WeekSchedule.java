package dev.Workers.domain.Objects;

import dev.Workers.domain.Enums.WeekStatus;
import java.time.LocalDate;
import java.time.temporal.TemporalAdjusters;
import java.time.DayOfWeek;
import java.util.List;

public class WeekSchedule {
    private final LocalDate startOfWeek;    // always a Sunday
    private boolean published = false;

    public WeekSchedule(LocalDate dateWithinWeek) {
        // normalize any date to the Sunday of that week
        this.startOfWeek = dateWithinWeek.with(TemporalAdjusters.previousOrSame(DayOfWeek.SUNDAY));
    }

    public LocalDate getStartOfWeek() {
        return startOfWeek;
    }

    public boolean isPublished() {
        return published;
    }

    public void setPublished(boolean published) {
        this.published = published;
    }

    public WeekStatus calculateStatus(boolean isFullyAssigned) {
        if (!isFullyAssigned) return WeekStatus.INCOMPLETE;
        if (published) return WeekStatus.PUBLISHED;
        return WeekStatus.READY_TO_PUBLISH;
    }

    public boolean isViewableByUser() {
        LocalDate today = LocalDate.now();
        LocalDate thisSunday = today.with(TemporalAdjusters.previousOrSame(DayOfWeek.SUNDAY));

        if (startOfWeek.isBefore(thisSunday) || startOfWeek.equals(thisSunday)) {
            return true;
        }
        return published;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        WeekSchedule that = (WeekSchedule) o;
        return startOfWeek.equals(that.startOfWeek);
    }

    @Override
    public int hashCode() {
        return startOfWeek.hashCode();
    }
}