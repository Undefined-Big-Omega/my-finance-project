package finance.models;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

public class Wallet {
    private final String ownerLogin;
    private final List<Transaction> transactions;
    private final Map<Category, Budget> budgets;
    private double balance;

    public Wallet(String ownerLogin) {
        if (ownerLogin == null || ownerLogin.trim().isEmpty()) {
            throw new IllegalArgumentException("Логин владельца не может быть пустым");
        }
        this.ownerLogin = ownerLogin;
        this.transactions = new ArrayList<>();
        this.budgets = new ConcurrentHashMap<>();
        this.balance = 0.0;
    }

    public Wallet(String ownerLogin, double balance, List<Transaction> transactions, Map<Category, Budget> budgets) {
        this.ownerLogin = ownerLogin;
        this.balance = balance;
        this.transactions = transactions != null ? new ArrayList<>(transactions) : new ArrayList<>();
        this.budgets = budgets != null ? new ConcurrentHashMap<>(budgets) : new ConcurrentHashMap<>();
    }

    public String getOwnerLogin() {
        return ownerLogin;
    }

    public double getBalance() {
        return balance;
    }

    public List<Transaction> getTransactions() {
        return Collections.unmodifiableList(transactions);
    }

    public void addTransaction(Transaction transaction) {
        if (transaction == null) {
            throw new IllegalArgumentException("Транзакция не может быть null");
        }

        transactions.add(transaction);

        if (transaction.getCategory().getType() == Category.TransactionType.INCOME) {
            balance += transaction.getAmount();
        } else {
            balance -= transaction.getAmount();

            Budget budget = budgets.get(transaction.getCategory());
            if (budget != null) {
                budget.addExpense(transaction.getAmount());
            }
        }
    }

    public void setBudget(Category category, double limit) {
        if (category == null) {
            throw new IllegalArgumentException("Категория не может быть null");
        }
        if (category.getType() != Category.TransactionType.EXPENSE) {
            throw new IllegalArgumentException("Бюджет можно установить только для категорий расходов");
        }

        double currentSpent = getExpensesByCategory(category);
        Budget budget = new Budget(category, limit, currentSpent);
        budgets.put(category, budget);
    }

    public Budget getBudget(Category category) {
        return budgets.get(category);
    }

    public Map<Category, Budget> getAllBudgets() {
        return Collections.unmodifiableMap(budgets);
    }

    public double getTotalIncome() {
        return transactions.stream()
                .filter(t -> t.getCategory().getType() == Category.TransactionType.INCOME)
                .mapToDouble(Transaction::getAmount)
                .sum();
    }

    public double getTotalExpense() {
        return transactions.stream()
                .filter(t -> t.getCategory().getType() == Category.TransactionType.EXPENSE)
                .mapToDouble(Transaction::getAmount)
                .sum();
    }

    public Map<String, Double> getIncomeByCategory() {
        return transactions.stream()
                .filter(t -> t.getCategory().getType() == Category.TransactionType.INCOME)
                .collect(Collectors.groupingBy(
                        t -> t.getCategory().getName(),
                        Collectors.summingDouble(Transaction::getAmount)
                ));
    }

    public Map<String, Double> getExpenseByCategory() {
        return transactions.stream()
                .filter(t -> t.getCategory().getType() == Category.TransactionType.EXPENSE)
                .collect(Collectors.groupingBy(
                        t -> t.getCategory().getName(),
                        Collectors.summingDouble(Transaction::getAmount)
                ));
    }

    public double getExpensesByCategory(Category category) {
        return transactions.stream()
                .filter(t -> t.getCategory().equals(category))
                .filter(t -> t.getCategory().getType() == Category.TransactionType.EXPENSE)
                .mapToDouble(Transaction::getAmount)
                .sum();
    }

    public double getIncomesByCategory(Category category) {
        return transactions.stream()
                .filter(t -> t.getCategory().equals(category))
                .filter(t -> t.getCategory().getType() == Category.TransactionType.INCOME)
                .mapToDouble(Transaction::getAmount)
                .sum();
    }

    public double getTotalByCategories(List<Category> categories) {
        if (categories == null || categories.isEmpty()) {
            return 0.0;
        }

        Set<Category> categorySet = new HashSet<>(categories);
        return transactions.stream()
                .filter(t -> categorySet.contains(t.getCategory()))
                .mapToDouble(Transaction::getAmount)
                .sum();
    }

    public List<Budget> getExceededBudgets() {
        return budgets.values().stream()
                .filter(Budget::isExceeded)
                .collect(Collectors.toList());
    }

    public boolean hasNegativeBalance() {
        return balance < 0;
    }

    public boolean expensesExceedIncome() {
        return getTotalExpense() > getTotalIncome();
    }

    public void removeBudget(Category category) {
        budgets.remove(category);
    }

    public void replaceTransaction(Transaction oldTransaction, Transaction newTransaction) {
        int index = transactions.indexOf(oldTransaction);
        if (index != -1) {
            transactions.set(index, newTransaction);
        }
    }
}
