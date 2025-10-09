package com.aula.organizze.config;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class ConfigFirebase {

    /* Estou instanciando os atributos estáticos para que eles possam ser acessados sem a necessidade de
        instanciar a classe, pois os atributos teram sempre o mesmo valor independente da quantidade de instâncias */
    private static FirebaseAuth autenticacao;
    private static DatabaseReference bancoFirebase;

    // Método estático que retorna a instância do FirebaseDatabase
    public static DatabaseReference getFirebaseDatabase(){
        if(bancoFirebase == null){
            bancoFirebase = FirebaseDatabase.getInstance().getReference();
        }
        return bancoFirebase;
    }

    // Método estático para retornar a instância do FirebaseAuth
    public static FirebaseAuth getFirebaseAutenticacao(){
        if(autenticacao == null){
            autenticacao = FirebaseAuth.getInstance();
        }

        return autenticacao;
    }
}
