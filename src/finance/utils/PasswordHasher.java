package finance.utils;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Base64;

public class PasswordHasher {
    private static final String ALGORITHM = "SHA-256";

    public static String hash(String password) {
        if (password == null) {
            throw new IllegalArgumentException("Пароль не может быть null");
        }

        try {
            MessageDigest digest = MessageDigest.getInstance(ALGORITHM);
            byte[] hashBytes = digest.digest(password.getBytes(StandardCharsets.UTF_8));
            return Base64.getEncoder().encodeToString(hashBytes);
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("Алгоритм SHA-256 недоступен", e);
        }
    }

    public static boolean verify(String password, String hash) {
        if (password == null || hash == null) {
            return false;
        }
        return hash(password).equals(hash);
    }
}
