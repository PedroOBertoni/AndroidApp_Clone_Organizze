package com.aula.organizze.model;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.HashMap;
import java.util.Map;

public class Movimentacao {

    private String data;
    private String categoria;
    private String descricao;
    private Double valor;
    private String titulo;
    private String tipo; // "R" ou "D"

    private Integer quantParcelas; // null = não parcelado
    private String frequencia;     // null = não fixo

    public Movimentacao() {
    }

    // Método salvar atualizado
    public void salvar(String mesAno) {
        FirebaseAuth auth = FirebaseAuth.getInstance();
        String uid = auth.getCurrentUser().getUid();

        DatabaseReference firebase = FirebaseDatabase.getInstance().getReference();
        firebase.child("movimentacoes")
                .child(uid)
                .child(mesAno)
                .push()
                .setValue(this);
    }

    // Getters e Setters
    public String getData() { return data; }
    public void setData(String data) { this.data = data; }

    public String getCategoria() { return categoria; }
    public void setCategoria(String categoria) { this.categoria = categoria; }

    public String getDescricao() { return descricao; }
    public void setDescricao(String descricao) { this.descricao = descricao; }

    public Double getValor() { return valor; }
    public void setValor(Double valor) { this.valor = valor; }

    public String getTitulo() { return titulo; }
    public void setTitulo(String titulo) { this.titulo = titulo; }

    public String getTipo() { return tipo; }
    public void setTipo(String tipo) { this.tipo = tipo; }

    // Novos getters/setters
    public Integer getQuantParcelas() { return quantParcelas; }
    public void setQuantParcelas(Integer quantParcelas) { this.quantParcelas = quantParcelas; }

    public String getFrequencia() { return frequencia; }
    public void setFrequencia(String frequencia) { this.frequencia = frequencia; }
}