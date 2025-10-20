package com.aula.finansee.model;

public class Recorrencia {

    /* Atriutos */

    // "fixa", "parcelada", ou null
    private String tipo;

    // Usar Integer (wrapper) para aceitar null quando não aplicável
    private Integer parcelaAtual;    // ex: 1
    private Integer parcelasTotais;  // ex: 12

    // Data final (opcional) no formato "dd/MM/yyyy" — pode ser null
    private String fim;


    /* Construtores */

    // Construtor vazio necessário para o Firebase
    public Recorrencia() {
    }

    // Construtor conveniente (opcional)
    public Recorrencia(String tipo, Integer parcelaAtual, Integer parcelasTotais, String fim) {
        this.tipo = tipo;
        this.parcelaAtual = parcelaAtual;
        this.parcelasTotais = parcelasTotais;
        this.fim = fim;
    }


    // Getters e Setters
    public String getTipo() { return tipo; }
    public void setTipo(String tipo) { this.tipo = tipo; }

    public Integer getParcelaAtual() { return parcelaAtual; }
    public void setParcelaAtual(Integer parcelaAtual) { this.parcelaAtual = parcelaAtual; }

    public Integer getParcelasTotais() { return parcelasTotais; }
    public void setParcelasTotais(Integer parcelasTotais) { this.parcelasTotais = parcelasTotais; }

    public String getFim() { return fim; }
    public void setFim(String fim) { this.fim = fim; }
}
