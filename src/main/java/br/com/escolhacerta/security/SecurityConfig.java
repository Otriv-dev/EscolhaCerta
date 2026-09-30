package br.com.escolhacerta.security;

import br.com.escolhacerta.repository.AdminUserRepository;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.security.web.session.HttpSessionEventPublisher;

@Configuration
public class SecurityConfig {
    @Bean
    org.springframework.security.core.session.SessionRegistry sessionRegistry() {
        return new org.springframework.security.core.session.SessionRegistryImpl();
    }

    @Bean
    HttpSessionEventPublisher sessionEvents() {
        return new HttpSessionEventPublisher();
    }

    @Bean
    PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder(12);
    }

    @Bean
    UserDetailsService users(AdminUserRepository repo) {
        return username -> repo.findByEmail(username.strip().toLowerCase(java.util.Locale.ROOT))
            .map(user -> User.withUsername(user.getEmail())
                .password(user.getPasswordHash()).roles("ADMIN").build())
            .orElseThrow(() -> new UsernameNotFoundException("Credenciais inválidas"));
    }

    @Bean
    FilterRegistrationBean<RateLimitFilter> rateRegistration(RateLimitFilter filter) {
        // Register only in the security chain so login submissions are also limited.
        var registration = new FilterRegistrationBean<>(filter);
        registration.setEnabled(false);
        return registration;
    }

    @Bean
    SecurityFilterChain filter(HttpSecurity http, RateLimitFilter rateLimit, org.springframework.security.core.session.SessionRegistry registry, br.com.escolhacerta.service.AuditService audit) throws Exception {
        var success = new org.springframework.security.web.authentication.SavedRequestAwareAuthenticationSuccessHandler();
        success.setDefaultTargetUrl("/admin");
        success.setAlwaysUseDefaultTargetUrl(true);
        var failure = new org.springframework.security.web.authentication.SimpleUrlAuthenticationFailureHandler("/login?error");
        return http
            .addFilterBefore(rateLimit, UsernamePasswordAuthenticationFilter.class)
            .authorizeHttpRequests(auth -> auth
                .requestMatchers("/admin/**").hasRole("ADMIN")
                .requestMatchers("/actuator/health", "/actuator/health/**").permitAll()
                .requestMatchers("/actuator/**").denyAll()
                .anyRequest().permitAll())
            .formLogin(login -> login
                .loginPage("/login")
                .successHandler((request, response, authentication) -> {
                    audit.authentication("LOGIN_SUCCESS", authentication.getName());
                    success.onAuthenticationSuccess(request, response, authentication);
                })
                .failureHandler((request, response, exception) -> {
                    audit.authentication("LOGIN_FAILURE", request.getParameter("username"));
                    failure.onAuthenticationFailure(request, response, exception);
                })
                .permitAll())
            .logout(logout -> logout.deleteCookies("JSESSIONID").logoutSuccessUrl("/login?logout"))
            .sessionManagement(session -> session
                .sessionFixation(fixation -> fixation.migrateSession())
                .maximumSessions(1).sessionRegistry(registry))
            .headers(headers -> headers
                .referrerPolicy(policy -> policy.policy(org.springframework.security.web.header.writers.ReferrerPolicyHeaderWriter.ReferrerPolicy.NO_REFERRER))
                .contentSecurityPolicy(csp -> csp.policyDirectives(
                "default-src 'self'; img-src 'self' https:; style-src 'self'; "
                + "script-src 'self'; object-src 'none'; frame-ancestors 'none'; "
                + "base-uri 'self'; form-action 'self'")))
            .build();
    }
}
