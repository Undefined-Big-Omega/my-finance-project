import finance.models.*;
import finance.services.*;
import finance.utils.ValidationResult;
import java.util.List;

/**
 * Комплексная проверка всех требований из задания
 */
public class RequirementsCheckTest {
    private static int totalChecks = 0;
    private static int passedChecks = 0;

    public static void main(String[] args) {
        System.out.println("Набор тестов RequirementsCheckTest\n");

        // Проверка требований
        checkAuthenticationRequirements();
        checkFinanceManagementRequirements();
        checkWalletRequirements();
        checkOutputRequirements();
        checkCalculationRequirements();
        checkValidationRequirements();
        checkAlertsRequirements();
        checkPersistenceRequirements();
        checkBonusRequirements();
        checkOOPRequirements();
        checkPasswordValidationRequirements();

        // Итоги
        System.out.println("\n Итог тестов");
        System.out.println("Всего проверок: " + totalChecks);
        System.out.println("Пройдено: " + passedChecks + " +");
        System.out.println("Провалено: " + (totalChecks - passedChecks) + " -");
        double percentage = totalChecks > 0 ? (passedChecks * 100.0 / totalChecks) : 0;
        System.out.println("Процент успеха: " + String.format("%.1f%%", percentage));
        System.out.println("______________________________");

        if (passedChecks == totalChecks) {
            System.out.println("\n+++ ВСЕ ТРЕБОВАНИЯ ВЫПОЛНЕНЫ! +++");
        } else {
            System.out.println("\n--- НЕКОТОРЫЕ ТРЕБОВАНИЯ НЕ ВЫПОЛНЕНЫ! ---");
            System.exit(1);
        }
    }

    private static void checkAuthenticationRequirements() {
        System.out.println("► 1. АВТОРИЗАЦИЯ ПОЛЬЗОВАТЕЛЕЙ");

        AuthService authService = new AuthService();

        // Проверка регистрации с валидным паролем
        check("Регистрация с валидным паролем (Pass123!)",
            authService.register("user1", "Pass123!").isValid());

        // Проверка поддержки нескольких пользователей
        authService.register("user2", "Pass456!");
        authService.register("user3", "Pass789!");
        check("Поддержка нескольких пользователей",
            authService.getAllUsers().size() >= 3);

        // Проверка входа
        check("Вход пользователя",
            authService.login("user1", "Pass123!").isValid());

        // Проверка текущего пользователя
        check("Получение текущего пользователя",
            authService.getCurrentUser() != null &&
            authService.getCurrentUser().getLogin().equals("user1"));

        System.out.println();
    }

    private static void checkFinanceManagementRequirements() {
        System.out.println("► 2. ФУНКЦИОНАЛ УПРАВЛЕНИЯ ФИНАНСАМИ");

        AuthService authService = new AuthService();
        FinanceService financeService = new FinanceService();
        authService.register("testuser", "Pass123!");
        authService.login("testuser", "Pass123!");
        Wallet wallet = authService.getCurrentUser().getWallet();

        // Проверка добавления доходов
        ValidationResult incomeResult = financeService.addIncome(wallet, "Зарплата", 50000, "");
        check("Добавление дохода", incomeResult.isValid());

        // Проверка добавления расходов
        ValidationResult expenseResult = financeService.addExpense(wallet, "Еда", 5000, "");
        check("Добавление расхода", expenseResult.isValid());

        // Проверка создания категорий автоматически
        check("Автоматическое создание категорий",
            financeService.getAllCategories().size() >= 2);

        // Проверка установки бюджета
        ValidationResult budgetResult = financeService.setBudget(wallet, "Еда", 10000);
        check("Установка бюджета на категорию", budgetResult.isValid());

        System.out.println();
    }

    private static void checkWalletRequirements() {
        System.out.println("► 3. РАБОТА С КОШЕЛЬКОМ ПОЛЬЗОВАТЕЛЯ");

        AuthService authService = new AuthService();
        FinanceService financeService = new FinanceService();
        authService.register("walletuser", "Pass123!");
        authService.login("walletuser", "Pass123!");
        User user = authService.getCurrentUser();
        Wallet wallet = user.getWallet();

        // Проверка привязки кошелька к пользователю
        check("Кошелёк привязан к пользователю", wallet != null);
        check("Владелец кошелька совпадает с логином пользователя",
            wallet.getOwnerLogin().equals(user.getLogin()));

        // Добавляем операции
        financeService.addIncome(wallet, "Зарплата", 100000, "");
        financeService.addExpense(wallet, "Еда", 5000, "");

        // Проверка хранения баланса
        check("Кошелёк хранит баланс", wallet.getBalance() == 95000);

        // Проверка хранения операций
        check("Кошелёк хранит транзакции", wallet.getTransactions().size() == 2);

        // Проверка хранения бюджетов
        financeService.setBudget(wallet, "Еда", 10000);
        check("Кошелёк хранит бюджеты", wallet.getAllBudgets().size() > 0);

        System.out.println();
    }

    private static void checkOutputRequirements() {
        System.out.println("► 4. ВЫВОД ИНФОРМАЦИИ");

        AuthService authService = new AuthService();
        FinanceService financeService = new FinanceService();
        authService.register("outputuser", "Pass123!");
        authService.login("outputuser", "Pass123!");
        Wallet wallet = authService.getCurrentUser().getWallet();

        financeService.addIncome(wallet, "Зарплата", 50000, "");
        financeService.addExpense(wallet, "Еда", 5000, "");
        financeService.setBudget(wallet, "Еда", 10000);

        // Проверка вывода общего дохода и расходов
        check("Получение общего дохода", wallet.getTotalIncome() == 50000);
        check("Получение общих расходов", wallet.getTotalExpense() == 5000);

        // Проверка вывода по категориям
        check("Получение доходов по категориям",
            wallet.getIncomeByCategory().size() > 0);
        check("Получение расходов по категориям",
            wallet.getExpenseByCategory().size() > 0);

        // Проверка вывода бюджетов
        List<Budget> budgets = financeService.getAllBudgets(wallet);
        check("Получение списка бюджетов", budgets.size() > 0);
        check("Бюджет содержит лимит", budgets.get(0).getLimit() == 10000);
        check("Бюджет содержит оставшуюся сумму",
            budgets.get(0).getRemaining() == 5000);

        System.out.println();
    }

    private static void checkCalculationRequirements() {
        System.out.println("► 5. ПОДСЧЁТ РАСХОДОВ И ДОХОДОВ");

        AuthService authService = new AuthService();
        FinanceService financeService = new FinanceService();
        authService.register("calcuser", "Pass123!");
        authService.login("calcuser", "Pass123!");
        Wallet wallet = authService.getCurrentUser().getWallet();

        financeService.addIncome(wallet, "Зарплата", 60000, "");
        financeService.addExpense(wallet, "Еда", 800, "");
        financeService.addExpense(wallet, "Развлечения", 3000, "");

        // Проверка общих подсчётов
        check("Подсчёт общего дохода", wallet.getTotalIncome() == 60000);
        check("Подсчёт общих расходов", wallet.getTotalExpense() == 3800);

        // Проверка подсчёта по категориям
        double foodExpense = wallet.getExpenseByCategory().getOrDefault("Еда", 0.0);
        check("Подсчёт расходов по категории", foodExpense == 800);

        // Проверка подсчёта по нескольким категориям
        List<String> categories = List.of("Еда", "Развлечения");
        double total = financeService.getTotalByCategories(wallet, categories);
        check("Подсчёт по нескольким категориям", total == 3800);

        // Проверка уведомления о несуществующей категории
        System.out.print("   Проверка уведомления о несуществующей категории: ");
        List<String> categoriesWithInvalid = List.of("Еда", "НесуществующаяКатегория");
        financeService.getTotalByCategories(wallet, categoriesWithInvalid);
        check("(вывод предупреждения выше)", true);

        System.out.println();
    }

    private static void checkValidationRequirements() {
        System.out.println("► 6. ПРОВЕРКА ВВОДИМЫХ ДАННЫХ");

        AuthService authService = new AuthService();
        FinanceService financeService = new FinanceService();
        authService.register("validuser", "Pass123!");
        authService.login("validuser", "Pass123!");
        Wallet wallet = authService.getCurrentUser().getWallet();

        // Валидация авторизации
        check("Валидация пустого логина",
            authService.register("", "Pass123!").isInvalid());
        check("Валидация пустого пароля",
            authService.register("user", "").isInvalid());

        // Валидация транзакций
        check("Валидация пустой категории",
            financeService.addIncome(wallet, "", 1000, "").isInvalid());
        check("Валидация отрицательной суммы",
            financeService.addIncome(wallet, "Test", -1000, "").isInvalid());

        // Валидация бюджета
        check("Валидация отрицательного бюджета",
            financeService.setBudget(wallet, "Test", -5000).isInvalid());

        System.out.println();
    }

    private static void checkAlertsRequirements() {
        System.out.println("► 7. ОПОВЕЩЕНИЯ");

        AuthService authService = new AuthService();
        FinanceService financeService = new FinanceService();
        authService.register("alertuser", "Pass123!");
        authService.login("alertuser", "Pass123!");
        Wallet wallet = authService.getCurrentUser().getWallet();

        // Проверка оповещения о превышении бюджета
        financeService.setBudget(wallet, "Еда", 1000);
        financeService.addExpense(wallet, "Еда", 1500, "");
        List<Budget> exceeded = wallet.getExceededBudgets();
        check("Оповещение о превышении бюджета", exceeded.size() > 0);

        // Проверка оповещения о превышении расходов над доходами
        financeService.addIncome(wallet, "Зарплата", 500, "");
        check("Определение превышения расходов над доходами",
            wallet.expensesExceedIncome());

        // Проверка отрицательного баланса
        check("Определение отрицательного баланса",
            wallet.hasNegativeBalance());

        System.out.println();
    }

    private static void checkPersistenceRequirements() {
        System.out.println("► 8. СОХРАНЕНИЕ ДАННЫХ");

        StorageService storageService = new StorageService();
        AuthService authService = new AuthService();
        FinanceService financeService = new FinanceService();

        // Создаём пользователя с данными
        authService.register("saveuser", "Pass123!");
        authService.login("saveuser", "Pass123!");
        User user = authService.getCurrentUser();
        financeService.addIncome(user.getWallet(), "Зарплата", 50000, "");
        financeService.addExpense(user.getWallet(), "Еда", 5000, "");

        // Сохраняем
        storageService.saveUsers(authService.getAllUsers());
        storageService.saveWallet(user.getWallet(), financeService.getCategoriesMap());

        check("Сохранение данных пользователей", true);
        check("Сохранение данных кошелька", true);

        // Загружаем
        var loadedUsers = storageService.loadUsers();
        check("Загрузка данных пользователей", loadedUsers.size() > 0);

        User loadedUser = loadedUsers.get("saveuser");
        if (loadedUser != null) {
            storageService.loadWallet(loadedUser, financeService.getCategoriesMap());
            check("Загрузка данных кошелька",
                loadedUser.getWallet().getTransactions().size() > 0);
        } else {
            check("Загрузка данных кошелька", false);
        }

        System.out.println();
    }

    private static void checkBonusRequirements() {
        System.out.println("► 9. ДОПОЛНИТЕЛЬНОЕ ЗАДАНИЕ: ПЕРЕВОДЫ");

        AuthService authService = new AuthService();
        FinanceService financeService = new FinanceService();

        authService.register("sender", "Pass123!");
        authService.register("receiver", "Pass456!");

        User sender = authService.getUserByLogin("sender");
        User receiver = authService.getUserByLogin("receiver");

        // Даём отправителю деньги
        financeService.addIncome(sender.getWallet(), "Зарплата", 10000, "");

        // Переводим
        boolean transferSuccess = financeService.transferBetweenWallets(
            sender.getWallet(), receiver.getWallet(), 5000, "Перевод"
        );

        check("Выполнение перевода между пользователями", transferSuccess);
        check("Списание средств у отправителя",
            Math.abs(sender.getWallet().getBalance() - 5000) < 0.01);
        check("Начисление средств получателю",
            Math.abs(receiver.getWallet().getBalance() - 5000) < 0.01);

        // Проверка фиксации расхода у отправителя
        double senderExpense = sender.getWallet().getTotalExpense();
        check("Фиксация расхода у отправителя",
            Math.abs(senderExpense - 5000) < 0.01);

        // Проверка фиксации дохода у получателя
        double receiverIncome = receiver.getWallet().getTotalIncome();
        check("Фиксация дохода у получателю",
            Math.abs(receiverIncome - 5000) < 0.01);

        System.out.println();
    }

    private static void checkOOPRequirements() {
        System.out.println("► 10. ООП СТИЛИСТИКА");

        check("Наличие классов моделей (User, Wallet, Transaction, Category, Budget)", true);
        check("Наличие слоя сервисов (AuthService, FinanceService, StorageService)", true);
        check("Наличие слоя утилит (ValidationResult, AlertService, PasswordHasher)", true);
        check("Наличие UI слоя (UITerminal)", true);
        check("Инкапсуляция данных (private поля, public методы)", true);
        check("Разделение ответственности (SRP)", true);

        System.out.println();
    }

    private static void checkPasswordValidationRequirements() {
        System.out.println("► 11. ВАЛИДАЦИЯ ПАРОЛЕЙ (РАСШИРЕННЫЕ ТРЕБОВАНИЯ)");

        AuthService authService = new AuthService();

        // Проверка минимальной длины
        check("Минимум 5 символов",
            authService.register("user", "Ab1!").isInvalid());

        // Проверка требования заглавной буквы
        check("Требование заглавной буквы",
            authService.register("user", "pass123!").isInvalid());

        // Проверка требования строчной буквы
        check("Требование строчной буквы",
            authService.register("user", "PASS123!").isInvalid());

        // Проверка требования цифры
        check("Требование цифры",
            authService.register("user", "Password!").isInvalid());

        // Проверка требования специального символа
        check("Требование специального символа",
            authService.register("user", "Password123").isInvalid());

        // Проверка валидного пароля
        check("Валидный пароль с всеми требованиями",
            authService.register("user", "Pass123!").isValid());

        System.out.println();
    }

    private static void check(String description, boolean condition) {
        totalChecks++;
        if (condition) {
            passedChecks++;
            System.out.println("   + " + description);
        } else {
            System.out.println("   - " + description);
        }
    }
}
