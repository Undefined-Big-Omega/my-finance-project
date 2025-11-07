package finance.utils;

import java.util.regex.Pattern;

public class MoneyValidator {
    private static final Pattern DECIMAL_PATTERN = Pattern.compile("^\\d+([.,]\\d{1,2})?$");
    private static final String ERROR_MESSAGE = "Неверный формат суммы. Допускаются только числа с максимум 2 знаками после запятой (например, 100.50 или 100,50)";

    public static class MoneyParseResult {
        private final boolean valid;
        private final String errorMessage;
        private final double amount;

        private MoneyParseResult(boolean valid, double amount, String errorMessage) {
            this.valid = valid;
            this.amount = amount;
            this.errorMessage = errorMessage;
        }

        public static MoneyParseResult success(double amount) {
            return new MoneyParseResult(true, amount, null);
        }

        public static MoneyParseResult error(String message) {
            return new MoneyParseResult(false, 0, message);
        }

        public boolean isValid() {
            return valid;
        }

        public boolean isInvalid() {
            return !valid;
        }

        public double getAmount() {
            return amount;
        }

        public String getErrorMessage() {
            return errorMessage;
        }
    }

    public static MoneyParseResult parseAmount(String amountStr) {
        if (amountStr == null || amountStr.trim().isEmpty()) {
            return MoneyParseResult.error("Сумма не может быть пустой");
        }

        String normalized = amountStr.trim();

        // Проверяем формат
        if (!DECIMAL_PATTERN.matcher(normalized).matches()) {
            return MoneyParseResult.error(ERROR_MESSAGE);
        }

        try {
            // Заменяем запятую на точку для парсинга
            String parseable = normalized.replace(',', '.');
            double amount = Double.parseDouble(parseable);

            if (amount <= 0) {
                return MoneyParseResult.error("Сумма должна быть больше нуля");
            }

            if (amount > 1_000_000_000) {
                return MoneyParseResult.error("Сумма слишком большая");
            }

            return MoneyParseResult.success(amount);
        } catch (NumberFormatException e) {
            return MoneyParseResult.error(ERROR_MESSAGE);
        }
    }
}
