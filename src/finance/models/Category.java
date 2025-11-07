package finance.models;

import java.util.Objects;

public class Category {
    private final String name;
    private final TransactionType type;

    public Category(String name, TransactionType type) {
        if (name == null || name.trim().isEmpty()) {
            throw new IllegalArgumentException("Название категории не может быть пустым");
        }
        if (type == null) {
            throw new IllegalArgumentException("Тип транзакции не может быть null");
        }
        this.name = name.trim();
        this.type = type;
    }

    public String getName() {
        return name;
    }

    public TransactionType getType() {
        return type;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Category category = (Category) o;
        return Objects.equals(name, category.name) && type == category.type;
    }

    @Override
    public int hashCode() {
        return Objects.hash(name, type);
    }

    @Override
    public String toString() {
        return name + " (" + type + ")";
    }

    public enum TransactionType {
        INCOME("Доход"),
        EXPENSE("Расход");

        private final String displayName;

        TransactionType(String displayName) {
            this.displayName = displayName;
        }

        public String getDisplayName() {
            return displayName;
        }
    }
}
