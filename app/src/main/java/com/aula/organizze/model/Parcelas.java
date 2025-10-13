package com.aula.organizze.model;

public class Parcelas {
    private int total;   // Total de parcelas
    private int atual;   // Parcela atual (1, 2, 3...)
    private String fim;  // Data da última parcela (geralmente calculada)

    // Construtores
    public Parcelas() {
    }

    public Parcelas(int total, int atual, String fim) {
        this.total = total;
        this.atual = atual;
        this.fim = fim;
    }

    // Getters e Setters
    public int getTotal() { return total; }
    public void setTotal(int total) { this.total = total; }

    public int getAtual() { return atual; }
    public void setAtual(int atual) { this.atual = atual; }

    public String getFim() { return fim; }
    public void setFim(String fim) { this.fim = fim; }
}
