package br.com.escolhacerta.security;

import java.nio.charset.StandardCharsets;
import java.util.Locale;
import java.util.Set;

/** Política compartilhada; blocklist inicial offline, sem enviar senha a terceiros. */
public final class PasswordPolicy {
    private PasswordPolicy() {}
    private static final Set<String> COMMON = Set.of(
        "passwordpassword", "password123456789", "123456789012345", "12345678901234567890",
        "qwertyuiopasdfgh", "adminadminadmin", "administrador123", "senhasenhasenha123",
        "escolhacerta12345", "escolha certa 123", "iloveyouiloveyou", "letmeinletmein123");
    public static void validate(String password) {
        if (password == null || password.codePointCount(0, password.length()) < 15
                || password.getBytes(StandardCharsets.UTF_8).length > 72) {
            throw new IllegalArgumentException("Use uma senha com pelo menos 15 caracteres e no máximo 72 bytes. Uma frase longa é uma boa opção.");
        }
        String normalized = password.strip().toLowerCase(Locale.ROOT);
        if (COMMON.contains(normalized) || normalized.isEmpty()
                || normalized.codePoints().distinct().count() <= 1) {
            throw new IllegalArgumentException("Escolha uma senha menos previsível, evitando senhas comuns, repetidas ou sequências numéricas.");
        }
    }
}
