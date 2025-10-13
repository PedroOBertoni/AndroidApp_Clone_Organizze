package com.aula.organizze.model;

public class Recorrencia {
    private String tipo;  // Pode ser: "nenhum", "mensal", "diario", "anual"
    private String fim;   // "10/12/2025" (campo opcional) — data limite da recorrência

    public Recorrencia() {
    }

    // Getters e Setters
    public String getTipo() { return tipo; }
    public void setTipo(String tipo) { this.tipo = tipo; }

    public String getFim() { return fim; }
    public void setFim(String fim) { this.fim = fim; }
}
