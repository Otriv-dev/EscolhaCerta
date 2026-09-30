package br.com.escolhacerta.service;

import br.com.escolhacerta.model.*;
import br.com.escolhacerta.dto.*;
import br.com.escolhacerta.repository.*;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.core.type.TypeReference;
import jakarta.validation.Validator;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service @Transactional public class FormService {
    private final AuditService audit;
    private final PublicFormMenu menu;
    private final CustomFormRepository repo;
    private final ObjectMapper json;
    private final Validator validator;
    public FormService(CustomFormRepository repo,ObjectMapper json,Validator validator,AuditService audit,PublicFormMenu menu) {
        this.repo=repo;
        this.json=json;
        this.validator=validator;
        this.audit=audit;
        this.menu=menu;
    }
    public CustomForm get(long id) {
        return repo.findById(id).orElseThrow(()->new org.springframework.web.server.ResponseStatusException(org.springframework.http.HttpStatus.NOT_FOUND));
    }
    public java.util.List<FieldDefinition> fields(String value) {
        try {
            java.util.List<FieldDefinition> fields=json.readValue(value,new TypeReference<java.util.List<FieldDefinition>>() {
            }
            );
            if(fields==null||fields.isEmpty()||fields.size()>20)throw new IllegalArgumentException();
            var names=new java.util.HashSet<String>();
            for(var f:fields) {
                if(f==null||!validator.validate(f).isEmpty()||!names.add(f.name())||java.util.Set.of("name","email","phone","city","message","consent","website","_csrf").contains(f.name()))throw new IllegalArgumentException();
                if(f.type().equals("select")&&(f.options()==null||f.options().isEmpty()||f.options().size()>30))throw new IllegalArgumentException();
            }
            return fields;
        }
        catch(Exception e) {
            throw new IllegalArgumentException("Campos inválidos. Use até 20 campos com nomes únicos e tipos text, textarea, date, number ou select.");
        }
    }
    public void save(Long id,CustomFormDto dto) {
        fields(dto.fieldsJson);
        CustomForm f=id==null?new CustomForm():get(id);
        f.setTitle(dto.title);
        f.setDescription(dto.description);
        f.setFieldsJson(dto.fieldsJson);
        f.setPublished(dto.published);
        CustomForm saved=repo.save(f);
        menu.invalidate();
        audit.record("FORM_SAVED", saved.getId().toString());
    }
    public String answers(CustomForm f,java.util.Map<String,String> params) {
        var answers=new java.util.LinkedHashMap<String,String>();
        for(var field:fields(f.getFieldsJson())) {
            String value=params.getOrDefault(field.name(),"").trim();
            if((field.required()&&value.isBlank())||value.length()>2000)throw new IllegalArgumentException("Preencha corretamente: "+field.label());
            if(!value.isBlank()) {
                try {
                    switch(field.type()) {
                        case "date" -> java.time.LocalDate.parse(value);
                        case "number" -> new java.math.BigDecimal(value);
                        case "select" -> {
                            if(!field.options().contains(value))throw new IllegalArgumentException();
                        }
                        default -> {
                        }
                    }
                }
                catch(Exception ex) {
                    throw new IllegalArgumentException("Valor inválido: "+field.label());
                }
            }
            answers.put(field.label(),value);
        }
        try {
            return json.writeValueAsString(answers);
        }
        catch(Exception ex) {
            throw new IllegalStateException(ex);
        }
    }
}
