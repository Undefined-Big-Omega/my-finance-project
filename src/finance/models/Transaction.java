package finance.models;

import java.time.Instant;
import java.util.UUID;

public class Transaction {
    private final String id;
    private final Category category;
    private final double amount;
    private final Instant createdAt;
    private final String description;

    public Transaction(Category category, double amount, String description) {
        if (category == null) {
            throw new IllegalArgumentException("Категория не может быть null");
        }
        if (amount <= 0) {
            throw new IllegalArgumentException("Сумма должна быть положительной");
        }
        this.id = UUID.randomUUID().toString();
        this.category = category;
        this.amount = amount;
        this.description = description != null ? description.trim() : "";
        this.createdAt = Instant.now();
    }

    public Transaction(String id, Category category, double amount, String description, Instant createdAt) {
        this.id = id;
        this.category = category;
        this.amount = amount;
        this.description = description != null ? description : "";
        this.createdAt = createdAt != null ? createdAt : Instant.now();
    }

    public String getId() {
        return id;
    }

    public Category getCategory() {
        return category;
    }

    public double getAmount() {
        return amount;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public String getDescription() {
        return description;
    }

    public long getCreatedAtMillis() {
        return createdAt.toEpochMilli();
    }

    @Override
    public String toString() {
        return String.format("%s: %.2f руб. [%s]%s",
                category.getName(),
                amount,
                category.getType().getDisplayName(),
                description.isEmpty() ? "" : " - " + description);
    }
}
