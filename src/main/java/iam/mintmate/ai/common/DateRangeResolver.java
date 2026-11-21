package iam.mintmate.ai.common;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.time.Clock;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.temporal.TemporalAdjusters;
import java.util.Locale;

/**
 * This component is responsible for resolving common date periods.
 */
@Component
@RequiredArgsConstructor
public class DateRangeResolver {

    private final Clock clock;

    // fromDates supports explicit date ranges if provided
    public DateRange fromDates(LocalDate startDate, LocalDate endDate) {
        return new DateRange(startDate, endDate);
    }

    public DateRange lastMonth() {
        LocalDate today = LocalDate.now(clock);

        LocalDate startDate = today.minusMonths(1)
                .withDayOfMonth(1);

        LocalDate endDate = today.withDayOfMonth(1)
                .minusDays(1);

        return new DateRange(startDate, endDate);
    }

    public DateRange thisMonth() {
        LocalDate today = LocalDate.now(clock);

        return new DateRange(
                today.withDayOfMonth(1),
                today
        );
    }

    public DateRange lastWeek() {
        LocalDate today = LocalDate.now(clock);

        LocalDate startDate = today.minusWeeks(1)
                .with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY));

        LocalDate endDate = startDate.plusDays(6);

        return new DateRange(startDate, endDate);
    }


    public DateRange resolve(
            String period,
            String startDate,
            String endDate
    ) {
        boolean hasStartDate = hasText(startDate);
        boolean hasEndDate = hasText(endDate);
        boolean hasPeriod = hasText(period);

        if (hasStartDate != hasEndDate) {
            throw new IllegalArgumentException(
                    "Provide both startDate and endDate together"
            );
        }

        if (hasStartDate && hasPeriod) {
            throw new IllegalArgumentException(
                    "Provide either a relative period or explicit dates, not both"
            );
        }

        if (hasStartDate) {
            return fromDates(
                    LocalDate.parse(startDate.trim()),
                    LocalDate.parse(endDate.trim())
            );
        }

        if (hasText(period)) {
            return switch (period.trim().toUpperCase(Locale.ROOT)) {
                case "LAST_MONTH" -> lastMonth();
                case "THIS_MONTH" -> thisMonth();
                case "LAST_WEEK" -> lastWeek();
                default -> throw new IllegalArgumentException(
                        "Unsupported period: " + period
                );
            };
        }

        throw new IllegalArgumentException(
                "Provide either a relative period or explicit dates"
        );
    }

    private boolean hasText(String value) {
        return value != null && !value.isBlank();
    }
}
