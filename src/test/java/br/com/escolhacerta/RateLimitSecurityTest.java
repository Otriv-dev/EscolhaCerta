package br.com.escolhacerta;
import br.com.escolhacerta.security.RateLimitFilter;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import static org.assertj.core.api.Assertions.assertThat;
class RateLimitSecurityTest {
    private RateLimitFilter filter() {
        var account = org.mockito.Mockito.mock(br.com.escolhacerta.security.AccountLoginThrottle.class);
        org.mockito.Mockito.when(account.allowed(org.mockito.ArgumentMatchers.any())).thenReturn(true);
        return new RateLimitFilter(account);
    }
    @Test void alternatingFormsCannotBypassTheSharedLimit() throws Exception {
        var filter = filter();
        for (int i=0; i<11; i++) {
            var request = new MockHttpServletRequest("POST", i%2==0?"/contato":"/formularios/"+i);
            request.setRemoteAddr("192.0.2.90");
            var response = new MockHttpServletResponse();
            filter.doFilter(request, response, (req,res)->{});
            assertThat(response.getStatus()).isEqualTo(i<10?200:429);
        }
    }
    @Test void independentClientsHaveSeparateBudgets() throws Exception {
        var filter = filter();
        for (int i=0; i<12; i++) {
            var request = new MockHttpServletRequest("POST", "/login");
            request.setRemoteAddr("192.0.2."+i);
            request.setParameter("username", "account@example.com");
            var response = new MockHttpServletResponse();
            filter.doFilter(request, response, (req,res)->{});
            assertThat(response.getStatus()).isEqualTo(200);
        }
    }
}
