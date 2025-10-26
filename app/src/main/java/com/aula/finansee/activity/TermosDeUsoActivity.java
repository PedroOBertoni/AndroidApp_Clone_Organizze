package com.aula.finansee.activity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.aula.finansee.R;

public class TermosDeUsoActivity extends AppCompatActivity {

    // Componentes da interface
    private TextView linkCadastro;
    private TextView linkLogin;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_termos_de_uso);

        /* Recuperando componentes da interface pelo ID */

        // TextViews para links de navegação
        linkCadastro = findViewById(R.id.linkCadastro);
        linkLogin = findViewById(R.id.linkLogin);

        /* onClickListenners */

        // Redireciona para a tela de cadastro
        linkCadastro.setOnClickListener(v -> {
            Intent intent = new Intent(TermosDeUsoActivity.this, CadastroActivity.class);
            startActivity(intent);
        });

        // Redireciona para a tela de login
        linkLogin.setOnClickListener(v -> {
            Intent intent = new Intent(TermosDeUsoActivity.this, LoginActivity.class);
            startActivity(intent);
        });
    }
}