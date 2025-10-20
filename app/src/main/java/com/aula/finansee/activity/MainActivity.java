package com.aula.finansee.activity;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;

import com.aula.finansee.config.ConfigFirebase;
import com.google.firebase.auth.FirebaseAuth;
import com.heinrichreimersoftware.materialintro.app.IntroActivity;
import com.aula.finansee.R;
import com.heinrichreimersoftware.materialintro.slide.FragmentSlide;
import com.jakewharton.threetenabp.AndroidThreeTen;

public class MainActivity extends IntroActivity {

    // Variavel de intancia da autenticação do Firebase
    private FirebaseAuth autenticacao;

    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        setTheme(R.style.Theme_Finansee); // força o tema base
        super.onCreate(savedInstanceState);

        // Inicializa a biblioteca de data/hora
        AndroidThreeTen.init(this);

        // esconde botões padrão
        setButtonBackVisible(false);
        setButtonNextVisible(false);

        // Adiciona o slide 1
        addSlide(new FragmentSlide.Builder()
                .background(R.color.headerBackground)
                .fragment(R.layout.intro_1)
                .build());

        // Adiciona o slide 2
        addSlide(new FragmentSlide.Builder()
                .background(R.color.headerBackground)
                .fragment(R.layout.intro_2)
                .build());

        // E por fim adiciona o slide 3
        addSlide(new FragmentSlide.Builder()
                .background(R.color.headerBackground)
                .fragment(R.layout.intro_3)
                .canGoForward(false)
                .build());
    }

    public void onStart(){
        super.onStart();

        // verifica se usuário está logado antes de iniciar
        verificarUsuarioLogado();
    }

    // Verifica se o usuário está logado
    public void verificarUsuarioLogado(){
        // Verifica no firebase se o usuário está logado
        autenticacao = ConfigFirebase.getFirebaseAutenticacao();

        /* Faz com que o usuário seja deslogado
        autenticacao.signOut(); */

        if(autenticacao.getCurrentUser() != null){
            // Se o usuário estiver logado, abre a tela principal
            abrirTelaPrincipal();

        }
    }

    // Abre a tela principal
    public void abrirTelaPrincipal() {
        startActivity(new Intent(getApplicationContext(), PrincipalActivity.class));
    }

    // Redireciona para tela de login
    public void redirectEntrar(View view){
        startActivity(new Intent(this, LoginActivity.class));
        finish();
    }

    // Redireciona para tela de cadastro
    public void redirectCadastrar(View view){
        startActivity(new Intent(this, CadastroActivity.class));
        finish();
    }
}