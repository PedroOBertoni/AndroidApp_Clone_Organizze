package com.aula.finansee.activity;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;

import androidx.appcompat.app.AppCompatActivity;

import com.aula.finansee.R;
import com.google.android.material.snackbar.Snackbar;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.auth.FirebaseAuth;

public class RedefinirSenhaActivity extends AppCompatActivity {

    // Componentes do layout activity
    private TextInputLayout inputSenha, inputConfirmarSenha;
    private EditText editNovaSenha, editConfirmarSenha;
    private Button buttonSalvarSenha;

    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_redefinir_senha);

        // Recupera os TextInputLayouts
        inputSenha = findViewById(R.id.inputSenha);
        inputConfirmarSenha = findViewById(R.id.inputConfirmarSenha);

        // Recupera os EditTexts internos
        editNovaSenha = inputSenha.getEditText();
        editConfirmarSenha = inputConfirmarSenha.getEditText();

        // Recupera o botão
        buttonSalvarSenha = findViewById(R.id.buttonSalvarSenha);

        // Recupera o email passado pela intent
        String email = getIntent().getStringExtra("email");

        // Ao clicar no botão salvar senha
        buttonSalvarSenha.setOnClickListener(v -> {
            // Recupera o texto dos EditTexts
            String novaSenha = editNovaSenha.getText().toString().trim();
            String confirmarSenha = editConfirmarSenha.getText().toString().trim();

            // Validação
            if (novaSenha.isEmpty()) {
                Snackbar.make(findViewById(android.R.id.content),
                        "Preencha a senha!", Snackbar.LENGTH_LONG).show();
                return;
            }

            /*
            if (novaSenha.isEmpty()) {
                // Validação de campo de senha vazio
                excecao = "Preencha a senha!";
                inputSenha.setError(excecao);

                // Mostra erro geral com Snackbar
                Snackbar.make(findViewById(android.R.id.content),
                        excecao,
                        Snackbar.LENGTH_LONG).show();

                // e define válido como falso
                valido = false;

            } else {
                // Validação de senha com critérios de segurança mais fortes
                // Regex exige:
                // - Pelo menos 1 letra minúscula
                // - Pelo menos 1 letra maiúscula
                // - Pelo menos 1 número
                // - Pelo menos 1 caractere especial
                // - Mínimo de 8 caracteres
                String regexSenhaForte = "^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)(?=.*[@$!%*?&])[A-Za-z\\d@$!%*?&]{6,}$";

                if (!senha.matches(regexSenhaForte)) {
                    excecao = "A senha deve conter ao menos:\n" +
                            "- Uma letra maiúscula\n" +
                            "- Uma letra minúscula\n" +
                            "- Um número\n" +
                            "- Um símbolo (@, $, !, %, *, ?, &)";
                    layoutSenha.setError(excecao);

                    // Mostra erro geral com Snackbar
                    Snackbar.make(findViewById(android.R.id.content),
                            excecao,
                            Snackbar.LENGTH_LONG).show();

                    // Define como inválido
                    valido = false;
                }
            } */

            if (confirmarSenha.isEmpty()) {
                Snackbar.make(findViewById(android.R.id.content),
                        "Confirme a senha!", Snackbar.LENGTH_LONG).show();
                return;
            }

            if (!novaSenha.equals(confirmarSenha)) {
                Snackbar.make(findViewById(android.R.id.content),
                        "As senhas não conferem", Snackbar.LENGTH_LONG).show();
                return;
            }

            // Redefinir senha via Firebase
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
