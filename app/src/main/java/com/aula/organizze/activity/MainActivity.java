package com.aula.organizze.activity;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;

import androidx.appcompat.app.AppCompatActivity;

import com.heinrichreimersoftware.materialintro.app.IntroActivity;
import com.aula.organizze.R;

public class MainActivity extends IntroActivity {

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

    public void redirectEntrar(View view){
        startActivity(new Intent(this, LoginActivity.class));
    }

    public void redirectCadastrar(View view){
        startActivity(new Intent(this, CadastroActivity.class));
    }
}