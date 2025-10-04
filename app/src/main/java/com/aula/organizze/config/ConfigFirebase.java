package com.aula.organizze.config;

import com.google.firebase.auth.FirebaseAuth;

public class ConfigFirebase {

    /* Estou instanciando o atributo estático para que ele possa ser acessado sem a necessidade de
        instanciar a classe, pois o atributo terá sempre o mesmo valor independente da quantidade de instâncias */
    private static FirebaseAuth autenticacao;

    // Método estático para retornar a instância do FirebaseAuth
    public static FirebaseAuth getFirebaseAutenticacao(){
        if(autenticacao == null){
            autenticacao = FirebaseAuth.getInstance();
        }

        return autenticacao;
    }
}
