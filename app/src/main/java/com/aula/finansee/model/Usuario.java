package com.aula.finansee.model;

import com.aula.finansee.config.ConfigFirebase;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.Exclude;

public class Usuario {
    // Atributos
    private String idUsuario;
    private String nome;
    private String email;

    // Construtor vazio necessário para o Firebase
    public Usuario() {
    }

    // Método para salvar o usuário no Realtime Database
    public void salvar() {
        // Recupera a referência do Firebase Database
        DatabaseReference refFirebase = ConfigFirebase.getFirebaseDatabase();

        // Salva o usuário na árvore "usuarios" com o ID do usuário como chave
        refFirebase.child("usuarios")
                .child(this.idUsuario)
                .setValue(this);
    }

    // Getters e Setters
    @Exclude // Estou excluindo esse atributo do firebase database, portanto não será salvo no firebase database
    public String getIdUsuario() {
        return idUsuario;
    }

    public void setIdUsuario(String idUsuario) {
        this.idUsuario = idUsuario;
    }

    public String getNome() {
        return nome;
    }

    public void setNome(String nome) {
        this.nome = nome;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }
}
