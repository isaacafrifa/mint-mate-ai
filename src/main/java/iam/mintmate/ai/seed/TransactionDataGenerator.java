package iam.mintmate.ai.seed;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.YearMonth;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Random;

import iam.mintmate.ai.category.CategoryEntity;
import iam.mintmate.ai.transaction.TransactionEntity;
import iam.mintmate.ai.transaction.TransactionType;
import net.datafaker.Faker;
import org.springframework.stereotype.Component;

@Component
public class TransactionDataGenerator {

    private static final long SEED = 1_234_567_890L;
    private static final LocalDate START_MONTH = LocalDate.of(2026, 5, 1);
    private static final int MONTH_COUNT = 6;
    public static final String SPOTIFY = "Spotify";
    public static final String GROCERIES = "GROCERIES";

    public List<TransactionEntity> generate(Map<String, CategoryEntity> categories) {
        // Respect the DB constraint in '001-create-initial-tables.sql'
        // chk_transaction_amount: income stays positive and expenses stay negative.
        Faker faker = new Faker(new Random(SEED));
        Random random = new Random(SEED);
        List<TransactionEntity> transactions = new ArrayList<>();

        for (int monthOffset = 0; monthOffset < MONTH_COUNT; monthOffset++) {
            YearMonth month = YearMonth.from(START_MONTH.plusMonths(monthOffset));
            boolean expensiveShoppingMonth = month.equals(YearMonth.of(2026, 10));
            boolean higherRestaurantMonth = month.equals(YearMonth.of(2026, 6));
            boolean highGroceryMonth = month.equals(YearMonth.of(2026, 9));

            addRecurringMonthlyCosts(transactions, month, categories, random, highGroceryMonth);
            addSalary(transactions, month, categories);
            addGroceries(transactions, month, categories, random, faker, highGroceryMonth);
            addDining(transactions, month, categories, random, faker, higherRestaurantMonth);
            addTransport(transactions, month, categories, random);
            addShopping(transactions, month, categories, random, faker, expensiveShoppingMonth);
            addEntertainment(transactions, month, categories, random, faker);
            addFitness(transactions, month, categories);
            addOneOffMedicalExpense(transactions, month, categories);
        }

        transactions.sort(Comparator.comparing(TransactionEntity::getTransactionDate)
                .thenComparing(TransactionEntity::getTransactionType)
                .thenComparing(TransactionEntity::getDescription));
        return transactions;
    }

    private void addRecurringMonthlyCosts(
            List<TransactionEntity> transactions,
            YearMonth month,
            Map<String, CategoryEntity> categories,
            Random random,
            boolean highGroceryMonth
    ) {
        addTransaction(transactions, month.atDay(1), "Monthly rent", "Amsterdam Housing", money(1150), TransactionType.EXPENSE, categories.get("HOUSING"));
        addTransaction(transactions, month.atDay(4), "Electricity and gas", "Vattenfall", money(122.0 + random.nextInt(18)), TransactionType.EXPENSE, categories.get("UTILITIES"));
        addTransaction(transactions, month.atDay(8), "Internet bill", "Ziggo", money(44.50), TransactionType.EXPENSE, categories.get("UTILITIES"));
        addTransaction(transactions, month.atDay(18), "Health insurance", "VGZ", money(111.20), TransactionType.EXPENSE, categories.get("HEALTHCARE"));
        addTransaction(transactions, month.atDay(21), "Spotify Premium", SPOTIFY, money(10.99), TransactionType.EXPENSE, categories.get("ENTERTAINMENT"));
        addTransaction(transactions, month.atDay(24), "Monthly gym membership", "Basic-Fit", money(29.99), TransactionType.EXPENSE, categories.get("FITNESS"));

        if (highGroceryMonth) {
            addTransaction(transactions, month.atDay(12), "Bulk household pantry restock", "Albert Heijn XL", money(128.42), TransactionType.EXPENSE, categories.get(GROCERIES));
        } else {
            addTransaction(transactions, month.atDay(12), "Household restock", "Albert Heijn", money(26.75 + random.nextDouble() * 8), TransactionType.EXPENSE, categories.get(GROCERIES));
        }
    }

    private void addSalary(List<TransactionEntity> transactions, YearMonth month, Map<String, CategoryEntity> categories) {
        addTransaction(transactions, month.atEndOfMonth(), "Monthly net salary", "Employer Payroll", money(3450), TransactionType.INCOME, categories.get("SALARY"));
    }

    private void addGroceries(
            List<TransactionEntity> transactions,
            YearMonth month,
            Map<String, CategoryEntity> categories,
            Random random,
            Faker faker,
            boolean highGroceryMonth
    ) {
        LocalDate weekStart = month.atDay(1);
        LocalDate monthEnd = month.atEndOfMonth();
        while (!weekStart.isAfter(monthEnd)) {
            int groceryCount = 3 + random.nextInt(3);
            if (highGroceryMonth) {
                groceryCount++;
            }

            for (int i = 0; i < groceryCount; i++) {
                LocalDate date = weekStart.plusDays(random.nextInt(7));
                if (date.isAfter(monthEnd)) {
                    date = monthEnd.minusDays(random.nextInt(2));
                }

                String merchant = switch (random.nextInt(4)) {
                    case 0 -> "Albert Heijn";
                    case 1 -> "Jumbo";
                    case 2 -> "Lidl";
                    default -> "Marqt";
                };
                if (random.nextInt(8) == 0) {
                    merchant = faker.company().name();
                }

                double baseAmount = 14.50 + random.nextDouble() * 42.00;
                if (highGroceryMonth) {
                    baseAmount *= 1.35;
                }

                addTransaction(transactions, date, "Weekly groceries", merchant, money(baseAmount), TransactionType.EXPENSE, categories.get(GROCERIES));
            }

            weekStart = weekStart.plusWeeks(1);
        }
    }

    private void addDining(
            List<TransactionEntity> transactions,
            YearMonth month,
            Map<String, CategoryEntity> categories,
            Random random,
            Faker faker,
            boolean higherRestaurantMonth
    ) {
        int diningCount = 2 + random.nextInt(3);
        if (higherRestaurantMonth) {
            diningCount += 2;
        }

        for (int i = 0; i < diningCount; i++) {
            int day = 3 + random.nextInt(Math.max(1, month.lengthOfMonth() - 6));
            LocalDate date = month.atDay(Math.min(day, month.lengthOfMonth()));
            String merchant = switch (random.nextInt(4)) {
                case 0 -> "Cafe de Jaren";
                case 1 -> "Pllek";
                case 2 -> "Bistro Belcampo";
                default -> "Bakers and Roasters";
            };
            if (random.nextInt(3) == 0) {
                merchant = faker.company().name();
            }

            double amount = 16.50 + random.nextDouble() * 42.00;
            if (higherRestaurantMonth) {
                amount += 18 + random.nextDouble() * 30;
            }

            addTransaction(transactions, date, "Restaurant dinner", merchant, money(amount), TransactionType.EXPENSE, categories.get("DINING"));
        }
    }

    private void addTransport(List<TransactionEntity> transactions, YearMonth month, Map<String, CategoryEntity> categories, Random random) {
        int transportCount = 3 + random.nextInt(2);
        for (int i = 0; i < transportCount; i++) {
            LocalDate date = month.atDay(2 + random.nextInt(Math.max(1, month.lengthOfMonth() - 3)));
            String description;
            String merchant;
            double amount;
            if (i % 3 == 0) {
                description = "NS commute";
                merchant = "NS";
                amount = 8.20 + random.nextDouble() * 4.25;
            } else {
                description = "GVB tram and metro";
                merchant = "GVB";
                amount = 2.80 + random.nextDouble() * 1.80;
            }

            addTransaction(transactions, date, description, merchant, money(amount), TransactionType.EXPENSE, categories.get("TRANSPORT"));
        }
    }

    private void addShopping(
            List<TransactionEntity> transactions,
            YearMonth month,
            Map<String, CategoryEntity> categories,
            Random random,
            Faker faker,
            boolean expensiveShoppingMonth
    ) {
        int shoppingCount = 1 + random.nextInt(3);
        if (expensiveShoppingMonth) {
            shoppingCount += 2;
        }

        for (int i = 0; i < shoppingCount; i++) {
            LocalDate date = month.atDay(10 + random.nextInt(Math.max(1, month.lengthOfMonth() - 12)));
            String merchant = switch (random.nextInt(4)) {
                case 0 -> "Zalando";
                case 1 -> "HEMA";
                case 2 -> "UNIQLO";
                default -> "Decathlon";
            };
            if (random.nextInt(2) == 0) {
                merchant = faker.company().name();
            }

            double amount = 24.00 + random.nextDouble() * 75.00;
            if (expensiveShoppingMonth) {
                amount *= 1.8;
            }

            addTransaction(transactions, date, "Retail purchase", merchant, money(amount), TransactionType.EXPENSE, categories.get("SHOPPING"));
        }

        if (expensiveShoppingMonth) {
            addTransaction(transactions, month.atDay(Math.min(25, month.lengthOfMonth())), "Expensive seasonal shopping haul", "Zalando", money(489.90), TransactionType.EXPENSE, categories.get("SHOPPING"));
        }
    }

    private void addEntertainment(List<TransactionEntity> transactions, YearMonth month, Map<String, CategoryEntity> categories, Random random, Faker faker) {
        int entertainmentCount = 1 + random.nextInt(3);
        for (int i = 0; i < entertainmentCount; i++) {
            LocalDate date = month.atDay(6 + random.nextInt(Math.max(1, month.lengthOfMonth() - 8)));
            String merchant = switch (random.nextInt(3)) {
                case 0 -> "Pathe";
                case 1 -> SPOTIFY;
                default -> "Netflix";
            };
            if (random.nextInt(4) == 0) {
                merchant = faker.company().name();
            }

            double amount = merchant.equals(SPOTIFY) ? 10.99 : 13.50 + random.nextDouble() * 18.00;
            addTransaction(transactions, date, "Leisure spend", merchant, money(amount), TransactionType.EXPENSE, categories.get("ENTERTAINMENT"));
        }
    }

    private void addFitness(List<TransactionEntity> transactions, YearMonth month, Map<String, CategoryEntity> categories) {
        addTransaction(transactions, month.atDay(24), "Gym membership", "Basic-Fit", money(29.99), TransactionType.EXPENSE, categories.get("FITNESS"));
    }

    private void addOneOffMedicalExpense(List<TransactionEntity> transactions, YearMonth month, Map<String, CategoryEntity> categories) {
        if (month.equals(YearMonth.of(2026, 7))) {
            addTransaction(transactions, month.atDay(17), "Unexpected medical expense", "Amsterdam Medical Center", money(147.80), TransactionType.EXPENSE, categories.get("HEALTHCARE"));
        }
    }

    private void addTransaction(
            List<TransactionEntity> transactions,
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
        transaction.setAmount(transactionType == TransactionType.EXPENSE ? amount.negate() : amount.abs());
        transaction.setTransactionType(transactionType);
        transaction.setCategory(category);
        transactions.add(transaction);
    }

    private BigDecimal money(double amount) {
        return BigDecimal.valueOf(amount).setScale(2, java.math.RoundingMode.HALF_UP);
    }
}
