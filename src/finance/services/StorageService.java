package finance.services;

import finance.models.*;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.time.Instant;
import java.util.*;

public class StorageService {
    private static final String DATA_DIR = "data";
    private static final String WALLET_FILE_SUFFIX = "_wallet.csv";
    private static final String USER_FILE = "users.csv";
    private static final String SEPARATOR = ";";

    public StorageService() {
        try {
            Files.createDirectories(Paths.get(DATA_DIR));
        } catch (IOException e) {
            System.err.println("Ошибка при создании директории данных: " + e.getMessage());
        }
    }

    public void saveUsers(Map<String, User> users) {
        String filePath = DATA_DIR + File.separator + USER_FILE;

        try (PrintWriter writer = new PrintWriter(new FileWriter(filePath))) {
            writer.println("login" + SEPARATOR + "passwordHash" + SEPARATOR + "createdAt");

            for (User user : users.values()) {
                writer.println(
                        user.getLogin() + SEPARATOR +
                                user.getPasswordHash() + SEPARATOR +
                                user.getCreatedAt().toEpochMilli()
                );
            }
        } catch (IOException e) {
            System.err.println("Ошибка при сохранении пользователей: " + e.getMessage());
        }
    }

    public Map<String, User> loadUsers() {
        Map<String, User> users = new HashMap<>();
        String filePath = DATA_DIR + File.separator + USER_FILE;

        File file = new File(filePath);
        if (!file.exists()) {
            return users;
        }

        try (BufferedReader reader = new BufferedReader(new FileReader(filePath))) {
            String line = reader.readLine();

            while ((line = reader.readLine()) != null) {
                String[] parts = line.split(SEPARATOR);
                if (parts.length >= 3) {
                    String login = parts[0];
                    String passwordHash = parts[1];
                    Instant createdAt = Instant.ofEpochMilli(Long.parseLong(parts[2]));

                    User user = new User(login, passwordHash, createdAt, new Wallet(login));
                    users.put(login.toLowerCase(), user);
                }
            }
        } catch (IOException e) {
            System.err.println("Ошибка при загрузке пользователей: " + e.getMessage());
        }

        return users;
    }

    public void saveWallet(Wallet wallet, Map<String, Category> categories) {
        String filePath = DATA_DIR + File.separator + wallet.getOwnerLogin() + WALLET_FILE_SUFFIX;

        try (PrintWriter writer = new PrintWriter(new FileWriter(filePath))) {
            writer.println("# BALANCE");
            writer.println(wallet.getBalance());

            writer.println("# BUDGETS");
            writer.println("category" + SEPARATOR + "type" + SEPARATOR + "limit" + SEPARATOR + "spent");
            for (Budget budget : wallet.getAllBudgets().values()) {
                writer.println(
                        budget.getCategory().getName() + SEPARATOR +
                                budget.getCategory().getType() + SEPARATOR +
                                budget.getLimit() + SEPARATOR +
                                budget.getSpent()
                );
            }

            writer.println("# TRANSACTIONS");
            writer.println("id" + SEPARATOR + "category" + SEPARATOR + "type" + SEPARATOR +
                    "amount" + SEPARATOR + "description" + SEPARATOR + "createdAt");
            for (Transaction transaction : wallet.getTransactions()) {
                writer.println(
                        transaction.getId() + SEPARATOR +
                                transaction.getCategory().getName() + SEPARATOR +
                                transaction.getCategory().getType() + SEPARATOR +
                                transaction.getAmount() + SEPARATOR +
                                escapeDescription(transaction.getDescription()) + SEPARATOR +
                                transaction.getCreatedAtMillis()
                );
            }
        } catch (IOException e) {
            System.err.println("Ошибка при сохранении кошелька: " + e.getMessage());
        }
    }

    public void loadWallet(User user, Map<String, Category> categories) {
        String filePath = DATA_DIR + File.separator + user.getLogin() + WALLET_FILE_SUFFIX;
        File file = new File(filePath);

        if (!file.exists()) {
            return;
        }

        try (BufferedReader reader = new BufferedReader(new FileReader(filePath))) {
            String line;
            String section = "";
            double balance = 0.0;
            List<Transaction> transactions = new ArrayList<>();
            Map<Category, Budget> budgets = new HashMap<>();

            while ((line = reader.readLine()) != null) {
                if (line.startsWith("# ")) {
                    section = line.substring(2).trim();
                    if (!section.equals("BALANCE")) {
                        reader.readLine();
                    }
                    continue;
                }

                if (line.trim().isEmpty()) {
                    continue;
                }

                switch (section) {
                    case "BALANCE":
                        balance = Double.parseDouble(line);
                        break;

                    case "BUDGETS":
                        String[] budgetParts = line.split(SEPARATOR);
                        if (budgetParts.length >= 4) {
                            String categoryName = budgetParts[0];
                            Category.TransactionType type = Category.TransactionType.valueOf(budgetParts[1]);
                            double limit = Double.parseDouble(budgetParts[2]);
                            double spent = Double.parseDouble(budgetParts[3]);

                            Category category = getOrCreateCategory(categories, categoryName, type);
                            Budget budget = new Budget(category, limit, spent);
                            budgets.put(category, budget);
                        }
                        break;

                    case "TRANSACTIONS":
                        String[] txParts = line.split(SEPARATOR);
                        if (txParts.length >= 6) {
                            String id = txParts[0];
                            String categoryName = txParts[1];
                            Category.TransactionType type = Category.TransactionType.valueOf(txParts[2]);
                            double amount = Double.parseDouble(txParts[3]);
                            String description = unescapeDescription(txParts[4]);
                            Instant createdAt = Instant.ofEpochMilli(Long.parseLong(txParts[5]));

                            Category category = getOrCreateCategory(categories, categoryName, type);
                            Transaction transaction = new Transaction(id, category, amount, description, createdAt);
                            transactions.add(transaction);
                        }
                        break;

                    default:
                        break;
                }
            }

            Wallet wallet = new Wallet(user.getLogin(), balance, transactions, budgets);
            user.setWallet(wallet);

        } catch (IOException e) {
            System.err.println("Ошибка при загрузке кошелька: " + e.getMessage());
        }
    }

    private Category getOrCreateCategory(Map<String, Category> categories, String name, Category.TransactionType type) {
        String key = name.trim().toLowerCase();

        if (!categories.containsKey(key)) {
            Category category = new Category(name, type);
            categories.put(key, category);
            return category;
        }

        return categories.get(key);
    }

    private String escapeDescription(String description) {
        if (description == null) {
            return "";
        }
        return description.replace(SEPARATOR, "\\;").replace("\n", "\\n");
    }

    private String unescapeDescription(String description) {
        if (description == null) {
            return "";
        }
        return description.replace("\\;", SEPARATOR).replace("\\n", "\n");
    }
}
