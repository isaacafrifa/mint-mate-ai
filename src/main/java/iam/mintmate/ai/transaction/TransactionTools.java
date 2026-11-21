package iam.mintmate.ai.transaction;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Locale;
import iam.mintmate.ai.common.DateRange;
import iam.mintmate.ai.common.DateRangeResolver;
import iam.mintmate.ai.dto.TransactionSummaryDTO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.ai.tool.annotation.ToolParam;
import org.springframework.stereotype.Component;

/*This *Tools class exposes the transaction service methods as tools that the AI assistant can invoke.
 Register this bean with the ChatClient (or attach it to individual prompts) to make these tools available to the LLM.
 */
@Component
@Slf4j
@RequiredArgsConstructor
public class TransactionTools {

    private final TransactionService transactionService;
    private final DateRangeResolver dateRangeResolver;


    @Tool(
            name = "sumAmountByCategoryNameAndTransactionTypeAndTransactionDateBetween",
            description = """
        Returns the total transaction amount for a given category
        and transaction type (INCOME or EXPENSE) within an inclusive
        date range. Use EXPENSE for spending questions and INCOME
        for earnings or income questions.
        Convert month and year references into their first and last dates.
        For example, October 2026 means 2026-10-01 through 2026-10-31.
        
        Valid categories:
        SALARY, GROCERIES, HOUSING, UTILITIES, TRANSPORT, DINING, ENTERTAINMENT, SHOPPING, HEALTHCARE, FITNESS, OTHER.
        """
    )
    public BigDecimal sumAmountByCategoryNameAndTransactionTypeAndTransactionDateBetween(
            @ToolParam(description = "Category name, for example GROCERIES or TRANSPORT.") String categoryName,
            @ToolParam(description = "Transaction type to include in the total.") TransactionType transactionType,
            @ToolParam(description = "Start date, inclusive.") LocalDate startDate,
            @ToolParam(description = "End date, inclusive.") LocalDate endDate
    ) {
        log.info(
                "Executing transaction aggregation: category={}, type={}, startDate={}, endDate={}",
                categoryName,
                transactionType,
                startDate,
                endDate
        );

        return transactionService.sumAmountByCategoryNameAndTransactionTypeAndTransactionDateBetween(
                categoryName.toUpperCase(Locale.ROOT),
                transactionType,
                startDate,
                endDate
        );
    }

    @Tool(
            name = "findLargestTransactions",
            description = """
            Returns the largest transactions by absolute amount.

            Use this tool when the user asks about their biggest,
            largest or most expensive transactions.

            Supported periods:
            - Last month: previous complete calendar month.
            - This month: from the first day of the current month until today.
            - Last week: previous complete Monday-to-Sunday calendar week.

            If the user does not specify a number, return 5 transactions.
            The results include both income and expenses.
            """
    )
    public List<TransactionSummaryDTO> findLargestTransactions(
            @ToolParam(description = "Relative period such as LAST_MONTH, THIS_MONTH or LAST_WEEK. Leave blank when using explicit dates.")
            String period,
            @ToolParam(description = "Explicit start date in ISO-8601 format, for example 2026-10-01. Must be provided together with endDate. Leave blank when using a relative period.")
            String startDate,
            @ToolParam(description = "Explicit end date in ISO-8601 format, for example 2026-10-31. Must be provided together with startDate. Leave blank when using a relative period.")
            String endDate,
            @ToolParam(description = "Maximum number of transactions to return. Use 5 if not specified.")
            int limit
    ) {
        DateRange dateRange = dateRangeResolver.resolve(period, startDate, endDate);

        log.info(
                "Executing largest-transactions lookup: period={}, startDate={}, endDate={}, limit={}",
                period,
                dateRange.startDate(),
                dateRange.endDate(),
                limit
        );

        return transactionService.findLargestTransactionsInDateRange(
                dateRange.startDate(),
                dateRange.endDate(),
                limit
        );
    }
}
