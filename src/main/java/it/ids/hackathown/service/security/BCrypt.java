package it.ids.hackathown.service.security;

public final class BCrypt {

    private BCrypt() {
    }

    public static String hash(String password) {
        if (password == null) {
            throw new IllegalArgumentException("Password cannot be null");
        }
        return org.springframework.security.crypto.bcrypt.BCrypt.hashpw(
            password,
            org.springframework.security.crypto.bcrypt.BCrypt.gensalt()
        );
    }

    public static boolean check(String password, String hash) {
        if (password == null || hash == null || hash.isBlank()) {
            return false;
        }
        return org.springframework.security.crypto.bcrypt.BCrypt.checkpw(password, hash);
    }
}
