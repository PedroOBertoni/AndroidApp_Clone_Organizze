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

    // objeto para autenticação do Firebase
    private FirebaseAuth autenticacao;

    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_redefinir_senha);

        /* Recuperando componentes da interface pelo ID */

        // Elementos do layout Força de Senha (progressBar e texto que mudam conforme a senha é digitada)
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
        editNovaSenha = findViewById(R.id.editNovaSenha);
        editConfirmarSenha = findViewById(R.id.editConfirmarSenha);

        // Button
        buttonSalvarSenha = findViewById(R.id.buttonSalvarSenha);

        // Recupera o email passado pela intent
        String email = getIntent().getStringExtra("email");

        /* textChangedListener */

        /* Aplica a mudança de cor, texto ao lado e progresso da LinearProgressIndicator conforme
            o usuário digita */
        editNovaSenha.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                // Captura a senha digitada
                String senha = s.toString();

                // Calcula o score com base força da senha (quantos requisitos da senha foram atendidos)
                int score = calcularForcaSenha(senha);

                /* Instancia variáveis para progresso, texto e cor que serão definidos com base na
                    força da senha */
                int progress;
                String texto;
                int corResId;

                // Verifica o score e aplica os valores para as variaveis de acordo com o score
                if (score < 1) { // Se não foi digitado nada o texto permanece cinza e vazio
                    progress = 0;
                    texto = "";
                    corResId = R.color.textGray;

                } else if (score == 1) { // Se tiver 1 de score (1 requisito atendido)
                    progress = 10; // Preenche 10% da progressBar, texto "Muito Fraca" e cor vermelho
                    texto = "Muito Fraca";
                    corResId = R.color.error;

                } else if (score == 2) { // Se tiver 2 de score (2 requisitos atendidos)
                    progress = 30; // Preenche 30% da progressBar, texto "Fraca" e cor vermelho
                    texto = "Fraca";
                    corResId = R.color.error;

                } else if (score == 3) { // Se tiver 3 de score (3 requisitos atendidos)
                    progress = 50; // Preenche 50% da progressBar, texto "Médio" e cor amarelo alaranjado
                    texto = "Médio";
                    corResId = R.color.warning;

                } else if (score == 4) { // Se tiver 4 de score (4 requisitos atendidos)
                    progress = 80; // Preenche 80% da progressBar, texto "Forte" e cor verde claro
                    texto = "Forte";
                    corResId = R.color.colorAccentReceita;

                } else { // Se tiver mais de 4 de score (5 (todos) requisitos atendido)
                    progress = 100; // Preenche 100% da progressBar, texto "Muito forte" e cor verde escuro
                    texto = "Muito forte";
                    corResId = R.color.colorPrimaryDarkReceita;
                }

                // Atualiza a progressBar com animação suave
                progressBar.setProgress(progress, true);
                progressBar.setIndicatorColor(ContextCompat.getColor(RedefinirSenhaActivity.this, corResId));

                // E tambem o texto ao lado dela
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
            if (validarCampos()) {
                // Redefinir senha via Firebase
                FirebaseAuth autenticacao = FirebaseAuth.getInstance();
                autenticacao.sendPasswordResetEmail(email)
                        .addOnCompleteListener(task -> {
                            if (task.isSuccessful()) {
                                // Se der certo a task, apresenta mensagem de sucesso por meio da snackBar
                                Snackbar.make(findViewById(android.R.id.content),
                                        "Senha redefinida! Verifique seu email.",
                                        Snackbar.LENGTH_LONG).show();

                                // E depois finaliza a activity
                                finish();

                            } else {
                                // Se der errado a task, apresenta mensagem de erro por meio da snackBar
                                Snackbar.make(findViewById(android.R.id.content),
                                        "Não foi possível redefinir a senha.",
                                        Snackbar.LENGTH_LONG).show();
                            }
                        });
            }
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

    private boolean validarCampos() {
        boolean valido = true;
        String excecao = null;

        String novaSenha = editNovaSenha.getText().toString().trim();
        String confirmarSenha = editConfirmarSenha.getText().toString().trim();

        // Validação campo vazio
        if (novaSenha.isEmpty()) {
            excecao = "Preencha a senha!";
            inputSenha.setError(excecao);
            Snackbar.make(findViewById(android.R.id.content),
                    excecao,
                    Snackbar.LENGTH_LONG).show();
            valido = false;
        }

        // Regex de senha forte
        String regexSenhaForte = "^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)(?=.*[@$!%*?&#.])[A-Za-z\\d@$!%*?&#.]{8,}$";

        if (!novaSenha.matches(regexSenhaForte)) {
            excecao = "A senha deve conter ao menos:\n" +
                    "- Uma letra maiúscula\n" +
                    "- Uma letra minúscula\n" +
                    "- Um número\n" +
                    "- Um símbolo (@, $, !, %, *, ?, &, # ou .)\n" +
                    "- Mínimo de 8 caracteres";
            inputSenha.setError(excecao);

            Snackbar.make(findViewById(android.R.id.content),
                    excecao,
                    Snackbar.LENGTH_LONG).show();

            valido = false;
        }

        // Confirmação de senha
        if (confirmarSenha.isEmpty()) {
            Snackbar.make(findViewById(android.R.id.content),
                    "Confirme a senha!", Snackbar.LENGTH_LONG).show();

            valido = false;
        }

        if (!novaSenha.equals(confirmarSenha)) {
            Snackbar.make(findViewById(android.R.id.content),
                    "As senhas não conferem",
                    Snackbar.LENGTH_LONG).show();

            valido = false;
        }

        return valido;
    }

}
