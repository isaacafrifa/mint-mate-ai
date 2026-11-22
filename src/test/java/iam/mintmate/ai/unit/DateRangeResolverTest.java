package iam.mintmate.ai.unit;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.time.Clock;
import java.time.Instant;
import java.time.ZoneId;
import java.time.LocalDate;

import iam.mintmate.ai.common.DateRange;
import iam.mintmate.ai.common.DateRangeResolver;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class DateRangeResolverTest {

    private DateRangeResolver dateRangeResolver;

    @BeforeEach
    void setUp() {
        Clock fixedClock = Clock.fixed(
                Instant.parse("2026-11-22T12:00:00Z"),
                ZoneId.of("Europe/Amsterdam")
        );
        dateRangeResolver = new DateRangeResolver(fixedClock);
    }

    @Test
    void resolvesExplicitDateRange() {
        DateRange dateRange = dateRangeResolver.resolve(
                null,
                "2026-10-01",
                "2026-10-31"
        );

        assertEquals(LocalDate.of(2026, 10, 1), dateRange.startDate());
        assertEquals(LocalDate.of(2026, 10, 31), dateRange.endDate());
    }

    @Test
    void rejectsPartialExplicitDateRange() {
        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> dateRangeResolver.resolve("LAST_MONTH", "2026-10-01", null)
        );

        assertEquals("Provide both startDate and endDate together", exception.getMessage());
    }

    @Test
    void rejectsMixedPeriodAndExplicitDates() {
        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> dateRangeResolver.resolve("LAST_MONTH", "2026-10-01", "2026-10-31")
        );

        assertEquals("Provide either a relative period or explicit dates, not both", exception.getMessage());
    }

    @Test
    void resolvesLastMonth() {
        DateRange dateRange = dateRangeResolver.resolve("LAST_MONTH", null, null);

        assertEquals(LocalDate.of(2026, 10, 1), dateRange.startDate());
        assertEquals(LocalDate.of(2026, 10, 31), dateRange.endDate());
    }

    @Test
    void resolvesThisMonth() {
        DateRange dateRange = dateRangeResolver.resolve("THIS_MONTH", null, null);

        assertEquals(LocalDate.of(2026, 11, 1), dateRange.startDate());
        assertEquals(LocalDate.of(2026, 11, 22), dateRange.endDate());
    }

    @Test
    void resolvesLastWeek() {
        DateRange dateRange = dateRangeResolver.resolve("LAST_WEEK", null, null);

        assertEquals(LocalDate.of(2026, 11, 9), dateRange.startDate());
        assertEquals(LocalDate.of(2026, 11, 15), dateRange.endDate());
    }

    @Test
    void rejectsUnsupportedPeriod() {
        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> dateRangeResolver.resolve("LAST_10_DAYS", null, null)
        );

        assertEquals("Unsupported period: LAST_10_DAYS", exception.getMessage());
    }
}
