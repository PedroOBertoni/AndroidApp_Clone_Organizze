package com.aula.finansee.activity;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.graphics.drawable.GradientDrawable;
import android.os.Bundle;
import android.text.InputType;
import android.util.Patterns;
import android.util.TypedValue;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.view.ContextThemeWrapper;
import androidx.core.content.ContextCompat;

import com.aula.finansee.R;
import com.aula.finansee.config.ConfigFirebase;
import com.aula.finansee.utils.FirebaseErrorHandler;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.snackbar.Snackbar;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException;
import com.google.firebase.auth.FirebaseAuthInvalidUserException;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import org.json.JSONObject;

import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;

public class LoginActivity extends AppCompatActivity {

    // Componentes da interface
    private TextView resetaSenha;
    private EditText editEmail, editSenha;
    private TextInputLayout inputEmail, inputSenha;
    private Button buttonEntra;

    // Objeto para autenticação do Firebase
    private FirebaseAuth autenticacao;

    // Código para a redefinição de senha
    private final int minutosExpiracao = 3; // código expira em 3 minutos
    private String codigoAtual = "";
    private long codigoExpiracaoMillis = 0L; // timestamp em milissegundos
    private static final long RELOAD_COOLDOWN_MS = 30_000L; // 30 segundos de cooldown para reenviar
    private long ultimoEnvioMillis = 0L;


    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_login);

        /* Recupera os componentes da interface pelo ID */

        // TextView
        resetaSenha = findViewById(R.id.resetaSenha);

        // EditText
        editEmail = findViewById(R.id.editNovaSenha);
        editSenha = findViewById(R.id.editConfirmarSenha);

        // TextInputLayout
        inputEmail = findViewById(R.id.inputSenha);
        inputSenha = findViewById(R.id.inputConfirmarSenha);

        // Aplica a função que limpa erro ao focar
        limparErroAoFocar(inputEmail, editEmail);
        limparErroAoFocar(inputSenha, editSenha);

        // Button
        buttonEntra = findViewById(R.id.buttonEntra);

        /* onClickListenners */

        // Listener do botão de login
        buttonEntra.setOnClickListener(v -> {
            // Captura os textos no momento do clique
            String textoEmail = editEmail.getText().toString().toLowerCase().trim();
            String textoSenha = editSenha.getText().toString().trim();

            // Valida os campos antes de enviar
            validaCampos(textoEmail, textoSenha);
        });

        resetaSenha.setOnClickListener(v -> {
            // chama o método para redefinição de senha
            exibirDialogRedefinirSenha();
        });

        /* Redirecionamento para outras páginas */

        TextView linkTermosDeUso = findViewById(R.id.linkTermosDeUso);
        TextView linkCadastro = findViewById(R.id.linkCadastro);

        // Redireciona para a tela de Termos de Uso
        linkTermosDeUso.setOnClickListener(v -> {
            Intent intent = new Intent(LoginActivity.this, TermosDeUsoActivity.class);
            startActivity(intent);
        });

        // Redireciona para a tela de Cadastro
        linkCadastro.setOnClickListener(v -> {
            Intent intent = new Intent(LoginActivity.this, CadastroActivity.class);
            startActivity(intent);
        });
    }

    // Método que limpa erros ao clicar no TextInputLayout do campo com o erro
    private void limparErroAoFocar(TextInputLayout layout, EditText editText) {
        // Se focar no campo, remove o erro
        editText.setOnFocusChangeListener((v, hasFocus) -> {
            if (hasFocus) {
                // Remove o erro
                layout.setError(null);
                layout.setErrorEnabled(false);

                // Reforça o ícone de vizualizar senha (olho), caso o campo seja de senha
                if (layout.getEndIconMode() == TextInputLayout.END_ICON_PASSWORD_TOGGLE) {
                    layout.setEndIconMode(TextInputLayout.END_ICON_PASSWORD_TOGGLE);
                }
            }
        });

        // Se o usuário clicar no campo (mesmo sem foco ainda)
        editText.setOnClickListener(v -> {
            // Remove o erro
            layout.setError(null);
            layout.setErrorEnabled(false);

            // E também reforça o ícone de vizualizar senha (olho), caso o campo seja de senha
            if (layout.getEndIconMode() == TextInputLayout.END_ICON_PASSWORD_TOGGLE) {
                layout.setEndIconMode(TextInputLayout.END_ICON_PASSWORD_TOGGLE);
            }
        });
    }

    // Método que valida os campos de email e senha
    public void validaCampos(String email, String senha) {

        // Limpa erros anteriores
        inputEmail.setError(null);
        inputSenha.setError(null);

        // Variáveis para controle de validação e exceção
        boolean valido = true;
        String excecao;

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

        } else if (senha.length() < 6) {
            // Validação de senha com no mínimo 6 caracteres
            excecao = "A senha deve ter no mínimo 6 caracteres!";
            inputSenha.setError(excecao);

            // Mostra erro geral com Snackbar
            Snackbar.make(findViewById(android.R.id.content),
                    excecao,
                    Snackbar.LENGTH_LONG).show();

            // e define válido como falso
            valido = false;
        }

        // Caso tudo esteja válido, tenta autenticar o usuário
        if (valido) {
            validarLoginUsuario(email, senha);
        }
    }

    // Método que valida o login do usuário com Firebase Authentication
    public void validarLoginUsuario(String email, String senha) {

        // Primeiro verifica se o aparelho está conectado a uma rede ativa
        if (!FirebaseErrorHandler.checkConnectionAndNotify(this, "fazer login")) {
            return;
        }

        // Obtém a instância do FirebaseAuth
        autenticacao = ConfigFirebase.getFirebaseAutenticacao();

        // Tenta autenticar o usuário com e-mail e senha
        autenticacao.signInWithEmailAndPassword(
                email,
                senha
        ).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
            @Override
            public void onComplete(@NonNull Task<AuthResult> task) {

                if (task.isSuccessful()) {
                    // Login bem-sucedido: redireciona para a tela principal
                    abrirTelaPrincipal();

                } else {
                    /* Definindo varivabel captura das mensagens de erro no tratamento de erros
                        específicos do Firebase */
                    String excecao;

                    try {
                        // Lança a exceção capturada para tratá-la
                        throw task.getException();

                    } catch (FirebaseAuthInvalidUserException e) {
                        // Usuário não cadastrado
                        excecao = "Usuário digitado não está cadastrado!";
                        inputEmail.setError(excecao);

                    } catch (FirebaseAuthInvalidCredentialsException e) {
                        // E-mail e/ou senha inválidos
                        excecao = "E-mail e/ou senha não correspondem a um usuário cadastrado!";
                        inputSenha.setError(excecao);

                    } catch (Exception e) {
                        // Erro genérico
                        excecao = "Erro ao fazer login: " + e.getMessage();
                        e.printStackTrace();
                    }

                    // Mostra erro geral com Snackbar
                    Snackbar.make(findViewById(android.R.id.content),
                            excecao,
                            Snackbar.LENGTH_LONG).show();
                }
            }
        });
    }

    // Método que exibe o diálogo para redefinir a senha
    @SuppressLint("ResourceAsColor")
    private void exibirDialogRedefinirSenha() {
        MaterialAlertDialogBuilder builder = new MaterialAlertDialogBuilder(
                new ContextThemeWrapper(this, R.style.RoundedAlertDialogTheme)
        );
        builder.setTitle("Redefinir senha");

        // EditText simples com borda/fundo conforme você já usa
        EditText inputEmail = new EditText(this);
        inputEmail.setHint("Digite seu e-mail");
        inputEmail.setInputType(InputType.TYPE_TEXT_VARIATION_EMAIL_ADDRESS);
        inputEmail.setPadding(32, 24, 32, 24);

        // aplica borda arredondada personalizada
        GradientDrawable border = new GradientDrawable();
        border.setCornerRadius(16);
        border.setStroke(2, ContextCompat.getColor(this, R.color.textGray));
        border.setColor(ContextCompat.getColor(this, R.color.colorBackgroundDialog));
        inputEmail.setBackground(border);

        // adiciona padding lateral ao container
        LinearLayout container = new LinearLayout(this);
        container.setOrientation(LinearLayout.VERTICAL);
        int margin = (int) TypedValue.applyDimension(
                TypedValue.COMPLEX_UNIT_DIP, 16, getResources().getDisplayMetrics()
        );
        container.setPadding(margin, 0, margin, 0);
        container.addView(inputEmail);

        // configura o view do  alertDialog
        builder.setView(container);

        // configura o botão positivo do alertDialog
        builder.setPositiveButton("ENVIAR", (dialog, which) -> {
            String email = inputEmail.getText().toString().trim();

            if (email.isEmpty()) {
                Snackbar.make(findViewById(android.R.id.content),
                        "Informe o email!",
                        Snackbar.LENGTH_LONG).show();
                return;
            }

            if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
                Snackbar.make(findViewById(android.R.id.content),
                        "Digite um e-mail válido!",
                        Snackbar.LENGTH_LONG).show();
                return;
            }

            // monta corpo do email (seu método EmailSender usa apenas email+codigo; eu recomendo passar corpo/assunto lá)
            // aqui chamamos seu método que envia o email em background
            solicitarRedefinicaoSenha(email);
        });

        // configura o botão negativo do alertDialog
        builder.setNegativeButton("CANCELAR", (dialog, which) -> dialog.dismiss());
        builder.show();
    }

    // Método que envia o email de redefinição de senha personalizado
    private void solicitarRedefinicaoSenha(final String email) {
        if (email.isEmpty()) {
            Snackbar.make(findViewById(android.R.id.content),
                    "Informe o email!",
                    Snackbar.LENGTH_LONG).show();
            return;
        }

        if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            Snackbar.make(findViewById(android.R.id.content),
                    "Digite um e-mail válido!",
                    Snackbar.LENGTH_LONG).show();
            return;
        }

        // Verifica se existe usuário com esse email no Firebase
        DatabaseReference usuariosRef = FirebaseDatabase.getInstance()
                .getReference()
                .child("usuarios");

        usuariosRef.orderByChild("email").equalTo(email)
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        if (snapshot.exists()) {
                            // Usuário existe: envia requisição para o backend
                            new Thread(() -> {
                                try {
                                    // Monta JSON com email
                                    JSONObject body = new JSONObject();
                                    body.put("email", email);

                                    // Faz requisição HTTP POST para o backend
                                    URL url = new URL("https://finansee-backend.onrender.com/api/enviar-reset");
                                    HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                                    conn.setRequestMethod("POST");
                                    conn.setRequestProperty("Content-Type", "application/json; charset=UTF-8");
                                    conn.setDoOutput(true);

                                    OutputStream os = conn.getOutputStream();
                                    os.write(body.toString().getBytes(StandardCharsets.UTF_8));
                                    os.close();

                                    int responseCode = conn.getResponseCode();
                                    conn.disconnect();

                                    runOnUiThread(() -> {
                                        if (responseCode == 200) {
                                            Snackbar.make(findViewById(android.R.id.content),
                                                    "Link de redefinição enviado! Verifique seu email.",
                                                    Snackbar.LENGTH_LONG).show();
                                        } else {
                                            Snackbar.make(findViewById(android.R.id.content),
                                                    "Falha ao enviar link de redefinição.",
                                                    Snackbar.LENGTH_LONG).show();
                                        }
                                    });
                                } catch (Exception e) {
                                    e.printStackTrace();
                                    runOnUiThread(() -> Snackbar.make(findViewById(android.R.id.content),
                                            "Erro ao enviar link de redefinição.",
                                            Snackbar.LENGTH_LONG).show());
                                }
                            }).start();
                        } else {
                            Snackbar.make(findViewById(android.R.id.content),
                                    "Nenhuma conta encontrada com esse e-mail!",
                                    Snackbar.LENGTH_LONG).show();
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                        Snackbar.make(findViewById(android.R.id.content),
                                "Erro ao verificar o e-mail. Tente novamente.",
                                Snackbar.LENGTH_LONG).show();
                    }
                });
    }

    // Redireciona para tela Principal após login bem-sucedido
    public void abrirTelaPrincipal() {
        startActivity(new Intent(getApplicationContext(), PrincipalActivity.class));
        finish();
    }
}
