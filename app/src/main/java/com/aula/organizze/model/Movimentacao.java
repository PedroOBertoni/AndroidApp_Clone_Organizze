package com.aula.organizze.model;

import android.util.Log;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

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

    public Movimentacao() {
    }

    // 🔹 Método para salvar no Firebase
    public void salvar() {
        FirebaseAuth auth = FirebaseAuth.getInstance();
        String uid = auth.getCurrentUser().getUid();

        DatabaseReference firebaseRef = FirebaseDatabase.getInstance().getReference();
        DatabaseReference movimentacoesRef = firebaseRef.child("movimentacoes").child(uid);

        // Gera o nó do mês/ano a partir da data da movimentação
        try {
            SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
            Date dataObj = sdf.parse(this.data);
            SimpleDateFormat mesAnoFormat = new SimpleDateFormat("MM-yyyy", Locale.getDefault());
            String mesAno = mesAnoFormat.format(dataObj);

            DatabaseReference mesRef = movimentacoesRef.child(mesAno);

            // Gera um id único dentro do mês correspondente
            String idMov = mesRef.push().getKey();
            this.id = idMov;

            // Salva a movimentação
            mesRef.child(idMov).setValue(this);

        } catch (Exception e) {
            e.printStackTrace();
            Log.e("Movimentacao", "Erro ao salvar movimentação: data inválida (" + this.data + ")");
        }
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
}
