package iam.mintmate.ai;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;

import java.math.BigDecimal;
import java.util.List;
import java.time.LocalDate;

import iam.mintmate.ai.category.CategoryEntity;
import iam.mintmate.ai.category.CategoryRepository;
import iam.mintmate.ai.transaction.TransactionEntity;
import iam.mintmate.ai.transaction.TransactionRepository;
import iam.mintmate.ai.dto.TransactionSummaryDTO;
import iam.mintmate.ai.transaction.TransactionType;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.boot.jdbc.test.autoconfigure.AutoConfigureTestDatabase;
import org.springframework.context.annotation.Import;


@DataJpaTest
@Import(TestcontainersConfiguration.class)
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@DisplayName("Running TransactionRepository tests")
class TransactionRepositoryTest {

    @Autowired
    private TransactionRepository transactionRepository;

    @Autowired
    private CategoryRepository categoryRepository;

    @Test
    void sumsCategoryTransactionsAcrossDateRange() {
        CategoryEntity groceries = categoryRepository.findByName("GROCERIES")
                .orElseThrow(() -> new IllegalStateException("Seeded GROCERIES category not found"));

        transactionRepository.save(createTransaction(
                LocalDate.of(2026, 10, 5),
                "Weekly groceries",
                "Albert Heijn",
                new BigDecimal("-42.50"),
                TransactionType.EXPENSE,
                groceries
        ));
        transactionRepository.save(createTransaction(
                LocalDate.of(2026, 10, 17),
                "Bulk groceries",
                "Jumbo",
                new BigDecimal("-18.25"),
                TransactionType.EXPENSE,
                groceries
        ));
        transactionRepository.save(createTransaction(
                LocalDate.of(2026, 10, 20),
                "Bus ticket",
                "GVB",
                new BigDecimal("-9.50"),
                TransactionType.EXPENSE,
                createOtherCategory()
        ));
        transactionRepository.save(createTransaction(
                LocalDate.of(2026, 9, 30),
                "Late grocery purchase",
                "Albert Heijn",
                new BigDecimal("-12.00"),
                TransactionType.EXPENSE,
                groceries
        ));

        BigDecimal total = transactionRepository.sumAmountByCategoryNameAndTransactionTypeAndTransactionDateBetween(
                "GROCERIES",
                TransactionType.EXPENSE,
                LocalDate.of(2026, 10, 1),
                LocalDate.of(2026, 10, 31)
        );

        assertEquals(new BigDecimal("60.75"), total);
    }

    // Income related enquiry
    @Test
    void sumsIncomeCategoryTransactionsAcrossDateRange() {
        CategoryEntity salary = categoryRepository.findByName("SALARY")
                .orElseThrow(() -> new IllegalStateException("Seeded SALARY category not found"));

        transactionRepository.save(createTransaction(
                LocalDate.of(2026, 10, 31),
                "Monthly net salary",
                "Employer Payroll",
                new BigDecimal("3450.00"),
                TransactionType.INCOME,
                salary
        ));
        transactionRepository.save(createTransaction(
                LocalDate.of(2026, 10, 15),
                "Freelance project",
                "Client Transfer",
                new BigDecimal("450.00"),
                TransactionType.INCOME,
                salary
        ));
        transactionRepository.save(createTransaction(
                LocalDate.of(2026, 10, 20),
                "Dinner out",
                "Restaurant",
                new BigDecimal("-35.00"),
                TransactionType.EXPENSE,
                createOtherCategory()
        ));
        transactionRepository.save(createTransaction(
                LocalDate.of(2026, 9, 30),
                "Previous month salary",
                "Employer Payroll",
                new BigDecimal("3450.00"),
                TransactionType.INCOME,
                salary
        ));

        BigDecimal total = transactionRepository.sumAmountByCategoryNameAndTransactionTypeAndTransactionDateBetween(
                "SALARY",
                TransactionType.INCOME,
                LocalDate.of(2026, 10, 1),
                LocalDate.of(2026, 10, 31)
        );

        assertEquals(new BigDecimal("3900.00"), total);
    }

    @Test
    void findsLargestTransactionsInDateRange() {
        CategoryEntity groceries = categoryRepository.findByName("GROCERIES")
                .orElseThrow(() -> new IllegalStateException("Seeded GROCERIES category not found"));
        CategoryEntity transport = categoryRepository.findByName("TRANSPORT")
                .orElseThrow(() -> new IllegalStateException("Seeded TRANSPORT category not found"));

        transactionRepository.save(createTransaction(
                LocalDate.of(2026, 10, 4),
                "Apartment rent",
                "Housing Ltd",
                new BigDecimal("-1200.00"),
                TransactionType.EXPENSE,
                categoryRepository.findByName("HOUSING")
                        .orElseThrow(() -> new IllegalStateException("Seeded HOUSING category not found"))
        ));
        transactionRepository.save(createTransaction(
                LocalDate.of(2026, 10, 12),
                "Monthly salary",
                "Employer Payroll",
                new BigDecimal("3450.00"),
                TransactionType.INCOME,
                categoryRepository.findByName("SALARY")
                        .orElseThrow(() -> new IllegalStateException("Seeded SALARY category not found"))
        ));
        transactionRepository.save(createTransaction(
                LocalDate.of(2026, 10, 18),
                "Shopping spree",
                "Zalando",
                new BigDecimal("-489.90"),
                TransactionType.EXPENSE,
                categoryRepository.findByName("SHOPPING")
                        .orElseThrow(() -> new IllegalStateException("Seeded SHOPPING category not found"))
        ));
        transactionRepository.save(createTransaction(
                LocalDate.of(2026, 10, 20),
                "Weekly groceries",
                "Albert Heijn",
                new BigDecimal("-42.50"),
                TransactionType.EXPENSE,
                groceries
        ));
        transactionRepository.save(createTransaction(
                LocalDate.of(2026, 10, 22),
                "Metro card top-up",
                "GVB",
                new BigDecimal("-35.00"),
                TransactionType.EXPENSE,
                transport
        ));

        List<TransactionSummaryDTO> largestTransactions = transactionRepository.findLargestTransactionsInDateRange(
                LocalDate.of(2026, 10, 1),
                LocalDate.of(2026, 10, 31),
                org.springframework.data.domain.PageRequest.of(0, 3)
        ).stream().map(TransactionSummaryDTO::fromEntity).toList();

        assertEquals(3, largestTransactions.size());
        assertEquals(new BigDecimal("3450.00"), largestTransactions.get(0).amount());
        assertEquals(new BigDecimal("-1200.00"), largestTransactions.get(1).amount());
        assertEquals(new BigDecimal("-489.90"), largestTransactions.get(2).amount());
        assertFalse(largestTransactions.isEmpty());
    }

    private TransactionEntity createTransaction(
            LocalDate transactionDate,
            String description,
            String merchant,
            BigDecimal amount,
            TransactionType transactionType,
            CategoryEntity category
    ) {
        TransactionEntity transaction = new TransactionEntity();
        transaction.setTransactionDate(transactionDate);
        transaction.setDescription(description);
        transaction.setMerchant(merchant);
        transaction.setAmount(amount);
        transaction.setTransactionType(transactionType);
        transaction.setCategory(category);
        return transaction;
    }

    private CategoryEntity createOtherCategory() {
        return categoryRepository.findByName("TRANSPORT")
                .orElseThrow(() -> new IllegalStateException("Seeded TRANSPORT category not found"));
    }
}
