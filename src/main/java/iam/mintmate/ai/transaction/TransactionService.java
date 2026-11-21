package iam.mintmate.ai.transaction;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import iam.mintmate.ai.dto.TransactionSummaryDTO;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class TransactionService {

    private final TransactionRepository transactionRepository;

    public TransactionService(TransactionRepository transactionRepository) {
        this.transactionRepository = transactionRepository;
    }

    @Transactional(readOnly = true)
    public BigDecimal sumAmountByCategoryNameAndTransactionTypeAndTransactionDateBetween(
            String categoryName,
            TransactionType transactionType,
            LocalDate startDate,
            LocalDate endDate
    ) {
        if (startDate.isAfter(endDate)) {
            throw new IllegalArgumentException(
                    "Start date must not be after end date"
            );
        }
        return transactionRepository.sumAmountByCategoryNameAndTransactionTypeAndTransactionDateBetween(
                categoryName,
                transactionType,
                startDate,
                endDate
        );
    }

    @Transactional(readOnly = true)
    public List<TransactionSummaryDTO> findLargestTransactionsInDateRange(
            LocalDate startDate,
            LocalDate endDate,
            int limit
    ) {
        if (startDate.isAfter(endDate)) {
            throw new IllegalArgumentException(
                    "Start date must not be after end date"
            );
        }
        if (limit <= 0) {
            throw new IllegalArgumentException("Limit must be greater than zero");
        }

        return transactionRepository.findLargestTransactionsInDateRange(
                        startDate,
                        endDate,
                        PageRequest.of(0, limit)
                )
                .stream()
                .map(TransactionSummaryDTO::fromEntity)
                .toList();
    }
}
