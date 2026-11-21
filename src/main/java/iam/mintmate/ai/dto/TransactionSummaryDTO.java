package iam.mintmate.ai.dto;

import iam.mintmate.ai.transaction.TransactionEntity;
import iam.mintmate.ai.transaction.TransactionType;

import java.math.BigDecimal;
import java.time.LocalDate;

public record TransactionSummaryDTO(
        Long id,
        LocalDate transactionDate,
        String description,
        String merchant,
        BigDecimal amount,
        TransactionType transactionType,
        String categoryName
) {
    public static TransactionSummaryDTO fromEntity(TransactionEntity transaction) {
        return new TransactionSummaryDTO(
                transaction.getId(),
                transaction.getTransactionDate(),
                transaction.getDescription(),
                transaction.getMerchant(),
                transaction.getAmount(),
                transaction.getTransactionType(),
                transaction.getCategory().getName()
        );
    }
}
