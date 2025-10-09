package com.aula.organizze.model;

import com.aula.organizze.config.ConfigFirebase;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;

public class Movimentacao {

    private double valor;
    private String titulo;
    private String descricao;
    private String categoria;
    private String data;
    private String tipo;

    public Movimentacao() {
    }

    // Método para salvar a movimentação no Realtime Database
    public void salvar(){
        // Recupera id do usuário logado
        FirebaseAuth autenticacao = ConfigFirebase.getFirebaseAutenticacao();
        String idUsuario = autenticacao.getCurrentUser().getUid();

        // Formata a data para mês e ano
        String mesAno = mesAnoDataEscolhida( getData() );

        DatabaseReference refFirebase = ConfigFirebase.getFirebaseDatabase();
        refFirebase.child("movimentacoes")
                .child(idUsuario)
                .child(mesAno)
                .setValue(this);
    }

    public static String mesAnoDataEscolhida(String dataEscolhida){
        // separando a string dataEscolhida a cada "/"
        String retornoData[] = dataEscolhida.split("/");

        // pegando o mês e o ano
        String mes = retornoData[1];
        String ano = retornoData[2];

        // concatenando mês e ano
        String mesAno = mes + ano;
        return mesAno;
    }

    public double getValor() {
        return valor;
    }

    public void setValor(double valor) {
        this.valor = valor;
    }

    public String getTitulo() {
        return titulo;
    }

    public void setTitulo(String titulo) {
        this.titulo = titulo;
    }

    public String getDescricao() {
        return descricao;
    }

    public void setDescricao(String descricao) {
        this.descricao = descricao;
    }

    public String getCategoria() {
        return categoria;
    }

    public void setCategoria(String categoria) {
        this.categoria = categoria;
    }

    public String getData() {
        return data;
    }

    public void setData(String data) {
        this.data = data;
    }

    public String getTipo() {
        return tipo;
    }

    public void setTipo(String tipo) {
        this.tipo = tipo;
    }
}
