package iam.mintmate.ai.common;

import java.time.LocalDate;

public record DateRange(
        LocalDate startDate,
        LocalDate endDate
) {
    public DateRange {
        if (startDate.isAfter(endDate)) {
            throw new IllegalArgumentException(
                    "Start date must not be after end date"
            );
        }
    }
}