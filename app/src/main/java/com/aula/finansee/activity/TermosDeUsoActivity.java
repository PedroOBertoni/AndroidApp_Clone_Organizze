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

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_termos_de_uso);

        TextView linkCadastro = findViewById(R.id.linkCadastro);
        TextView linkLogin = findViewById(R.id.linkLogin);

        linkCadastro.setOnClickListener(v -> {
            Intent intent = new Intent(TermosDeUsoActivity.this, CadastroActivity.class);
            startActivity(intent);
        });

        linkLogin.setOnClickListener(v -> {
            Intent intent = new Intent(TermosDeUsoActivity.this, LoginActivity.class);
            startActivity(intent);
        });
    }
}