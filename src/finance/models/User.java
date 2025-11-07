package finance.models;

import java.time.Instant;
import java.util.Objects;

public class User {
    private final String login;
    private final String passwordHash;
    private final Instant createdAt;
    private Wallet wallet;

    public User(String login, String passwordHash) {
        if (login == null || login.trim().isEmpty()) {
            throw new IllegalArgumentException("Логин не может быть пустым");
        }
        if (passwordHash == null || passwordHash.isEmpty()) {
            throw new IllegalArgumentException("Хеш пароля не может быть пустым");
        }
        this.login = login.trim();
        this.passwordHash = passwordHash;
        this.createdAt = Instant.now();
        this.wallet = new Wallet(login);
    }

    public User(String login, String passwordHash, Instant createdAt, Wallet wallet) {
        this.login = login;
        this.passwordHash = passwordHash;
        this.createdAt = createdAt;
        this.wallet = wallet;
    }

    public String getLogin() {
        return login;
    }

    public String getPasswordHash() {
        return passwordHash;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public Wallet getWallet() {
        return wallet;
    }

    public void setWallet(Wallet wallet) {
        if (wallet == null) {
            throw new IllegalArgumentException("Кошелек не может быть null");
        }
        this.wallet = wallet;
    }

    public boolean verifyPassword(String passwordHash) {
        return this.passwordHash.equals(passwordHash);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        User user = (User) o;
        return Objects.equals(login, user.login);
    }

    @Override
    public int hashCode() {
        return Objects.hash(login);
    }

    @Override
    public String toString() {
        return "User{" +
                "login='" + login + '\'' +
                ", createdAt=" + createdAt +
                ", balance=" + wallet.getBalance() +
                '}';
    }
}
