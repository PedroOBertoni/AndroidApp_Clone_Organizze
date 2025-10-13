package com.aula.organizze.model;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class Movimentacao {

    private String id;            // id único da movimentação (Firebase .push())
    private String data;          // ex: "10/08/2025"
    private String categoria;
    private String descricao;
    private Double valor;
    private String titulo;
    private String tipo;
    private String status;

    private Recorrencia recorrencia; // objeto que define se é fixa (mensal, diária, etc.)
    private Parcelas parcelas;       // objeto que define as parcelas (se houver)

    public Movimentacao() {
    }

    // 🔹 Método para salvar no Firebase
    public void salvar() {
        FirebaseAuth auth = FirebaseAuth.getInstance();
        String uid = auth.getCurrentUser().getUid();

        DatabaseReference firebaseRef = FirebaseDatabase.getInstance().getReference();
        DatabaseReference movimentacoesRef = firebaseRef.child("movimentacoes").child(uid);

        // Gera um id único para esta movimentação
        String idMov = movimentacoesRef.push().getKey();
        this.id = idMov;

        // Salva o objeto completo (sem separar por ano/mês)
        movimentacoesRef.child(idMov).setValue(this);
    }

    // Getters e Setters
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

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

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public Recorrencia getRecorrencia() { return recorrencia; }
    public void setRecorrencia(Recorrencia recorrencia) { this.recorrencia = recorrencia; }

    public Parcelas getParcelas() { return parcelas; }
    public void setParcelas(Parcelas parcelas) { this.parcelas = parcelas; }
}
