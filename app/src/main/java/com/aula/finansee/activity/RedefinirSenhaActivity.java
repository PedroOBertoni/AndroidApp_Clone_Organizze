package com.aula.finansee.activity;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.aula.finansee.R;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.auth.FirebaseAuth;

public class RedefinirSenhaActivity extends AppCompatActivity {

    // Componentes do layout activity
    private EditText editNovaSenha, editConfirmarSenha;
    private Button buttonSalvarSenha;

    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_redefinir_senha);

        // Recupera os componentes da interface pelo ID
        editNovaSenha = findViewById(R.id.editNovaSenha);
        editConfirmarSenha = findViewById(R.id.editConfirmarSenha);
        buttonSalvarSenha = findViewById(R.id.buttonSalvarSenha);

        //
        String email = getIntent().getStringExtra("email");

        // Ao clicar no votão salvar senha
        buttonSalvarSenha.setOnClickListener(v -> {
            // recupera o texto
            String novaSenha = editNovaSenha.getText().toString().trim();
            String confirmarSenha = editConfirmarSenha.getText().toString().trim();

            if (novaSenha.isEmpty() || confirmarSenha.isEmpty()) {
                Snackbar.make(findViewById(android.R.id.content),
                        "Preencha todos os campos", Snackbar.LENGTH_LONG).show();
                return;
            }

            if (!novaSenha.equals(confirmarSenha)) {
                Snackbar.make(findViewById(android.R.id.content),
                        "As senhas não conferem", Snackbar.LENGTH_LONG).show();
                return;
            }

            FirebaseAuth autenticacao = FirebaseAuth.getInstance();
            autenticacao.sendPasswordResetEmail(email)
                    .addOnCompleteListener(task -> {
                        if (task.isSuccessful()) {
                            Snackbar.make(findViewById(android.R.id.content),
                                    "Senha redefinida! Verifique seu email.", Snackbar.LENGTH_LONG).show();
                            finish();
                        } else {
                            Snackbar.make(findViewById(android.R.id.content),
                                    "Não foi possível redefinir a senha.", Snackbar.LENGTH_LONG).show();
                        }
                    });
        });
    }
}
