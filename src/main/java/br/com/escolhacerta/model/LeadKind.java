package br.com.escolhacerta.model;

public enum LeadKind {
    QUOTE("Orçamento"),
    CAREGIVER("Candidatura"),
    CONTACT("Contato"),
    CUSTOM("Formulário personalizado");

    private final String label;

    LeadKind(String label) { this.label = label; }

    public String getLabel() { return label; }
}
