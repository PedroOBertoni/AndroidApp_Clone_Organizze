package com.aula.organizze.model;

import com.aula.organizze.config.ConfigFirebase;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.Exclude;

public class Usuario {
    private String idUsuario;
    private String nome;
    private String email;

    public Usuario() {
    }

    // Método para salvar o usuário no Realtime Database
    public void salvar() {
        DatabaseReference ref = ConfigFirebase.getFirebaseDatabase();
        ref.child("usuarios")
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
