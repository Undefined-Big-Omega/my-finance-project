package finance.models;

public class Budget {
    private final Category category;
    private double limit;
    private double spent;

    public Budget(Category category, double limit) {
        if (category == null) {
            throw new IllegalArgumentException("Категория не может быть null");
        }
        if (category.getType() != Category.TransactionType.EXPENSE) {
            throw new IllegalArgumentException("Бюджет можно установить только для категорий расходов");
        }
        if (limit <= 0) {
            throw new IllegalArgumentException("Лимит должен быть положительным");
        }
        this.category = category;
        this.limit = limit;
        this.spent = 0.0;
    }

    public Budget(Category category, double limit, double spent) {
        this.category = category;
        this.limit = limit;
        this.spent = spent;
    }

    public Category getCategory() {
        return category;
    }

    public double getLimit() {
        return limit;
    }

    public void setLimit(double limit) {
        if (limit <= 0) {
            throw new IllegalArgumentException("Лимит должен быть положительным");
        }
        this.limit = limit;
    }

    public double getSpent() {
        return spent;
    }

    public void addExpense(double amount) {
        if (amount <= 0) {
            throw new IllegalArgumentException("Сумма расхода должна быть положительной");
        }
        this.spent += amount;
    }

    public double getRemaining() {
        return limit - spent;
    }

    public boolean isExceeded() {
        return spent > limit;
    }

    public boolean isNearLimit(double threshold) {
        return (spent / limit) >= threshold;
    }

    public double getUsagePercentage() {
        return (spent / limit) * 100.0;
    }

    @Override
    public String toString() {
        return String.format("%s: %.2f / %.2f руб. (осталось: %.2f руб.)",
                category.getName(),
                spent,
                limit,
                getRemaining());
    }
}
