package finance.utils;

import finance.models.Budget;
import finance.models.Wallet;

import java.util.ArrayList;
import java.util.List;

public class AlertService {
    private static final double WARNING_THRESHOLD = 0.5;

    public static List<String> checkWalletAlerts(Wallet wallet) {
        List<String> alerts = new ArrayList<>();

        checkBudgetAlerts(wallet, alerts);
        checkBalanceAlerts(wallet, alerts);

        return alerts;
    }

    private static void checkBudgetAlerts(Wallet wallet, List<String> alerts) {
        List<Budget> exceededBudgets = wallet.getExceededBudgets();

        for (Budget budget : exceededBudgets) {
            alerts.add(String.format(
                    "Превышен бюджет категории '%s'! Потрачено %.2f руб. из %.2f руб. (перерасход: %.2f руб.)",
                    budget.getCategory().getName(),
                    budget.getSpent(),
                    budget.getLimit(),
                    budget.getSpent() - budget.getLimit()
            ));
        }

        for (Budget budget : wallet.getAllBudgets().values()) {
            if (!budget.isExceeded() && budget.isNearLimit(WARNING_THRESHOLD)) {
                alerts.add(String.format(
                        "Бюджет категории '%s' почти исчерпан! Использовано %.1f%% (%.2f руб. из %.2f руб.)",
                        budget.getCategory().getName(),
                        budget.getUsagePercentage(),
                        budget.getSpent(),
                        budget.getLimit()
                ));
            }
        }
    }

    private static void checkBalanceAlerts(Wallet wallet, List<String> alerts) {
        if (wallet.hasNegativeBalance()) {
            alerts.add(String.format(
                    "Отрицательный баланс! Текущий баланс: %.2f руб.",
                    wallet.getBalance()
            ));
        }

        if (wallet.expensesExceedIncome()) {
            alerts.add(String.format(
                    "Расходы превышают доходы! Доходы: %.2f руб., Расходы: %.2f руб.",
                    wallet.getTotalIncome(),
                    wallet.getTotalExpense()
            ));
        }
    }

    public static String formatAlert(String message) {
        return message;
    }
}
