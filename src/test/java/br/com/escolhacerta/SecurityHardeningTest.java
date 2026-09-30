package br.com.escolhacerta;

import br.com.escolhacerta.security.*;
import br.com.escolhacerta.service.*;
import br.com.escolhacerta.repository.*;
import br.com.escolhacerta.model.*;
import br.com.escolhacerta.dto.*;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import java.util.*;
import java.util.concurrent.*;
import static org.assertj.core.api.Assertions.*;

@SpringBootTest @ActiveProfiles("test")
class SecurityHardeningTest {
    @Autowired JdbcTemplate jdbc;
    @Autowired ContentService content;
    @Autowired ContentRepository contents;
    @Autowired AdminUserRepository users;
    @Autowired PublicFormMenu menu;
    @Autowired FormService forms;

    @Test void predictablePasswordsAreRejectedAndPassphrasesAccepted() {
        for (String weak : List.of("aaaaaaaaaaaa", "123456789012", "aaaaaaaaaaaaaaa", "123456789012345", "passwordpassword")) {
            assertThatThrownBy(() -> PasswordPolicy.validate(weak)).isInstanceOf(IllegalArgumentException.class);
        }
        assertThatCode(() -> PasswordPolicy.validate("Meu jardim recebe luz de manhã!")).doesNotThrowAnyException();
        assertThatThrownBy(() -> PasswordPolicy.validate("Á".repeat(37))).isInstanceOf(IllegalArgumentException.class);
    }

    @Test void accountQuotaIsSharedAcrossInstancesAndAtomicUnderConcurrency() throws Exception {
        String account = "quota-" + UUID.randomUUID() + "@example.com";
        var first = new AccountLoginThrottle(jdbc);
        var second = new AccountLoginThrottle(jdbc);
        ExecutorService pool = Executors.newFixedThreadPool(8);
        try {
            List<Callable<Boolean>> calls = new ArrayList<>();
            for (int i = 0; i < 40; i++) {
                final int index = i;
                calls.add(() -> (index % 2 == 0 ? first : second).allowed(index % 2 == 0 ? account : account.toUpperCase(Locale.ROOT)));
            }
            long admitted = 0;
            for (var future : pool.invokeAll(calls)) if (future.get()) admitted++;
            assertThat(admitted).isEqualTo(10);
            assertThat(new AccountLoginThrottle(jdbc).allowed(account)).isFalse();
            jdbc.update("UPDATE login_throttle SET started_at = ? WHERE bucket_key = ?", System.currentTimeMillis() - 301000,
                java.util.HexFormat.of().formatHex(java.security.MessageDigest.getInstance("SHA-256").digest(account.getBytes(java.nio.charset.StandardCharsets.UTF_8))));
            assertThat(first.allowed(account)).isTrue();
        } finally { pool.shutdownNow(); }
    }

    @Test void dynamicGetsAreLimitedAndHealthRemainsAvailable() throws Exception {
        var filter = new RateLimitFilter(new AccountLoginThrottle(jdbc));
        for (int i = 0; i < 121; i++) {
            var req = new MockHttpServletRequest("GET", "/blog");req.setRemoteAddr("192.0.2.123");
            var res = new MockHttpServletResponse();filter.doFilter(req, res, (a,b) -> {});
            assertThat(res.getStatus()).isEqualTo(i < 120 ? 200 : 429);
        }
        var req = new MockHttpServletRequest("GET", "/actuator/health");req.setRemoteAddr("192.0.2.123");
        var res = new MockHttpServletResponse();filter.doFilter(req, res, (a,b) -> {});
        assertThat(res.getStatus()).isEqualTo(200);
    }

    @Test @WithMockUser(username="admin@test.local", roles="ADMIN")
    void pagingAndAuditArePersisted() {
        String marker = "paging-" + UUID.randomUUID();
        for (int i = 0; i < 28; i++) {
            ContentForm f = new ContentForm();f.title = marker + i;f.published = true;f.sortOrder = 0;
            content.save(ContentKind.POST, null, f);
        }
        assertThat(content.published(ContentKind.POST)).hasSize(3);
        var first = content.publicPage(ContentKind.POST, 0);
        var second = content.publicPage(ContentKind.POST, 1);
        assertThat(first.getContent()).hasSize(12);
        assertThat(first.hasNext()).isTrue();
        var firstIds = first.getContent().stream().map(PublicContent::getId).toList();
        assertThat(second.getContent().stream().map(PublicContent::getId).toList()).doesNotContainAnyElementsOf(firstIds);
        String actor = "user:" + users.findByEmail("admin@test.local").orElseThrow().getId();
        Integer count = jdbc.queryForObject("SELECT COUNT(*) FROM security_audit WHERE action = 'CONTENT_SAVED' AND actor = ?", Integer.class, actor);
        assertThat(count).isGreaterThanOrEqualTo(28);
        contents.findAll().stream().filter(c -> c.getTitle().startsWith(marker)).forEach(contents::delete);
    }

    @Test @WithMockUser(username="admin@test.local", roles="ADMIN")
    void cachedMenuIsRefreshedAfterPublishing() {
        menu.links();
        CustomFormDto f = new CustomFormDto();f.title = "Menu " + UUID.randomUUID();f.published = true;
        f.fieldsJson = "[{\"name\":\"horario\",\"label\":\"Horário\",\"type\":\"text\",\"required\":false}]";
        forms.save(null, f);
        assertThat(menu.links().stream().map(PublicFormLink::getTitle).toList()).contains(f.title);
    }
}
