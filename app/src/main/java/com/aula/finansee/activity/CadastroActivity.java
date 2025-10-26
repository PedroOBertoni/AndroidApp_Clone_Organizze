package com.aula.finansee.activity;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import com.aula.finansee.R;
import com.aula.finansee.config.ConfigFirebase;
import com.aula.finansee.model.Usuario;
import com.aula.finansee.utils.FirebaseErrorHandler;
import com.google.android.material.progressindicator.LinearProgressIndicator;
import com.google.android.material.snackbar.Snackbar;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException;
import com.google.firebase.auth.FirebaseAuthUserCollisionException;
import com.google.firebase.auth.FirebaseAuthWeakPasswordException;

public class CadastroActivity extends AppCompatActivity {

    // Componentes da interface do ForcaSenha (progressBar e texto que exibem a força da senha)
    private LinearProgressIndicator progressBar;
    private TextView textForca;

    // Componentes da interface do RequisitosContainer
    LinearLayout layoutRequisitosSenha;
    ImageView imageArrowRequisitos;
    View headerRequisitosContainer;
    boolean requisitosExpanded = false;

    // Objeto para autenticação do Firebase
    private FirebaseAuth autenticacao;
    private Usuario usuario;

    // Componentes da interface
    private EditText editNome, editEmail, editSenha;
    private TextInputLayout inputNome, inputEmail, inputSenha;
    private Button buttonCadastra;

    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_cadastro);

        /* Recuperando componentes da interface pelo ID */

        // Elementos do layout Força de Senha (progressBar e texto que mudam conforme a senha é digitada)
        progressBar = findViewById(R.id.progressBarForcaSenha);
        textForca = findViewById(R.id.textForcaSenha);

        // Elementos do RequisitosContainer (lista de requisitos para a senha)
        layoutRequisitosSenha = findViewById(R.id.layoutRequisitosSenha);
        imageArrowRequisitos = findViewById(R.id.imageArrowRequisitos);
        headerRequisitosContainer = findViewById(R.id.headerRequisitosContainer);

        // EditText
        editNome = findViewById(R.id.editNomeCadastro);
        editEmail = findViewById(R.id.editEmailCadastro);
        editSenha = findViewById(R.id.editSenhaCadastro);

        // TextInputLayout
        inputNome = findViewById(R.id.layoutNomeCadastro);
        inputEmail = findViewById(R.id.layoutEmailCadastro);
        inputSenha = findViewById(R.id.layoutSenhaCadastro);

        // Button
        buttonCadastra = findViewById(R.id.buttonCadastra);

        /* textChangedListeners */

        /* Aplica a mudança de cor, texto ao lado e progresso da LinearProgressIndicator conforme
            o usuário digita */
        editSenha.addTextChangedListener(new TextWatcher() {
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
                progressBar.setIndicatorColor(ContextCompat.getColor(CadastroActivity.this, corResId));
                textForca.setText(texto);
                textForca.setTextColor(ContextCompat.getColor(CadastroActivity.this, corResId));
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

        // Botão de cadastro
        buttonCadastra.setOnClickListener(v -> {
            // Captura os textos no momento do clique
            String textoNome = editNome.getText().toString().trim();
            String textoEmail = editEmail.getText().toString().trim();
            String textoSenha = editSenha.getText().toString().trim();

            // Chama o método de validação do preenchimento dos campos
            validaCampos(textoNome, textoEmail, textoSenha);
        });

        /* Redirecionamento para outras páginas */
        TextView linkTermosDeUso = findViewById(R.id.linkTermosDeUso);
        TextView linkLogin = findViewById(R.id.linkLogin);

        linkTermosDeUso.setOnClickListener(v -> {
            Intent intent = new Intent(CadastroActivity.this, TermosDeUsoActivity.class);
            startActivity(intent);
        });

        linkLogin.setOnClickListener(v -> {
            Intent intent = new Intent(CadastroActivity.this, LoginActivity.class);
            startActivity(intent);
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

    // Valida os campos de nome, email e senha
    public void validaCampos(String nome, String email, String senha) {

        // Limpa erros anteriores
        inputNome.setError(null);
        inputEmail.setError(null);
        inputSenha.setError(null);

        // Variáveis para controle de validação e exceção
        boolean valido = true;
        String excecao;

        if (nome.isEmpty()) {
            // Validação de campo de nome vazio
            excecao = "Preencha o nome!";
            inputNome.setError(excecao);

            // Mostra erro geral com Snackbar
            Snackbar.make(findViewById(android.R.id.content),
                    excecao,
                    Snackbar.LENGTH_LONG).show();

            // e define válido como falso
            valido = false;
        }

        if (email.isEmpty()) {
            // Validação de campo de e-mail vazio
            excecao = "Preencha o e-mail!";
            inputEmail.setError(excecao);

            // Mostra erro geral com Snackbar
            Snackbar.make(findViewById(android.R.id.content),
                    excecao,
                    Snackbar.LENGTH_LONG).show();

            // e define válido como falso
            valido = false;
        }

        if (senha.isEmpty()) {
            // Validação de campo de senha vazio
            excecao = "Preencha a senha!";
            inputSenha.setError(excecao);

            // Mostra erro geral com Snackbar
            Snackbar.make(findViewById(android.R.id.content),
                    excecao,
                    Snackbar.LENGTH_LONG).show();

            // e define válido como falso
            valido = false;

        } else if (!senha.matches("^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)(?=.*[!@#$%^&*+=?_.#-]).{8,}$")) {
            excecao = "A senha deve ter ao menos 8 caracteres, com letra maiúscula, minúscula, número e símbolo!";
            inputSenha.setError(excecao);

            Snackbar.make(findViewById(android.R.id.content),
                    excecao,
                    Snackbar.LENGTH_LONG).show();

            valido = false;
        }

        if (valido) {
            cadastrarUsuario(email, senha, nome);
        }
    }

    // Cadastra o usuário no Firebase
    public void cadastrarUsuario(String email, String senha, String nome) {

        // Primeiro verifica se o aparelho está conectado a uma rede ativa
        if (!FirebaseErrorHandler.checkConnectionAndNotify(this, "fazer cadastro")) {
            return;
        }

        // Criando objeto usuário
        usuario = new Usuario();

        // Definindo seu nome e email
        usuario.setNome(nome);
        usuario.setEmail(email.toLowerCase().trim());

        // Cadastrando usuário com e-mail e senha no Firebase
        autenticacao = ConfigFirebase.getFirebaseAutenticacao();
        autenticacao.createUserWithEmailAndPassword(email, senha)
                .addOnCompleteListener(this, task -> {
                    if ( task.isSuccessful() ) {

                        // Criando o usuário por meio do UID do Firebase
                        String idUsuario = task.getResult().getUser().getUid();
                        usuario.setIdUsuario(idUsuario);

                        // Salvando usuário por meio do método salvar() da classe Usuario
                        usuario.salvar();

                        // Finalizando a Activity de Cadastro e iniciando a Activity Principal
                        startActivity(new Intent(getApplicationContext(), PrincipalActivity.class));
                        finish();

                    } else {
                        /* Definindo varivabel captura das mensagens de erro no tratamento de erros
                        específicos do Firebase */
                        String excecao;

                        try {
                            // Lança a exceção capturada para tratá-la
                            throw task.getException();

                        } catch (FirebaseAuthWeakPasswordException e) {
                            // Senha fraca
                            excecao = "Digite uma senha mais forte!";
                            inputSenha.setError(excecao);

                        } catch (FirebaseAuthInvalidCredentialsException e) {
                            // E-mail inválido
                            excecao = "Por favor, digite um e-mail válido!";
                            inputEmail.setError(excecao);

                        } catch (FirebaseAuthUserCollisionException e) {
                            // E-mail já cadastrado
                            excecao = "Esta conta já foi cadastrada!";
                            inputEmail.setError(excecao);

                        } catch (Exception e) {
                            // Erro geral
                            excecao = "Erro ao cadastrar: " + e.getMessage();
                            e.printStackTrace();

                        }

                        // Mostra erro geral com Snackbar
                        Snackbar.make(findViewById(android.R.id.content),
                                excecao,
                                Snackbar.LENGTH_LONG).show();
                    }
                });
    }

    // Redireciona para a página Termos de Uso
    public void redirectTermosDeUso(View view){
        startActivity(new Intent(this, TermosDeUsoActivity.class));
        finish();
    }

    // Redireciona para a tela de Login
    public void redirectEntrar(View view){
        startActivity(new Intent(this, LoginActivity.class));
        finish();
    }
}