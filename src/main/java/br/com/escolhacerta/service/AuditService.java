package br.com.escolhacerta.service;

import br.com.escolhacerta.repository.AdminUserRepository;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;
import org.springframework.security.core.context.SecurityContextHolder;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import java.time.Instant;
import java.sql.Timestamp;
import java.util.UUID;
import java.util.Locale;
import java.util.HexFormat;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;

@Service
public class AuditService {
    private final JdbcTemplate jdbc;
    private final AdminUserRepository users;
    public AuditService(JdbcTemplate jdbc, AdminUserRepository users) { this.jdbc = jdbc; this.users = users; }
    @Transactional
    public void record(String action, String objectId) {
        var auth = SecurityContextHolder.getContext().getAuthentication();
        write(action, objectId, auth == null ? null : auth.getName());
    }
    @Transactional
    public void authentication(String action, String username) { write(action, "account", username); }
    private void write(String action, String objectId, String username) {
        String actor = "unidentified";
        if (username != null && !username.isBlank() && username.length() <= 180) {
            String normalized = username.strip().toLowerCase(Locale.ROOT);
            actor = users.findByEmail(normalized).map(u -> "user:" + u.getId()).orElseGet(() -> "principal:" + digest(normalized));
        }
        String correlation = MDC.get("request_id");
        if (correlation == null) correlation = UUID.randomUUID().toString();
        String id = UUID.randomUUID().toString();
        jdbc.update("INSERT INTO security_audit(id, occurred_at, actor, action, object_id, result, request_id) VALUES (?, ?, ?, ?, ?, ?, ?)",
            id, Timestamp.from(Instant.now()), actor, action, objectId,
            (action.equals("LOGIN_FAILURE") || action.endsWith("_DENIED")) ? "FAILURE" : "SUCCESS", correlation);
        final String actorValue = actor, requestValue = correlation;
        Runnable emit = () -> LoggerFactory.getLogger(AuditService.class).info(
            "security_audit event={} actor={} action={} object={} request_id={}", id, actorValue, action, objectId, requestValue);
        if (TransactionSynchronizationManager.isSynchronizationActive()) {
            TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
                @Override public void afterCommit() { emit.run(); }
            });
        } else emit.run();
    }
    private static String digest(String value) {
        try { return HexFormat.of().formatHex(MessageDigest.getInstance("SHA-256").digest(value.getBytes(StandardCharsets.UTF_8))); }
        catch (java.security.NoSuchAlgorithmException ex) { throw new IllegalStateException(ex); }
    }
}
