package com.aula.finansee.activity;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import com.aula.finansee.R;
import com.google.android.material.progressindicator.LinearProgressIndicator;
import com.google.android.material.snackbar.Snackbar;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.auth.FirebaseAuth;

public class RedefinirSenhaActivity extends AppCompatActivity {

    // Componentes da interface do ForcaSenha (progressBar e texto que exibem a força da senha)
    private LinearProgressIndicator progressBar;
    private TextView textForca;

    // Componentes da interface do RequisitosContainer
    LinearLayout layoutRequisitosSenha;
    ImageView imageArrowRequisitos;
    View headerRequisitosContainer;
    boolean requisitosExpanded = false;

    // Componentes da interface
    private TextInputLayout inputSenha, inputConfirmarSenha;
    private EditText editNovaSenha, editConfirmarSenha;
    private Button buttonSalvarSenha;

    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_redefinir_senha);

        /* Recuperando componentes da interface pelo ID */

        // Elementos do layout Força de Senha (progressBar e texto que mudam conforme a senha é digitada)
        editNovaSenha = findViewById(R.id.editNovaSenha);
        progressBar = findViewById(R.id.progressBarForcaSenha);
        textForca = findViewById(R.id.textForcaSenha);

        // Elementos do RequisitosContainer (lista de requisitos para a senha)
        layoutRequisitosSenha = findViewById(R.id.layoutRequisitosSenha);
        imageArrowRequisitos = findViewById(R.id.imageArrowRequisitos);
        headerRequisitosContainer = findViewById(R.id.headerRequisitosContainer);

        // TextInputLayout
        inputSenha = findViewById(R.id.inputSenha);
        inputConfirmarSenha = findViewById(R.id.inputConfirmarSenha);

        // EditText
        editNovaSenha = inputSenha.getEditText();
        editConfirmarSenha = inputConfirmarSenha.getEditText();

        // Button
        buttonSalvarSenha = findViewById(R.id.buttonSalvarSenha);

        // Recupera o email passado pela intent
        String email = getIntent().getStringExtra("email");

        /* Aplica a mudança de cor, texto ao lado e progresso da LinearProgressIndicator conforme
            o usuário digita */
        editNovaSenha.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                String senha = s.toString();
                int score = calcularForcaSenha(senha);

                int progress;
                String texto;
                int corResId;

                if (score < 1) {
                    progress = 0;
                    texto = "";
                    corResId = R.color.textGray;

                } else if (score == 1) {
                    progress = 10;
                    texto = "Muito Fraca";
                    corResId = R.color.error;

                } else if (score == 2) {
                    progress = 30;
                    texto = "Fraca";
                    corResId = R.color.error;

                } else if (score == 3) {
                    progress = 50;
                    texto = "Médio";
                    corResId = R.color.warning;

                } else if (score == 4) {
                    progress = 80;
                    texto = "Forte";
                    corResId = R.color.colorAccentReceita;

                } else {
                    progress = 100;
                    texto = "Muito forte";
                    corResId = R.color.colorPrimaryDarkReceita;
                }

                // Atualiza com animação suave
                progressBar.setProgress(progress, true);
                progressBar.setIndicatorColor(ContextCompat.getColor(RedefinirSenhaActivity.this, corResId));
                textForca.setText(texto);
                textForca.setTextColor(ContextCompat.getColor(RedefinirSenhaActivity.this, corResId));
            }

            @Override
            public void afterTextChanged(Editable s) {}
        });

        /* onClickListenners */

        // Texto de requisitos de senha expansível
        headerRequisitosContainer.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                toggleRequisitos();
            }
        });

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

    // Método que calcula a força da senha
    private int calcularForcaSenha(String senha) {
        int score = 0;

        if (senha.length() >= 8) score++;
        if (senha.matches(".*[A-Z].*")) score++;
        if (senha.matches(".*[a-z].*")) score++;
        if (senha.matches(".*[0-9].*")) score++;
        if (senha.matches(".*[!@#$%^&*()_+\\-={}\\[\\]|:;\"'<>,.?/~`].*")) score++;

        return score;
    }

    // Método para abrir/fechar os requisitos de senha
    private void toggleRequisitos() {
        if (requisitosExpanded) {
            layoutRequisitosSenha.animate()
                    .alpha(0f)
                    .setDuration(180)
                    .withEndAction(() -> {
                        layoutRequisitosSenha.setVisibility(View.GONE);
                        layoutRequisitosSenha.setAlpha(1f);
                    }).start();
            imageArrowRequisitos.animate().rotation(0f).setDuration(180).start();
        } else {
            layoutRequisitosSenha.setAlpha(0f);
            layoutRequisitosSenha.setVisibility(View.VISIBLE);
            layoutRequisitosSenha.animate().alpha(1f).setDuration(180).start();
            imageArrowRequisitos.animate().rotation(180f).setDuration(180).start();
        }
        requisitosExpanded = !requisitosExpanded;
    }
}
