package iam.mintmate.ai.seed;

import java.util.Map;

import iam.mintmate.ai.category.CategoryEntity;
import iam.mintmate.ai.category.CategoryRepository;
import iam.mintmate.ai.transaction.TransactionRepository;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
@ConditionalOnProperty(name = "mint-mate.seed-transactions.enabled", havingValue = "true", matchIfMissing = true)
public class TransactionDataInitializer implements ApplicationRunner {

    private final TransactionRepository transactionRepository;
    private final CategoryRepository categoryRepository;
    private final TransactionDataGenerator transactionDataGenerator;

    public TransactionDataInitializer(
            TransactionRepository transactionRepository,
            CategoryRepository categoryRepository,
            TransactionDataGenerator transactionDataGenerator
    ) {
        this.transactionRepository = transactionRepository;
        this.categoryRepository = categoryRepository;
        this.transactionDataGenerator = transactionDataGenerator;
    }

    @Override
    @Transactional
    public void run(ApplicationArguments args) {
        if (transactionRepository.count() > 0) {
            return;
        }

        Map<String, CategoryEntity> categoriesByName = categoryRepository.findAll()
                .stream()
                .collect(java.util.stream.Collectors.toMap(CategoryEntity::getName, category -> category));

        transactionRepository.saveAll(transactionDataGenerator.generate(categoriesByName));
    }
}
