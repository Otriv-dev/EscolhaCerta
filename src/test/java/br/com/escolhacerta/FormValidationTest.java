package br.com.escolhacerta;

import br.com.escolhacerta.service.FormService;
import br.com.escolhacerta.repository.CustomFormRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

class FormValidationTest {
    FormService service=new FormService(mock(CustomFormRepository.class),new ObjectMapper(),jakarta.validation.Validation.buildDefaultValidatorFactory().getValidator(),mock(br.com.escolhacerta.service.AuditService.class),mock(br.com.escolhacerta.service.PublicFormMenu.class));
    @Test void rejectsReservedNames() {
        assertThatThrownBy(()->service.fields("[{\"name\":\"_csrf\",\"label\":\"Pergunta\",\"type\":\"text\"}]")).isInstanceOf(IllegalArgumentException.class);
    }
    @Test void rejectsDuplicateNames() {
        assertThatThrownBy(()->service.fields("[{\"name\":\"teste\",\"label\":\"A\",\"type\":\"text\"},{\"name\":\"teste\",\"label\":\"B\",\"type\":\"text\"}]")).isInstanceOf(IllegalArgumentException.class);
    }
    @Test void rejectsInvalidType() {
        assertThatThrownBy(()->service.fields("[{\"name\":\"teste\",\"label\":\"A\",\"type\":\"password\"}]")).isInstanceOf(IllegalArgumentException.class);
    }
    @Test void acceptsValidFields() {
        assertThat(service.fields("[{\"name\":\"disponibilidade\",\"label\":\"Disponibilidade\",\"type\":\"text\",\"required\":true}]")).hasSize(1);
    }
}
