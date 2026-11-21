package iam.mintmate.ai.transaction;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.data.domain.Pageable;

public interface TransactionRepository extends JpaRepository<TransactionEntity, Long> {

    @Query("""
            select coalesce(sum(abs(t.amount)), 0)
            from TransactionEntity t
            where t.category.name = :categoryName
              and t.transactionType = :transactionType
              and t.transactionDate between :startDate and :endDate
            """)
    BigDecimal sumAmountByCategoryNameAndTransactionTypeAndTransactionDateBetween(
            @Param("categoryName") String categoryName,
            @Param("transactionType") TransactionType transactionType,
            @Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate
    );

    @Query("""
            select t
            from TransactionEntity t
            where t.transactionDate between :startDate and :endDate
            order by abs(t.amount) desc, t.transactionDate desc, t.id asc
            """)
    List<TransactionEntity> findLargestTransactionsInDateRange(
            @Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate,
            Pageable pageable
    );
}
