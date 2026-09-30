package br.com.escolhacerta.dto;

import jakarta.validation.constraints.*;

public record FieldDefinition(@NotBlank @Pattern(regexp="[a-z][a-z0-9_]{0,39}") String name, @NotBlank @Size(max=100) String label, @NotBlank @Pattern(regexp="text|textarea|date|number|select") String type, boolean required, java.util.List<@NotBlank @Size(max=100) String> options) {
}
