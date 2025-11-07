package finance.services;

import finance.models.User;
import finance.utils.PasswordHasher;
import finance.utils.ValidationResult;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class AuthService {
    private final Map<String, User> users;
    private User currentUser;

    public AuthService() {
        this.users = new ConcurrentHashMap<>();
        this.currentUser = null;
    }

    public ValidationResult register(String login, String password) {
        if (login == null || login.trim().isEmpty()) {
            return ValidationResult.invalid("Логин не может быть пустым");
        }

        if (password == null || password.isEmpty()) {
            return ValidationResult.invalid("Пароль не может быть пустым");
        }

        ValidationResult passwordValidation = validatePassword(password);
        if (passwordValidation.isInvalid()) {
            return passwordValidation;
        }

        String normalizedLogin = login.trim().toLowerCase();

        if (users.containsKey(normalizedLogin)) {
            return ValidationResult.invalid("Пользователь с таким логином уже существует");
        }

        String passwordHash = PasswordHasher.hash(password);
        User newUser = new User(normalizedLogin, passwordHash);
        users.put(normalizedLogin, newUser);
        currentUser = newUser;

        return ValidationResult.valid();
    }

    private ValidationResult validatePassword(String password) {
        if (password.length() < 5) {
            return ValidationResult.invalid("Пароль должен содержать минимум 5 символов");
        }

        if (!password.matches("^[a-zA-Z0-9!@#$%^&*()_+\\-=\\[\\]{};':\"\\\\|,.<>/?]+$")) {
            return ValidationResult.invalid("Пароль может содержать только английские буквы, цифры и символы");
        }

        boolean hasUpperCase = false;
        boolean hasLowerCase = false;
        boolean hasDigit = false;
        boolean hasSpecialChar = false;

        for (char c : password.toCharArray()) {
            if (Character.isUpperCase(c)) {
                hasUpperCase = true;
            } else if (Character.isLowerCase(c)) {
                hasLowerCase = true;
            } else if (Character.isDigit(c)) {
                hasDigit = true;
            } else {
                hasSpecialChar = true;
            }
        }

        if (!hasUpperCase) {
            return ValidationResult.invalid("Пароль должен содержать минимум 1 заглавную букву");
        }

        if (!hasLowerCase) {
            return ValidationResult.invalid("Пароль должен содержать минимум 1 строчную букву");
        }

        if (!hasDigit) {
            return ValidationResult.invalid("Пароль должен содержать минимум 1 цифру");
        }

        if (!hasSpecialChar) {
            return ValidationResult.invalid("Пароль должен содержать минимум 1 специальный символ");
        }

        return ValidationResult.valid();
    }

    public ValidationResult login(String login, String password) {
        if (login == null || login.trim().isEmpty()) {
            return ValidationResult.invalid("Логин не может быть пустым");
        }

        if (password == null || password.isEmpty()) {
            return ValidationResult.invalid("Пароль не может быть пустым");
        }

        String normalizedLogin = login.trim().toLowerCase();
        User user = users.get(normalizedLogin);

        if (user == null) {
            return ValidationResult.invalid("Пользователь не найден");
        }

        String passwordHash = PasswordHasher.hash(password);
        if (!user.verifyPassword(passwordHash)) {
            return ValidationResult.invalid("Неверный пароль");
        }

        currentUser = user;
        return ValidationResult.valid();
    }

    public void logout() {
        currentUser = null;
    }

    public boolean isAuthenticated() {
        return currentUser != null;
    }

    public User getCurrentUser() {
        return currentUser;
    }

    public boolean userExists(String login) {
        if (login == null || login.trim().isEmpty()) {
            return false;
        }
        String normalizedLogin = login.trim().toLowerCase();
        return users.containsKey(normalizedLogin);
    }

    public User getUserByLogin(String login) {
        if (login == null || login.trim().isEmpty()) {
            return null;
        }
        String normalizedLogin = login.trim().toLowerCase();
        return users.get(normalizedLogin);
    }

    public Map<String, User> getAllUsers() {
        return users;
    }
}
