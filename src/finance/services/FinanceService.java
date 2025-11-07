package finance.services;

import finance.models.*;
import finance.utils.ValidationResult;

import java.util.*;
import java.util.stream.Collectors;

public class FinanceService {
    private final Map<String, Category> categories;

    public FinanceService() {
        this.categories = new HashMap<>();
    }

    public ValidationResult addIncome(Wallet wallet, String categoryName, double amount, String description) {
        ValidationResult validation = validateTransaction(categoryName, amount);
        if (validation.isInvalid()) {
            return validation;
        }

        Category category = getOrCreateCategory(categoryName, Category.TransactionType.INCOME);
        Transaction transaction = new Transaction(category, amount, description);
        wallet.addTransaction(transaction);

        return ValidationResult.valid();
    }

    public ValidationResult addExpense(Wallet wallet, String categoryName, double amount, String description) {
        ValidationResult validation = validateTransaction(categoryName, amount);
        if (validation.isInvalid()) {
            return validation;
        }

        Category category = getOrCreateCategory(categoryName, Category.TransactionType.EXPENSE);
        Transaction transaction = new Transaction(category, amount, description);
        wallet.addTransaction(transaction);

        return ValidationResult.valid();
    }

    public ValidationResult setBudget(Wallet wallet, String categoryName, double limit) {
        if (categoryName == null || categoryName.trim().isEmpty()) {
            return ValidationResult.invalid("Название категории не может быть пустым");
        }

        if (limit <= 0) {
            return ValidationResult.invalid("Лимит бюджета должен быть положительным");
        }

        Category category = getOrCreateCategory(categoryName, Category.TransactionType.EXPENSE);
        wallet.setBudget(category, limit);

        return ValidationResult.valid();
    }

    public double getTotalIncome(Wallet wallet) {
        return wallet.getTotalIncome();
    }

    public double getTotalExpense(Wallet wallet) {
        return wallet.getTotalExpense();
    }

    public Map<String, Double> getIncomeByCategory(Wallet wallet) {
        return wallet.getIncomeByCategory();
    }

    public Map<String, Double> getExpenseByCategory(Wallet wallet) {
        return wallet.getExpenseByCategory();
    }

    public double getTotalByCategories(Wallet wallet, List<String> categoryNames) {
        if (categoryNames == null || categoryNames.isEmpty()) {
            return 0.0;
        }

        List<Category> foundCategories = new ArrayList<>();
        List<String> notFoundCategories = new ArrayList<>();

        for (String categoryName : categoryNames) {
            String baseName = categoryName.trim().toLowerCase();
            // Ищем категорию как для INCOME, так и для EXPENSE
            String incomeKey = baseName + "_" + Category.TransactionType.INCOME.name();
            String expenseKey = baseName + "_" + Category.TransactionType.EXPENSE.name();

            Category incomeCategory = categories.get(incomeKey);
            Category expenseCategory = categories.get(expenseKey);

            boolean found = false;
            if (incomeCategory != null) {
                foundCategories.add(incomeCategory);
                found = true;
            }
            if (expenseCategory != null) {
                foundCategories.add(expenseCategory);
                found = true;
            }

            if (!found) {
                notFoundCategories.add(categoryName);
            }
        }

        if (!notFoundCategories.isEmpty()) {
            System.out.println("ПРЕДУПРЕЖДЕНИЕ: Следующие категории не найдены: " +
                    String.join(", ", notFoundCategories));
        }

        return wallet.getTotalByCategories(foundCategories);
    }

    public List<Budget> getAllBudgets(Wallet wallet) {
        return new ArrayList<>(wallet.getAllBudgets().values());
    }

    public Budget getBudget(Wallet wallet, String categoryName) {
        if (categoryName == null || categoryName.trim().isEmpty()) {
            return null;
        }

        // Бюджеты устанавливаются только на расходы
        String key = categoryName.trim().toLowerCase() + "_" + Category.TransactionType.EXPENSE.name();
        Category category = categories.get(key);

        if (category == null) {
            return null;
        }

        return wallet.getBudget(category);
    }

    public List<Transaction> getRecentTransactions(Wallet wallet, int limit) {
        List<Transaction> transactions = wallet.getTransactions();
        int size = transactions.size();
        int fromIndex = Math.max(0, size - limit);
        return transactions.subList(fromIndex, size);
    }

    public boolean transferBetweenWallets(Wallet fromWallet, Wallet toWallet, double amount, String description) {
        if (amount <= 0) {
            return false;
        }

        if (fromWallet.getBalance() < amount) {
            return false;
        }

        Category expenseCategory = getOrCreateCategory("Перевод", Category.TransactionType.EXPENSE);
        Category incomeCategory = getOrCreateCategory("Перевод", Category.TransactionType.INCOME);

        String expenseDescription = "Перевод пользователю " + toWallet.getOwnerLogin() +
                (description != null && !description.isEmpty() ? " - " + description : "");
        String incomeDescription = "Перевод от пользователя " + fromWallet.getOwnerLogin() +
                (description != null && !description.isEmpty() ? " - " + description : "");

        Transaction expenseTransaction = new Transaction(expenseCategory, amount, expenseDescription);
        Transaction incomeTransaction = new Transaction(incomeCategory, amount, incomeDescription);

        fromWallet.addTransaction(expenseTransaction);
        toWallet.addTransaction(incomeTransaction);

        return true;
    }

    private ValidationResult validateTransaction(String categoryName, double amount) {
        if (categoryName == null || categoryName.trim().isEmpty()) {
            return ValidationResult.invalid("Название категории не может быть пустым");
        }

        if (amount <= 0) {
            return ValidationResult.invalid("Сумма должна быть положительной");
        }

        return ValidationResult.valid();
    }

    private Category getOrCreateCategory(String name, Category.TransactionType type) {
        // Ключ включает и имя, и тип, чтобы различать категории с одинаковым именем но разным типом
        String key = name.trim().toLowerCase() + "_" + type.name();

        if (!categories.containsKey(key)) {
            Category category = new Category(name.trim(), type);
            categories.put(key, category);
            return category;
        }

        return categories.get(key);
    }

    public List<Category> getAllCategories() {
        return new ArrayList<>(categories.values());
    }

    public Map<String, Category> getCategoriesMap() {
        return categories;
    }

    public ValidationResult deleteCategory(Wallet wallet, String categoryName, Category.TransactionType type) {
        if (categoryName == null || categoryName.trim().isEmpty()) {
            return ValidationResult.invalid("Название категории не может быть пустым");
        }

        String key = categoryName.trim().toLowerCase() + "_" + type.name();
        Category category = categories.get(key);

        if (category == null) {
            return ValidationResult.invalid("Категория не найдена");
        }

        // Проверяем, есть ли транзакции с этой категорией
        boolean hasTransactions = wallet.getTransactions().stream()
                .anyMatch(t -> t.getCategory().equals(category));

        if (hasTransactions) {
            return ValidationResult.invalid("Невозможно удалить категорию с существующими транзакциями");
        }

        // Проверяем, есть ли бюджет для этой категории
        if (type == Category.TransactionType.EXPENSE && wallet.getBudget(category) != null) {
            wallet.removeBudget(category);
        }

        categories.remove(key);
        return ValidationResult.valid();
    }

    public ValidationResult renameCategory(Wallet wallet, String oldName, String newName, Category.TransactionType type) {
        if (oldName == null || oldName.trim().isEmpty()) {
            return ValidationResult.invalid("Старое название категории не может быть пустым");
        }

        if (newName == null || newName.trim().isEmpty()) {
            return ValidationResult.invalid("Новое название категории не может быть пустым");
        }

        String oldKey = oldName.trim().toLowerCase() + "_" + type.name();
        String newKey = newName.trim().toLowerCase() + "_" + type.name();

        Category oldCategory = categories.get(oldKey);
        if (oldCategory == null) {
            return ValidationResult.invalid("Категория '" + oldName + "' не найдена");
        }

        // Проверяем, не существует ли уже категория с новым именем
        if (categories.containsKey(newKey)) {
            return ValidationResult.invalid("Категория с именем '" + newName + "' уже существует");
        }

        // Создаем новую категорию с новым именем
        Category newCategory = new Category(newName.trim(), type);

        // Обновляем все транзакции - создаем новые с обновленной категорией
        List<Transaction> transactionsToUpdate = wallet.getTransactions().stream()
                .filter(t -> t.getCategory().equals(oldCategory))
                .collect(Collectors.toList());

        for (Transaction oldTransaction : transactionsToUpdate) {
            Transaction newTransaction = new Transaction(
                    oldTransaction.getId(),
                    newCategory,
                    oldTransaction.getAmount(),
                    oldTransaction.getDescription(),
                    oldTransaction.getCreatedAt()
            );
            wallet.replaceTransaction(oldTransaction, newTransaction);
        }

        // Обновляем бюджет, если есть
        if (type == Category.TransactionType.EXPENSE) {
            Budget budget = wallet.getBudget(oldCategory);
            if (budget != null) {
                wallet.removeBudget(oldCategory);
                wallet.setBudget(newCategory, budget.getLimit());
            }
        }

        // Удаляем старую категорию и добавляем новую
        categories.remove(oldKey);
        categories.put(newKey, newCategory);

        return ValidationResult.valid();
    }

    public List<String> getUniqueCategoryNames() {
        Set<String> uniqueNames = new HashSet<>();
        for (Category category : categories.values()) {
            uniqueNames.add(category.getName());
        }
        return new ArrayList<>(uniqueNames);
    }

    public Category getCategoryByName(String name, Category.TransactionType type) {
        if (name == null || name.trim().isEmpty()) {
            return null;
        }
        String key = name.trim().toLowerCase() + "_" + type.name();
        return categories.get(key);
    }
}
