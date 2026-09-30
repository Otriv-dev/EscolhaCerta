package br.com.escolhacerta.config;

import br.com.escolhacerta.model.AdminUser;
import br.com.escolhacerta.repository.AdminUserRepository;
import org.springframework.stereotype.Component;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.ApplicationArguments;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.password.PasswordEncoder;

@Component public class AdminBootstrap implements ApplicationRunner {
    private final AdminUserRepository repo;
    private final PasswordEncoder encoder;
    private final String email,password;
    public AdminBootstrap(AdminUserRepository repo,PasswordEncoder encoder,@Value("${app.admin.email}") String email,@Value("${app.admin.password}") String password) {
        this.repo=repo;
        this.encoder=encoder;
        this.email=email;
        this.password=password;
    }
    public void run(ApplicationArguments args) {
        if(repo.count()==0) {
            if(!email.matches("[^@\\s]+@[^@\\s]+\\.[^@\\s]+")) throw new IllegalStateException("Configure ADMIN_EMAIL válido");
            br.com.escolhacerta.security.PasswordPolicy.validate(password);
            AdminUser user=new AdminUser();
            user.setEmail(email.toLowerCase(java.util.Locale.ROOT));
            user.setPasswordHash(encoder.encode(password));
            repo.save(user);
        }
    }
}
