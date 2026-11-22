package iam.mintmate.ai.unit;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import iam.mintmate.ai.common.DateRange;
import iam.mintmate.ai.common.DateRangeResolver;
import iam.mintmate.ai.dto.TransactionSummaryDTO;
import iam.mintmate.ai.transaction.TransactionService;
import iam.mintmate.ai.transaction.TransactionTools;
import iam.mintmate.ai.transaction.TransactionType;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
@DisplayName("Running unit tests of TransactionTools")
class TransactionToolsTest {

    @Mock
    private TransactionService transactionService;

    @Mock
    private DateRangeResolver dateRangeResolver;

    @InjectMocks
    private TransactionTools transactionTools;

    @Test
    void sumAmountToolUppercasesCategoryAndDelegatesToService() {
        when(transactionService.sumAmountByCategoryNameAndTransactionTypeAndTransactionDateBetween(
                "GROCERIES",
                TransactionType.EXPENSE,
                LocalDate.of(2026, 10, 1),
                LocalDate.of(2026, 10, 31)
        )).thenReturn(new BigDecimal("60.75"));

        BigDecimal result = transactionTools.sumAmountByCategoryNameAndTransactionTypeAndTransactionDateBetween(
                "groceries",
                TransactionType.EXPENSE,
                LocalDate.of(2026, 10, 1),
                LocalDate.of(2026, 10, 31)
        );

        assertEquals(new BigDecimal("60.75"), result);
        verify(transactionService).sumAmountByCategoryNameAndTransactionTypeAndTransactionDateBetween(
                "GROCERIES",
                TransactionType.EXPENSE,
                LocalDate.of(2026, 10, 1),
                LocalDate.of(2026, 10, 31)
        );
    }

    @Test
    void findLargestTransactionsDefaultsLimitToFiveWhenMissing() {
        when(dateRangeResolver.resolve("LAST_MONTH", null, null))
                .thenReturn(new DateRange(
                        LocalDate.of(2026, 10, 1),
                        LocalDate.of(2026, 10, 31)
                ));
        when(transactionService.findLargestTransactionsInDateRange(
                LocalDate.of(2026, 10, 1),
                LocalDate.of(2026, 10, 31),
                5
        )).thenReturn(List.of(sampleTransaction()));

        List<TransactionSummaryDTO> result = transactionTools.findLargestTransactions(
                "LAST_MONTH",
                null,
                null,
                null
        );

        assertEquals(1, result.size());
        verify(dateRangeResolver).resolve("LAST_MONTH", null, null);
        verify(transactionService).findLargestTransactionsInDateRange(
                LocalDate.of(2026, 10, 1),
                LocalDate.of(2026, 10, 31),
                5
        );
    }

    @Test
    void findLargestTransactionsUsesProvidedLimitAndExplicitDates() {
        when(dateRangeResolver.resolve(null, "2026-10-01", "2026-10-31"))
                .thenReturn(new DateRange(
                        LocalDate.of(2026, 10, 1),
                        LocalDate.of(2026, 10, 31)
                ));
        when(transactionService.findLargestTransactionsInDateRange(
                LocalDate.of(2026, 10, 1),
                LocalDate.of(2026, 10, 31),
                3
        )).thenReturn(List.of(sampleTransaction()));

        List<TransactionSummaryDTO> result = transactionTools.findLargestTransactions(
                null,
                "2026-10-01",
                "2026-10-31",
                3
        );

        assertEquals(1, result.size());
        verify(dateRangeResolver).resolve(null, "2026-10-01", "2026-10-31");
        verify(transactionService).findLargestTransactionsInDateRange(
                LocalDate.of(2026, 10, 1),
                LocalDate.of(2026, 10, 31),
                3
        );
    }

    @Test
    void findLargestTransactionsDefaultsLimitToFiveWhenNullLimitIsProvidedWithExplicitDates() {
        when(dateRangeResolver.resolve(null, "2026-10-01", "2026-10-31"))
                .thenReturn(new DateRange(
                        LocalDate.of(2026, 10, 1),
                        LocalDate.of(2026, 10, 31)
                ));
        when(transactionService.findLargestTransactionsInDateRange(
                LocalDate.of(2026, 10, 1),
                LocalDate.of(2026, 10, 31),
                5
        )).thenReturn(List.of(sampleTransaction()));

        List<TransactionSummaryDTO> result = transactionTools.findLargestTransactions(
                null,
                "2026-10-01",
                "2026-10-31",
                null
        );

        assertEquals(1, result.size());
        verify(dateRangeResolver).resolve(null, "2026-10-01", "2026-10-31");
        verify(transactionService).findLargestTransactionsInDateRange(
                LocalDate.of(2026, 10, 1),
                LocalDate.of(2026, 10, 31),
                5
        );
    }

    private TransactionSummaryDTO sampleTransaction() {
        return new TransactionSummaryDTO(
                1L,
                LocalDate.of(2026, 10, 1),
                "Sample transaction",
                "Sample merchant",
                new BigDecimal("-42.50"),
                TransactionType.EXPENSE,
                "GROCERIES"
        );
    }
}
