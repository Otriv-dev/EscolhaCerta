package br.com.escolhacerta.security;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.stereotype.Component;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.HexFormat;
import java.util.Locale;
import java.util.concurrent.atomic.AtomicLong;

/** Quota persistida e compartilhada. Janela temporária de 5 minutos; sem bloqueio permanente. */
@Component
public class AccountLoginThrottle {
    private final JdbcTemplate jdbc;
    private final AtomicLong lastCleanup = new AtomicLong();
    public AccountLoginThrottle(JdbcTemplate jdbc) { this.jdbc = jdbc; }
    public boolean allowed(String username) {
        if (username == null || username.isBlank() || username.length() > 180) return false;
        long now = System.currentTimeMillis(), cutoff = now - 300000;
        long previous = lastCleanup.get();
        if (now - previous > 60000 && lastCleanup.compareAndSet(previous, now)) {
            jdbc.update("DELETE FROM login_throttle WHERE started_at < ?", cutoff);
        }
        String key = key(username);
        try {
            jdbc.update("INSERT INTO login_throttle(bucket_key, started_at, attempts) VALUES (?, ?, 0)", key, now);
        } catch (DuplicateKeyException alreadyExists) { /* Compartilha o bucket existente. */ }
        jdbc.update("UPDATE login_throttle SET started_at = ?, attempts = 0 WHERE bucket_key = ? AND started_at < ?", now, key, cutoff);
        return jdbc.update("UPDATE login_throttle SET attempts = attempts + 1 WHERE bucket_key = ? AND attempts < 10", key) == 1;
    }
    static String key(String username) {
        try {
            return HexFormat.of().formatHex(MessageDigest.getInstance("SHA-256")
                .digest(username.strip().toLowerCase(Locale.ROOT).getBytes(StandardCharsets.UTF_8)));
        } catch (java.security.NoSuchAlgorithmException ex) { throw new IllegalStateException(ex); }
    }
}
