package br.com.escolhacerta.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

/** Limits are per instance. Apply distributed limits at the gateway when scaling. */
@Component
public class RateLimitFilter extends OncePerRequestFilter {
    private record Window(long start, int count) {}
    private final Map<String, Window> windows = new HashMap<>();
    private long lastCleanup;
    private final AccountLoginThrottle accounts;
    public RateLimitFilter(AccountLoginThrottle accounts) { this.accounts = accounts; }

    private synchronized boolean allowed(String key, int limit) {
        long now = System.currentTimeMillis();
        if (now - lastCleanup >= 60000) {
            windows.entrySet().removeIf(e -> now - e.getValue().start() >= 60000);
            lastCleanup = now;
        }
        Window previous = windows.get(key);
        if (previous == null && windows.size() >= 10000) return false;
        Window next = previous == null || now - previous.start() >= 60000
            ? new Window(now, 1) : new Window(previous.start(), previous.count() + 1);
        windows.put(key, next);
        return next.count() <= limit;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest req, HttpServletResponse res,
                                  FilterChain chain) throws ServletException, IOException {
        String path = req.getRequestURI().substring(req.getContextPath().length());
        String category = null;
        int limit = 10;
        if ("POST".equals(req.getMethod())) {
            if (path.equals("/login")) category = "login";
            else if (path.equals("/orcamento") || path.equals("/sou-cuidador")
                     || path.equals("/contato") || path.startsWith("/formularios/")) category = "public-forms";
            else if (path.equals("/admin/account")) category = "account";
            else if (path.equals("/admin/media")) { category = "upload"; limit = 5; }
        }
        if (("GET".equals(req.getMethod()) || "HEAD".equals(req.getMethod())) && !path.startsWith("/actuator/")
                && !path.startsWith("/css/") && !path.startsWith("/js/")
                && !path.startsWith("/images/") && !path.startsWith("/media/")) {
            category = "page-reads";
            limit = 120;
        }
        if (category != null && !allowed(req.getRemoteAddr() + ":" + category, limit)) {
            res.setHeader("Retry-After", "60");
            res.sendError(429);
            return;
        }
        if ("POST".equals(req.getMethod()) && path.equals("/login")) {
            try {
                if (!accounts.allowed(req.getParameter("username"))) {
                    res.setHeader("Retry-After", "300");
                    res.sendError(429);
                    return;
                }
            } catch (org.springframework.dao.DataAccessException unavailable) {
                // Não permite autenticação sem o controle compartilhado disponível.
                res.setHeader("Retry-After", "60");
                res.sendError(503);
                return;
            }
        }
        chain.doFilter(req, res);
    }
}
