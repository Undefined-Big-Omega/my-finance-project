import finance.models.*;
import finance.services.*;
import finance.utils.*;

public class MainTest {
    private static int testsRun = 0;
    private static int testsPassed = 0;
    private static int testsFailed = 0;
    private static int userCounter = 0;

    public static void main(String[] args) {
        System.out.println("Набор тестов MainTest\n");

        // Тесты аутентификации (3 теста)
        testUserRegistration();
        testUserLogin();
        testPasswordValidation();

        // Тесты финансового функционала (5 тестов)
        testAddIncome();
        testAddExpense();
        testSetBudget();
        testTransferBetweenWallets();
        testBudgetExceeded();

        // Тесты валидации денежных сумм (3 теста)
        testValidMoneyFormats();
        testInvalidMoneyFormats();
        testMoneyEdgeCases();

        // Тесты управления категориями (3 теста)
        testRenameCategory();
        testDeleteCategory();
        testCategoryUniqueness();

        // Тесты кошелька (3 теста)
        testWalletBalance();
        testWalletAlerts();
        testExpensesByCategory();

        // Тесты статистики (3 теста)
        testTotalIncome();
        testTotalExpense();
        testCategoryStatistics();

        printResults();
    }

    private static String getUniqueUser() {
        return "user" + (++userCounter);
    }

    // === ТЕСТЫ АУТЕНТИФИКАЦИИ ===
    
    private static void testUserRegistration() {
        AuthService authService = new AuthService();
        ValidationResult result = authService.register(getUniqueUser(), "Pass123!");
        
        assert result.isValid() : "Регистрация должна быть успешной";
        assert authService.getCurrentUser() != null : "Пользователь должен быть залогинен";
        
        pass("Регистрация пользователя");
    }

    private static void testUserLogin() {
        AuthService authService = new AuthService();
        String login = getUniqueUser();
        authService.register(login, "Pass123!");
        authService.logout();
        
        ValidationResult result = authService.login(login, "Pass123!");
        assert result.isValid() : "Вход должен быть успешным";
        assert authService.getCurrentUser().getLogin().equals(login) : "Текущий пользователь должен совпадать";
        
        pass("Вход пользователя");
    }

    private static void testPasswordValidation() {
        AuthService authService = new AuthService();
        
        ValidationResult weak = authService.register(getUniqueUser(), "weak");
        assert weak.isInvalid() : "Слабый пароль должен быть отклонен";
        
        ValidationResult noUpper = authService.register(getUniqueUser(), "pass123!");
        assert noUpper.isInvalid() : "Пароль без заглавной буквы должен быть отклонен";
        
        pass("Валидация пароля");
    }

    // === ТЕСТЫ ФИНАНСОВОГО ФУНКЦИОНАЛА ===
    
    private static void testAddIncome() {
        AuthService authService = new AuthService();
        FinanceService financeService = new FinanceService();
        
        authService.register(getUniqueUser(), "Pass123!");
        Wallet wallet = authService.getCurrentUser().getWallet();
        
        ValidationResult result = financeService.addIncome(wallet, "Зарплата", 50000, "");
        assert result.isValid() : "Добавление дохода должно быть успешным";
        assert wallet.getTotalIncome() == 50000 : "Общий доход должен быть 50000";
        
        pass("Добавление дохода");
    }

    private static void testAddExpense() {
        AuthService authService = new AuthService();
        FinanceService financeService = new FinanceService();
        
        authService.register(getUniqueUser(), "Pass123!");
        Wallet wallet = authService.getCurrentUser().getWallet();
        
        financeService.addIncome(wallet, "Зарплата", 10000, "");
        ValidationResult result = financeService.addExpense(wallet, "Еда", 500, "");
        
        assert result.isValid() : "Добавление расхода должно быть успешным";
        assert wallet.getTotalExpense() == 500 : "Общий расход должен быть 500";
        
        pass("Добавление расхода");
    }

    private static void testSetBudget() {
        AuthService authService = new AuthService();
        FinanceService financeService = new FinanceService();
        
        authService.register(getUniqueUser(), "Pass123!");
        Wallet wallet = authService.getCurrentUser().getWallet();
        
        ValidationResult result = financeService.setBudget(wallet, "Еда", 5000);
        assert result.isValid() : "Установка бюджета должна быть успешной";
        
        Budget budget = financeService.getBudget(wallet, "Еда");
        assert budget != null : "Бюджет должен быть создан";
        assert budget.getLimit() == 5000 : "Лимит должен быть 5000";
        
        pass("Установка бюджета");
    }

    private static void testTransferBetweenWallets() {
        AuthService authService = new AuthService();
        FinanceService financeService = new FinanceService();
        
        authService.register(getUniqueUser(), "Pass123!");
        User sender = authService.getCurrentUser();
        financeService.addIncome(sender.getWallet(), "Зарплата", 10000, "");
        authService.logout();
        
        authService.register(getUniqueUser(), "Pass123!");
        User receiver = authService.getCurrentUser();
        
        boolean success = financeService.transferBetweenWallets(
            sender.getWallet(), receiver.getWallet(), 1000, "Перевод"
        );
        
        assert success : "Перевод должен быть успешным";
        assert sender.getWallet().getBalance() == 9000 : "У отправителя должно остаться 9000";
        assert receiver.getWallet().getBalance() == 1000 : "У получателя должно быть 1000";
        
        pass("Перевод между кошельками");
    }

    private static void testBudgetExceeded() {
        AuthService authService = new AuthService();
        FinanceService financeService = new FinanceService();
        
        authService.register(getUniqueUser(), "Pass123!");
        Wallet wallet = authService.getCurrentUser().getWallet();
        
        financeService.setBudget(wallet, "Еда", 1000);
        financeService.addExpense(wallet, "Еда", 1500, "");
        
        Budget budget = financeService.getBudget(wallet, "Еда");
        assert budget.isExceeded() : "Бюджет должен быть превышен";
        assert budget.getSpent() == 1500 : "Потрачено должно быть 1500";
        
        pass("Превышение бюджета");
    }

    // === ТЕСТЫ ВАЛИДАЦИИ ДЕНЕЖНЫХ СУММ ===
    
    private static void testValidMoneyFormats() {
        assert MoneyValidator.parseAmount("100").isValid() : "100 должно быть валидно";
        assert MoneyValidator.parseAmount("100.50").isValid() : "100.50 должно быть валидно";
        assert MoneyValidator.parseAmount("100,50").isValid() : "100,50 должно быть валидно";
        assert Math.abs(MoneyValidator.parseAmount("99.99").getAmount() - 99.99) < 0.01 : "99.99 должно парситься корректно";
        
        pass("Валидные форматы денежных сумм");
    }

    private static void testInvalidMoneyFormats() {
        assert MoneyValidator.parseAmount("100.456").isInvalid() : "100.456 должно быть невалидно";
        assert MoneyValidator.parseAmount("10,4567").isInvalid() : "10,4567 должно быть невалидно";
        assert MoneyValidator.parseAmount("abc").isInvalid() : "abc должно быть невалидно";
        
        pass("Невалидные форматы денежных сумм");
    }

    private static void testMoneyEdgeCases() {
        assert MoneyValidator.parseAmount("0").isInvalid() : "0 должно быть невалидно";
        assert MoneyValidator.parseAmount("-100").isInvalid() : "-100 должно быть невалидно";
        assert MoneyValidator.parseAmount("").isInvalid() : "Пустая строка должна быть невалидна";
        assert MoneyValidator.parseAmount("10000000000").isInvalid() : "Слишком большое число должно быть невалидно";
        
        pass("Граничные случаи денежных сумм");
    }

    // === ТЕСТЫ УПРАВЛЕНИЯ КАТЕГОРИЯМИ ===
    
    private static void testRenameCategory() {
        AuthService authService = new AuthService();
        FinanceService financeService = new FinanceService();
        
        authService.register(getUniqueUser(), "Pass123!");
        Wallet wallet = authService.getCurrentUser().getWallet();
        
        financeService.addExpense(wallet, "Старое", 1000, "");
        ValidationResult result = financeService.renameCategory(wallet, "Старое", "Новое", Category.TransactionType.EXPENSE);
        
        assert result.isValid() : "Переименование должно быть успешным";
        assert wallet.getExpenseByCategory().get("Новое") != null : "Новая категория должна существовать";
        
        pass("Переименование категории");
    }

    private static void testDeleteCategory() {
        AuthService authService = new AuthService();
        FinanceService financeService = new FinanceService();
        
        authService.register(getUniqueUser(), "Pass123!");
        Wallet wallet = authService.getCurrentUser().getWallet();
        
        financeService.setBudget(wallet, "Тест", 5000);
        ValidationResult result = financeService.deleteCategory(wallet, "Тест", Category.TransactionType.EXPENSE);
        
        assert result.isValid() : "Удаление должно быть успешным";
        assert financeService.getBudget(wallet, "Тест") == null : "Бюджет должен быть удален";
        
        pass("Удаление категории");
    }

    private static void testCategoryUniqueness() {
        AuthService authService = new AuthService();
        FinanceService financeService = new FinanceService();
        
        authService.register(getUniqueUser(), "Pass123!");
        Wallet wallet = authService.getCurrentUser().getWallet();
        
        financeService.addIncome(wallet, "Зарплата", 1000, "");
        financeService.addExpense(wallet, "Еда", 500, "");
        
        java.util.List<String> names = financeService.getUniqueCategoryNames();
        assert names.contains("Зарплата") : "Зарплата должна быть в списке";
        assert names.contains("Еда") : "Еда должна быть в списке";
        
        pass("Уникальность категорий");
    }

    // === ТЕСТЫ КОШЕЛЬКА ===
    
    private static void testWalletBalance() {
        AuthService authService = new AuthService();
        FinanceService financeService = new FinanceService();
        
        authService.register(getUniqueUser(), "Pass123!");
        Wallet wallet = authService.getCurrentUser().getWallet();
        
        financeService.addIncome(wallet, "Зарплата", 10000, "");
        financeService.addExpense(wallet, "Еда", 3000, "");
        
        assert wallet.getBalance() == 7000 : "Баланс должен быть 7000";
        
        pass("Расчет баланса кошелька");
    }

    private static void testWalletAlerts() {
        AuthService authService = new AuthService();
        FinanceService financeService = new FinanceService();
        
        authService.register(getUniqueUser(), "Pass123!");
        Wallet wallet = authService.getCurrentUser().getWallet();
        
        financeService.addExpense(wallet, "Еда", 5000, "");
        
        assert wallet.hasNegativeBalance() : "Баланс должен быть отрицательным";
        assert wallet.expensesExceedIncome() : "Расходы должны превышать доходы";
        
        pass("Предупреждения кошелька");
    }

    private static void testExpensesByCategory() {
        AuthService authService = new AuthService();
        FinanceService financeService = new FinanceService();
        
        authService.register(getUniqueUser(), "Pass123!");
        Wallet wallet = authService.getCurrentUser().getWallet();
        
        financeService.addExpense(wallet, "Еда", 500, "");
        financeService.addExpense(wallet, "Еда", 300, "");
        
        assert wallet.getExpenseByCategory().get("Еда") == 800 : "Расходы на еду должны быть 800";
        
        pass("Расходы по категориям");
    }

    // === ТЕСТЫ СТАТИСТИКИ ===
    
    private static void testTotalIncome() {
        AuthService authService = new AuthService();
        FinanceService financeService = new FinanceService();
        
        authService.register(getUniqueUser(), "Pass123!");
        Wallet wallet = authService.getCurrentUser().getWallet();
        
        financeService.addIncome(wallet, "Зарплата", 50000, "");
        financeService.addIncome(wallet, "Бонус", 10000, "");
        
        assert wallet.getTotalIncome() == 60000 : "Общий доход должен быть 60000";
        
        pass("Подсчет общего дохода");
    }

    private static void testTotalExpense() {
        AuthService authService = new AuthService();
        FinanceService financeService = new FinanceService();
        
        authService.register(getUniqueUser(), "Pass123!");
        Wallet wallet = authService.getCurrentUser().getWallet();
        
        financeService.addExpense(wallet, "Еда", 1000, "");
        financeService.addExpense(wallet, "Транспорт", 500, "");
        
        assert wallet.getTotalExpense() == 1500 : "Общие расходы должны быть 1500";
        
        pass("Подсчет общих расходов");
    }

    private static void testCategoryStatistics() {
        AuthService authService = new AuthService();
        FinanceService financeService = new FinanceService();
        
        authService.register(getUniqueUser(), "Pass123!");
        Wallet wallet = authService.getCurrentUser().getWallet();
        
        financeService.addIncome(wallet, "Зарплата", 30000, "");
        financeService.addIncome(wallet, "Зарплата", 20000, "");
        
        assert wallet.getIncomeByCategory().get("Зарплата") == 50000 : "Доход по категории Зарплата должен быть 50000";
        
        pass("Статистика по категориям");
    }

    // === ВСПОМОГАТЕЛЬНЫЕ МЕТОДЫ ===
    
    private static void pass(String testName) {
        testsRun++;
        testsPassed++;
        System.out.println("+ " + testName);
    }

    private static void fail(String testName, String message) {
        testsRun++;
        testsFailed++;
        System.out.println("- " + testName + ": " + message);
    }

    private static void printResults() {
        System.out.println("\n Итог тестов");
        System.out.println("Всего тестов: " + testsRun);
        System.out.println("Пройдено: " + testsPassed + " +");
        System.out.println("Провалено: " + testsFailed + " -");
        System.out.println("Процент успеха: " + (testsRun > 0 ? (testsPassed * 100 / testsRun) : 0) + "%");
        System.out.println("______________________________");

        if (testsFailed > 0) {
            System.exit(1);
        }
    }
}
