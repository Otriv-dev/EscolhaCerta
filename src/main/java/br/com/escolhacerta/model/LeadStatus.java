package br.com.escolhacerta.model;

public enum LeadStatus {
    NEW("Novo"),
    IN_PROGRESS("Em atendimento"),
    COMPLETED("Concluído"),
    ARCHIVED("Arquivado");

    private final String label;

    LeadStatus(String label) { this.label = label; }

    public String getLabel() { return label; }
}
