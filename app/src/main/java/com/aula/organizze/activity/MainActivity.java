package com.aula.organizze.activity;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;

import androidx.appcompat.app.AppCompatActivity;

import com.aula.organizze.config.ConfigFirebase;
import com.google.firebase.auth.FirebaseAuth;
import com.heinrichreimersoftware.materialintro.app.IntroActivity;
import com.aula.organizze.R;

public class MainActivity extends IntroActivity {

    private FirebaseAuth autenticacao;

    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // esconde botões padrão
        setButtonBackVisible(false);
        setButtonNextVisible(false);

        // adiciona slides
        addSlide(new com.heinrichreimersoftware.materialintro.slide.FragmentSlide.Builder()
                .background(R.color.headerBackground)
                .fragment(R.layout.intro_1)
                .build());

        addSlide(new com.heinrichreimersoftware.materialintro.slide.FragmentSlide.Builder()
                .background(R.color.headerBackground)
                .fragment(R.layout.intro_2)
                .build());

        addSlide(new com.heinrichreimersoftware.materialintro.slide.FragmentSlide.Builder()
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

    public void redirectEntrar(View view){
        startActivity(new Intent(this, LoginActivity.class));
        finish();
    }

    public void redirectCadastrar(View view){
        startActivity(new Intent(this, CadastroActivity.class));
        finish();
    }

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

    public void abrirTelaPrincipal() {
        startActivity(new Intent(getApplicationContext(), PrincipalActivity.class));
    }
}