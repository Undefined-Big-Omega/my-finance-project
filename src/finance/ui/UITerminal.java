package finance.ui;

import finance.models.*;
import finance.services.*;
import finance.utils.*;

import java.text.SimpleDateFormat;
import java.util.*;

public class UITerminal {
    private static final String INVALID_INPUT = "Ошибка: введены некорректные данные";
    private static final String EMPTY_VALUE_ERROR = "Поле не должно быть пустым";

    private final AuthService authService;
    private final FinanceService financeService;
    private final StorageService storageService;
    private final Scanner scanner;

    public UITerminal(AuthService authService, FinanceService financeService, StorageService storageService) {
        this.authService = authService;
        this.financeService = financeService;
        this.storageService = storageService;
        this.scanner = new Scanner(System.in);
    }

    public void start() {
        loadAllData();
        performAuthentication();

        boolean isRunning = true;
        while (isRunning) {
            displayMainMenu();
            String choice = scanner.nextLine().trim();

            switch (choice.toLowerCase()) {
                case "1":
                    handleAddIncome();
                    break;
                case "2":
                    handleAddExpense();
                    break;
                case "3":
                    handleSetBudget();
                    break;
                case "4":
                    handleViewStatistics();
                    break;
                case "5":
                    handleViewBudgets();
                    break;
                case "6":
                    handleViewRecentTransactions();
                    break;
                case "7":
                    handleTransfer();
                    break;
                case "8":
                    handleManageCategories();
                    break;
                case "9":
                    handleSwitchUser();
                    break;
                case "10":
                    showMainMenuHelp();
                    break;
                case "0":
                    isRunning = false;
                    handleExit();
                    break;
                default:
                    System.out.println("\n" + INVALID_INPUT);
            }

            if (isRunning && authService.isAuthenticated()) {
                showAlerts();
            }
        }
    }

    private void loadAllData() {
        Map<String, User> users = storageService.loadUsers();
        authService.getAllUsers().putAll(users);

        for (User user : users.values()) {
            storageService.loadWallet(user, financeService.getCategoriesMap());
        }
    }

    private void performAuthentication() {
        System.out.println("\nСистема управления личными финансами");
        System.out.println("Необходима авторизация");
        System.out.println("\n 1) Войти \n 2) Зарегистрироваться \n 9) Справка \n 0) Выход \n Ваш выбор: ");

        String choice = scanner.nextLine().trim();

        if (choice.equals("0")) {
            System.out.println("\nВыход из приложения. До свидания!");
            System.exit(0);
        } else if (choice.equals("2")) {
            handleRegistration();
        } else if (choice.equals("9")) {
            showAuthenticationHelp();
            performAuthentication();
        } else {
            handleLogin();
        }

        System.out.println();
    }

    private void showAuthenticationHelp() {
        System.out.println("\n>>> Справка: Меню авторизации");
        System.out.println("\n1) Войти");
        System.out.println("   Войти в существующий аккаунт, используя логин и пароль.");
        System.out.println("   Если аккаунт не найден, система предложит создать новый.");
        System.out.println("\n2) Зарегистрироваться");
        System.out.println("   Создать новый аккаунт с логином и паролем.");
        System.out.println("   Требования к логину: не должен быть пустым.");
        System.out.println("   Требования к паролю:");
        System.out.println("   - Минимум 5 символов");
        System.out.println("   - Минимум 1 заглавная буква (A-Z)");
        System.out.println("   - Минимум 1 строчная буква (a-z)");
        System.out.println("   - Минимум 1 цифра (0-9)");
        System.out.println("   - Минимум 1 специальный символ (!@#$%^&* и т.д.)");
        System.out.println("   - Только английские буквы, цифры и символы");
        System.out.println("   После регистрации вы автоматически войдёте в систему.");
        System.out.println("\n9) Справка");
        System.out.println("   Показать эту справку с описанием команд.");
        System.out.println("\n0) Выход");
        System.out.println("   Завершить работу приложения без входа в систему.");
        System.out.println("\nПримечание: в меню входа и регистрации можно ввести '0' для возврата.\n");
    }

    private void showMainMenuHelp() {
        System.out.println("\n>>> Справка: Главное меню");
        System.out.println("\n1) Добавить доход");
        System.out.println("   Добавить доход в указанную категорию (например, Зарплата, Бонус).");
        System.out.println("   Укажите название категории, сумму и описание (необязательно).");
        System.out.println("   Баланс автоматически увеличится на указанную сумму.");
        System.out.println("\n2) Добавить расход");
        System.out.println("   Добавить расход в указанную категорию (например, Еда, Развлечения).");
        System.out.println("   Укажите название категории, сумму и описание (необязательно).");
        System.out.println("   Баланс автоматически уменьшится. Если установлен бюджет, он обновится.");
        System.out.println("\n3) Установить/изменить бюджет");
        System.out.println("   Установить лимит расходов для категории.");
        System.out.println("   Система будет предупреждать при достижении 80% лимита или его превышении.");
        System.out.println("\n4) Посмотреть статистику");
        System.out.println("   Показать общий доход, расходы и баланс.");
        System.out.println("   Вывод доходов и расходов по категориям.");
        System.out.println("   Возможность подсчитать сумму по нескольким выбранным категориям.");
        System.out.println("\n5) Посмотреть бюджеты");
        System.out.println("   Показать все установленные бюджеты с информацией:");
        System.out.println("   - Лимит бюджета");
        System.out.println("   - Потрачено на данный момент");
        System.out.println("   - Остаток и процент использования");
        System.out.println("\n6) Посмотреть последние транзакции");
        System.out.println("   Вывести список последних транзакций (по умолчанию 25).");
        System.out.println("   Для каждой транзакции показывается дата, тип, категория, сумма и описание.");
        System.out.println("\n7) Перевести средства другому пользователю");
        System.out.println("   Перевести деньги другому зарегистрированному пользователю по логину.");
        System.out.println("   У отправителя фиксируется расход, у получателя - доход.");
        System.out.println("   Требуется достаточный баланс для перевода.");
        System.out.println("\n8) Сменить пользователя");
        System.out.println("   Выйти из текущего аккаунта (данные сохраняются автоматически)");
        System.out.println("   и войти в другой аккаунт или зарегистрировать новый.");
        System.out.println("\n9) Справка");
        System.out.println("   Показать эту справку с описанием всех команд.");
        System.out.println("\n0) Выход");
        System.out.println("   Сохранить все данные и завершить работу приложения.");
        System.out.println("\nПримечание: после каждой операции система проверяет бюджеты и баланс,");
        System.out.println("выводя предупреждения при необходимости.\n");
    }

    private void handleRegistration() {
        System.out.println("\n>>> Регистрация нового пользователя");
        System.out.print("Введите логин (0 - назад): ");
        String login = scanner.nextLine().trim();

        if (login.equals("0")) {
            performAuthentication();
            return;
        }

        System.out.print("Введите пароль: ");
        String password = scanner.nextLine().trim();

        ValidationResult result = authService.register(login, password);
        if (result.isValid()) {
            System.out.println("Регистрация успешна!");
            authService.login(login, password);
            System.out.println("Вы автоматически авторизованы как: " + login);
        } else {
            System.out.println("Ошибка регистрации: " + result.getMessage());
            performAuthentication();
        }
    }

    private void handleLogin() {
        System.out.println("\n>>> Вход в систему");
        System.out.print("Введите логин (0 - назад): ");
        String login = scanner.nextLine().trim();

        if (login.equals("0")) {
            performAuthentication();
            return;
        }

        System.out.print("Введите пароль: ");
        String password = scanner.nextLine().trim();

        ValidationResult result = authService.login(login, password);
        if (result.isValid()) {
            System.out.println("Добро пожаловать, " + login + "!");
        } else {
            System.out.println("Ошибка входа: " + result.getMessage());
            performAuthentication();
        }
    }

    private void displayMainMenu() {
        User currentUser = authService.getCurrentUser();
        System.out.println("\n--- Главное меню (Пользователь: " + currentUser.getLogin() + ") ---");
        System.out.println("Баланс: " + String.format("%.2f", currentUser.getWallet().getBalance()) + " руб.");
        System.out.println("\n1) Добавить доход");
        System.out.println("2) Добавить расход");
        System.out.println("3) Установить/изменить бюджет");
        System.out.println("4) Посмотреть статистику");
        System.out.println("5) Посмотреть бюджеты");
        System.out.println("6) Посмотреть последние транзакции");
        System.out.println("7) Перевести средства другому пользователю");
        System.out.println("8) Управление категориями");
        System.out.println("9) Сменить пользователя");
        System.out.println("10) Справка");
        System.out.println("0) Выход");
        System.out.print("Введите команду: ");
    }

    private void handleAddIncome() {
        System.out.println("\n>>> Добавление дохода");
        System.out.print("Категория (например, Зарплата, Бонус): ");
        String category = scanner.nextLine().trim();

        if (category.isEmpty()) {
            System.out.println(EMPTY_VALUE_ERROR);
            return;
        }

        System.out.print("Сумма: ");
        String amountStr = scanner.nextLine().trim();

        MoneyValidator.MoneyParseResult parseResult = MoneyValidator.parseAmount(amountStr);
        if (parseResult.isInvalid()) {
            System.out.println("Ошибка: " + parseResult.getErrorMessage());
            return;
        }

        double amount = parseResult.getAmount();
        System.out.print("Описание (необязательно): ");
        String description = scanner.nextLine().trim();

        User user = authService.getCurrentUser();
        ValidationResult result = financeService.addIncome(user.getWallet(), category, amount, description);

        if (result.isValid()) {
            System.out.println("Доход добавлен успешно!");
            System.out.println("Текущий баланс: " + String.format("%.2f", user.getWallet().getBalance()) + " руб.");
        } else {
            System.out.println("Ошибка: " + result.getMessage());
        }
    }

    private void handleAddExpense() {
        System.out.println("\n>>> Добавление расхода");
        System.out.print("Категория (например, Еда, Развлечения, Коммунальные услуги): ");
        String category = scanner.nextLine().trim();

        if (category.isEmpty()) {
            System.out.println(EMPTY_VALUE_ERROR);
            return;
        }

        System.out.print("Сумма: ");
        String amountStr = scanner.nextLine().trim();

        MoneyValidator.MoneyParseResult parseResult = MoneyValidator.parseAmount(amountStr);
        if (parseResult.isInvalid()) {
            System.out.println("Ошибка: " + parseResult.getErrorMessage());
            return;
        }

        double amount = parseResult.getAmount();
        System.out.print("Описание (необязательно): ");
        String description = scanner.nextLine().trim();

        User user = authService.getCurrentUser();
        ValidationResult result = financeService.addExpense(user.getWallet(), category, amount, description);

        if (result.isValid()) {
            System.out.println("Расход добавлен успешно!");
            System.out.println("Текущий баланс: " + String.format("%.2f", user.getWallet().getBalance()) + " руб.");
        } else {
            System.out.println("Ошибка: " + result.getMessage());
        }
    }

    private void handleSetBudget() {
        System.out.println("\n>>> Установка/изменение бюджета");
        System.out.print("Категория расходов: ");
        String category = scanner.nextLine().trim();

        if (category.isEmpty()) {
            System.out.println(EMPTY_VALUE_ERROR);
            return;
        }

        System.out.print("Лимит бюджета: ");
        String limitStr = scanner.nextLine().trim();

        MoneyValidator.MoneyParseResult parseResult = MoneyValidator.parseAmount(limitStr);
        if (parseResult.isInvalid()) {
            System.out.println("Ошибка: " + parseResult.getErrorMessage());
            return;
        }

        double limit = parseResult.getAmount();
        User user = authService.getCurrentUser();
        ValidationResult result = financeService.setBudget(user.getWallet(), category, limit);

        if (result.isValid()) {
            System.out.println("Бюджет установлен успешно!");
        } else {
            System.out.println("Ошибка: " + result.getMessage());
        }
    }

    private void handleViewStatistics() {
        System.out.println("\n--- Статистика ---");
        User user = authService.getCurrentUser();
        Wallet wallet = user.getWallet();

        System.out.println("\nОбщий доход: " + String.format("%.2f", wallet.getTotalIncome()) + " руб.");

        Map<String, Double> incomeByCategory = wallet.getIncomeByCategory();
        if (!incomeByCategory.isEmpty()) {
            System.out.println("\nДоходы по категориям:");
            for (Map.Entry<String, Double> entry : incomeByCategory.entrySet()) {
                System.out.println("  " + entry.getKey() + ": " + String.format("%.2f", entry.getValue()) + " руб.");
            }
        }

        System.out.println("\nОбщие расходы: " + String.format("%.2f", wallet.getTotalExpense()) + " руб.");

        Map<String, Double> expenseByCategory = wallet.getExpenseByCategory();
        if (!expenseByCategory.isEmpty()) {
            System.out.println("\nРасходы по категориям:");
            for (Map.Entry<String, Double> entry : expenseByCategory.entrySet()) {
                System.out.println("  " + entry.getKey() + ": " + String.format("%.2f", entry.getValue()) + " руб.");
            }
        }

        System.out.println("\nТекущий баланс: " + String.format("%.2f", wallet.getBalance()) + " руб.");

        System.out.print("\nПосчитать сумму по нескольким категориям? (y/n): ");
        String choice = scanner.nextLine().trim().toLowerCase();

        if (choice.equals("y") || choice.equals("yes") || choice.equals("д") || choice.equals("да")) {
            handleCategorySum();
        }
    }

    private void handleCategorySum() {
        System.out.println("\nВведите названия категорий через запятую:");
        String input = scanner.nextLine().trim();

        if (input.isEmpty()) {
            System.out.println(EMPTY_VALUE_ERROR);
            return;
        }

        List<String> categoryNames = Arrays.asList(input.split(","));
        categoryNames = categoryNames.stream()
                .map(String::trim)
                .filter(s -> !s.isEmpty())
                .collect(java.util.stream.Collectors.toList());

        User user = authService.getCurrentUser();
        double total = financeService.getTotalByCategories(user.getWallet(), categoryNames);

        System.out.println("\nОбщая сумма по выбранным категориям: " + String.format("%.2f", total) + " руб.");
    }

    private void handleViewBudgets() {
        System.out.println("\n--- Бюджеты по категориям ---");
        User user = authService.getCurrentUser();
        List<Budget> budgets = financeService.getAllBudgets(user.getWallet());

        if (budgets.isEmpty()) {
            System.out.println("Бюджеты не установлены");
            return;
        }

        for (Budget budget : budgets) {
            System.out.println("\n" + budget.getCategory().getName() + ":");
            System.out.println("  Лимит: " + String.format("%.2f", budget.getLimit()) + " руб.");
            System.out.println("  Потрачено: " + String.format("%.2f", budget.getSpent()) + " руб.");
            System.out.println("  Осталось: " + String.format("%.2f", budget.getRemaining()) + " руб.");

            if (budget.isExceeded()) {
                System.out.println("  СТАТУС: ПРЕВЫШЕН (на " +
                        String.format("%.2f", budget.getSpent() - budget.getLimit()) + " руб.)");
            } else {
                System.out.println("  Использовано: " + String.format("%.1f%%", budget.getUsagePercentage()));
            }
        }
    }

    private void handleViewRecentTransactions() {
        System.out.println("\n--- Последние транзакции ---");
        System.out.print("Сколько последних транзакций показать? (по умолчанию 25): ");
        String input = scanner.nextLine().trim();

        int limit = 25;
        if (!input.isEmpty()) {
            try {
                limit = Integer.parseInt(input);
            } catch (NumberFormatException e) {
                System.out.println("Используется значение по умолчанию: 25");
            }
        }

        User user = authService.getCurrentUser();
        List<Transaction> transactions = financeService.getRecentTransactions(user.getWallet(), limit);

        if (transactions.isEmpty()) {
            System.out.println("Транзакций нет");
            return;
        }

        SimpleDateFormat dateFormat = new SimpleDateFormat("dd.MM.yyyy");
        for (Transaction transaction : transactions) {
            String date = dateFormat.format(Date.from(transaction.getCreatedAt()));
            String type = transaction.getCategory().getType().getDisplayName();
            System.out.println(String.format("\n%s - %s - %s",
                    date, type, transaction.getCategory().getName()));
            System.out.println(String.format("  Сумма: %.2f руб.", transaction.getAmount()));
            if (!transaction.getDescription().isEmpty()) {
                System.out.println("  Описание: " + transaction.getDescription());
            }
        }
    }

    private void handleTransfer() {
        System.out.println("\n>>> Перевод средств");
        System.out.print("Логин получателя: ");
        String recipientLogin = scanner.nextLine().trim();

        if (recipientLogin.isEmpty()) {
            System.out.println(EMPTY_VALUE_ERROR);
            return;
        }

        User recipient = authService.getUserByLogin(recipientLogin);
        if (recipient == null) {
            System.out.println("Пользователь с логином '" + recipientLogin + "' не найден");
            return;
        }

        User sender = authService.getCurrentUser();
        if (sender.getLogin().equalsIgnoreCase(recipientLogin)) {
            System.out.println("Нельзя переводить средства самому себе");
            return;
        }

        System.out.print("Сумма перевода: ");
        String amountStr = scanner.nextLine().trim();

        MoneyValidator.MoneyParseResult parseResult = MoneyValidator.parseAmount(amountStr);
        if (parseResult.isInvalid()) {
            System.out.println("Ошибка: " + parseResult.getErrorMessage());
            return;
        }

        double amount = parseResult.getAmount();

        if (sender.getWallet().getBalance() < amount) {
            System.out.println("Недостаточно средств. Ваш баланс: " +
                    String.format("%.2f", sender.getWallet().getBalance()) + " руб.");
            return;
        }

        System.out.print("Описание (необязательно): ");
        String description = scanner.nextLine().trim();

        boolean success = financeService.transferBetweenWallets(
                sender.getWallet(),
                recipient.getWallet(),
                amount,
                description
        );

        if (success) {
            System.out.println("Перевод выполнен успешно!");
            System.out.println("Ваш новый баланс: " +
                    String.format("%.2f", sender.getWallet().getBalance()) + " руб.");
        } else {
            System.out.println("Ошибка при выполнении перевода");
        }
    }

    private void handleSwitchUser() {
        System.out.println("\n>>> Смена пользователя");
        saveAllData();
        authService.logout();
        performAuthentication();
    }

    private void handleExit() {
        System.out.println("\n>>> Выход из приложения");
        saveAllData();
        System.out.println("Данные сохранены. До свидания!");
    }

    private void saveAllData() {
        storageService.saveUsers(authService.getAllUsers());

        for (User user : authService.getAllUsers().values()) {
            storageService.saveWallet(user.getWallet(), financeService.getCategoriesMap());
        }
    }

    private void showAlerts() {
        User user = authService.getCurrentUser();
        List<String> alerts = AlertService.checkWalletAlerts(user.getWallet());

        if (!alerts.isEmpty()) {
            System.out.println("\n" + "-".repeat(60));
            for (String alert : alerts) {
                System.out.println(AlertService.formatAlert(alert));
            }
            System.out.println("-".repeat(60));
        }
    }

    private void handleManageCategories() {
        System.out.println("\n>>> Управление категориями");
        System.out.println("\n1) Просмотреть все категории");
        System.out.println("2) Переименовать категорию");
        System.out.println("3) Удалить категорию");
        System.out.println("0) Назад в главное меню");
        System.out.print("Ваш выбор: ");

        String choice = scanner.nextLine().trim();

        switch (choice) {
            case "1":
                handleViewCategories();
                break;
            case "2":
                handleRenameCategory();
                break;
            case "3":
                handleDeleteCategory();
                break;
            case "0":
                return;
            default:
                System.out.println("\n" + INVALID_INPUT);
        }
    }

    private void handleViewCategories() {
        System.out.println("\n>>> Список всех категорий");

        Wallet wallet = authService.getCurrentUser().getWallet();

        // Получаем только категории, используемые текущим пользователем
        Set<Category> userCategories = new HashSet<>();
        for (Transaction transaction : wallet.getTransactions()) {
            userCategories.add(transaction.getCategory());
        }

        // Добавляем категории из бюджетов (могут быть без транзакций)
        for (Budget budget : wallet.getAllBudgets().values()) {
            userCategories.add(budget.getCategory());
        }

        if (userCategories.isEmpty()) {
            System.out.println("У вас пока нет категорий. Они создаются автоматически при добавлении транзакций.");
            return;
        }

        // Группируем по имени и типу
        Map<String, List<Category>> groupedByName = new HashMap<>();
        for (Category category : userCategories) {
            groupedByName.computeIfAbsent(category.getName(), k -> new ArrayList<>()).add(category);
        }

        for (Map.Entry<String, List<Category>> entry : groupedByName.entrySet()) {
            String categoryName = entry.getKey();
            List<Category> cats = entry.getValue();

            System.out.println("\n" + categoryName + ":");
            for (Category cat : cats) {
                String typeStr = cat.getType() == Category.TransactionType.INCOME ? "Доход" : "Расход";
                System.out.println("  - Тип: " + typeStr);

                if (cat.getType() == Category.TransactionType.EXPENSE) {
                    Budget budget = financeService.getBudget(wallet, categoryName);
                    if (budget != null) {
                        System.out.println("    Бюджет: " + String.format("%.2f", budget.getLimit()) + " руб.");
                    }
                }

                double total = cat.getType() == Category.TransactionType.INCOME
                        ? wallet.getIncomesByCategory(cat)
                        : wallet.getExpensesByCategory(cat);
                System.out.println("    Всего транзакций: " + String.format("%.2f", total) + " руб.");
            }
        }
    }

    private void handleRenameCategory() {
        System.out.println("\n>>> Переименование категории");

        System.out.print("Введите текущее название категории: ");
        String oldName = scanner.nextLine().trim();

        if (oldName.isEmpty()) {
            System.out.println(EMPTY_VALUE_ERROR);
            return;
        }

        System.out.print("Тип категории (1 - доход, 2 - расход): ");
        String typeChoice = scanner.nextLine().trim();

        Category.TransactionType type;
        if (typeChoice.equals("1")) {
            type = Category.TransactionType.INCOME;
        } else if (typeChoice.equals("2")) {
            type = Category.TransactionType.EXPENSE;
        } else {
            System.out.println(INVALID_INPUT);
            return;
        }

        Category category = financeService.getCategoryByName(oldName, type);
        if (category == null) {
            System.out.println("Ошибка: категория '" + oldName + "' не найдена.");
            return;
        }

        System.out.print("Введите новое название категории: ");
        String newName = scanner.nextLine().trim();

        if (newName.isEmpty()) {
            System.out.println(EMPTY_VALUE_ERROR);
            return;
        }

        ValidationResult result = financeService.renameCategory(
                authService.getCurrentUser().getWallet(),
                oldName,
                newName,
                type
        );

        if (result.isValid()) {
            System.out.println("Категория успешно переименована!");
        } else {
            System.out.println("Ошибка: " + result.getMessage());
        }
    }

    private void handleDeleteCategory() {
        System.out.println("\n>>> Удаление категории");

        System.out.print("Введите название категории для удаления: ");
        String categoryName = scanner.nextLine().trim();

        if (categoryName.isEmpty()) {
            System.out.println(EMPTY_VALUE_ERROR);
            return;
        }

        System.out.print("Тип категории (1 - доход, 2 - расход): ");
        String typeChoice = scanner.nextLine().trim();

        Category.TransactionType type;
        if (typeChoice.equals("1")) {
            type = Category.TransactionType.INCOME;
        } else if (typeChoice.equals("2")) {
            type = Category.TransactionType.EXPENSE;
        } else {
            System.out.println(INVALID_INPUT);
            return;
        }

        Category category = financeService.getCategoryByName(categoryName, type);
        if (category == null) {
            System.out.println("Ошибка: категория '" + categoryName + "' не найдена.");
            return;
        }

        System.out.print("Вы уверены, что хотите удалить категорию '" + categoryName + "'? (y/n): ");
        String confirmation = scanner.nextLine().trim().toLowerCase();

        if (!confirmation.equals("y") && !confirmation.equals("yes") && !confirmation.equals("д") && !confirmation.equals("да")) {
            System.out.println("Удаление отменено.");
            return;
        }

        ValidationResult result = financeService.deleteCategory(
                authService.getCurrentUser().getWallet(),
                categoryName,
                type
        );

        if (result.isValid()) {
            System.out.println("Категория успешно удалена!");
        } else {
            System.out.println("Ошибка: " + result.getMessage());
        }
    }
}
