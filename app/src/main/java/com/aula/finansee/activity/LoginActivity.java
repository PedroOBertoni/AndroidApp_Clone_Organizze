package com.aula.finansee.activity;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.graphics.drawable.GradientDrawable;
import android.os.Bundle;
import android.text.InputType;
import android.util.Patterns;
import android.util.TypedValue;
import android.view.View;
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
import com.aula.finansee.utils.EmailSender;
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

import java.util.Random;

public class LoginActivity extends AppCompatActivity {

    // componentes da interface
    private TextView resetaSenha;
    private EditText campoEmail, campoSenha;
    private TextInputLayout layoutEmail, layoutSenha;
    private Button buttonEntra;

    // objeto para autenticação do Firebase
    private FirebaseAuth autenticacao;

    // codigo para a redefinição de senha
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

        // Recupera os componentes da interface pelo ID
        // TextView
        resetaSenha = findViewById(R.id.resetaSenha);

        // EditText
        campoEmail = findViewById(R.id.editNovaSenha);
        campoSenha = findViewById(R.id.editConfirmarSenha);

        // TextInputLayout
        layoutEmail = findViewById(R.id.inputSenha);
        layoutSenha = findViewById(R.id.inputConfirmarSenha);

        // Button
        buttonEntra = findViewById(R.id.buttonEntra);

        // Listener do botão de login
        buttonEntra.setOnClickListener(v -> {
            // Captura os textos no momento do clique
            String textoEmail = campoEmail.getText().toString().trim();
            String textoSenha = campoSenha.getText().toString().trim();

            // Valida os campos antes de enviar
            validaPreenchimentoDosCampos(textoEmail, textoSenha);
        });

        resetaSenha.setOnClickListener(v -> {
            // chama o método para redefinição de senha
            exibirDialogRedefinirSenha();
        });

        /* Redirecionamento para outras páginas */
        TextView linkTermosDeUso = findViewById(R.id.linkTermosDeUso);
        TextView linkCadastro = findViewById(R.id.linkCadastro);

        linkTermosDeUso.setOnClickListener(v -> {
            Intent intent = new Intent(LoginActivity.this, TermosDeUsoActivity.class);
            startActivity(intent);
        });

        linkCadastro.setOnClickListener(v -> {
            Intent intent = new Intent(LoginActivity.this, CadastroActivity.class);
            startActivity(intent);
        });
    }

    public void validaPreenchimentoDosCampos(String email, String senha) {

        // Limpa erros anteriores
        layoutEmail.setError(null);
        layoutSenha.setError(null);

        // Variáveis para controle de validação e exceção
        boolean valido = true;
        String excecao;

        if (email.isEmpty()) {
            // Validação de campo de e-mail vazio
            excecao = "Preencha o e-mail!";
            layoutEmail.setError(excecao);

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
            layoutSenha.setError(excecao);

            // Mostra erro geral com Snackbar
            Snackbar.make(findViewById(android.R.id.content),
                    excecao,
                    Snackbar.LENGTH_LONG).show();

            // e define válido como falso
            valido = false;

        } else if (senha.length() < 6) {
            // Validação de senha com no mínimo 6 caracteres
            excecao = "A senha deve ter no mínimo 6 caracteres!";
            layoutSenha.setError(excecao);

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
                        layoutEmail.setError(excecao);

                    } catch (FirebaseAuthInvalidCredentialsException e) {
                        // E-mail e/ou senha inválidos
                        excecao = "E-mail e/ou senha não correspondem a um usuário cadastrado!";
                        layoutSenha.setError(excecao);

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

        GradientDrawable border = new GradientDrawable();
        border.setCornerRadius(16);
        border.setStroke(2, ContextCompat.getColor(this, R.color.textGray));
        border.setColor(ContextCompat.getColor(this, R.color.colorBackgroundDialog));
        inputEmail.setBackground(border);

        LinearLayout container = new LinearLayout(this);
        container.setOrientation(LinearLayout.VERTICAL);
        int margin = (int) TypedValue.applyDimension(
                TypedValue.COMPLEX_UNIT_DIP, 16, getResources().getDisplayMetrics()
        );
        container.setPadding(margin, 0, margin, 0);
        container.addView(inputEmail);

        builder.setView(container);

        builder.setPositiveButton("ENVIAR", (dialog, which) -> {
            String email = inputEmail.getText().toString().trim();

            if (email.isEmpty()) {
                Snackbar.make(findViewById(android.R.id.content),
                        "Informe o email!", Snackbar.LENGTH_LONG).show();
                return;
            }

            if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
                Snackbar.make(findViewById(android.R.id.content),
                        "Digite um e-mail válido!", Snackbar.LENGTH_LONG).show();
                return;
            }

            // escolha de expiração: 3 ou 5 minutos — aqui escolhi 3 por padrão
            int minutosExpiracao = 3; // <-- altere para 5 se preferir

            // gera e marca expiração + atualiza last-send
            gerarENotarCodigoComExpiracao(minutosExpiracao);

            // monta corpo do email (seu método EmailSender usa apenas email+codigo; eu recomendo passar corpo/assunto lá)
            // aqui chamamos seu método que envia o email em background
            enviarEmailRedefinicaoPersonalizado(email, codigoAtual);
        });

        builder.setNegativeButton("CANCELAR", (dialog, which) -> dialog.dismiss());
        builder.show();
    }

    private void enviarEmailRedefinicaoPersonalizado(String email, String codigo) {
        new Thread(() -> {
            try {
                // Define assunto e corpo do email com HTML (negrito e cores opcionais)
                String assunto = "Redefinição de senha - FinanSee";
                String corpo = "<html>" +
                        "<body style='font-family: sans-serif;'>" +
                        "<h2 style='color:#008cff;'>Olá!</h2>" +
                        "<p>Seu código para redefinir a senha é:</p>" +
                        "<h1 style='color:#008cff;'><b>" + codigo + "</b></h1>" +
                        "<p>Este código expira em <b>5 minutos</b>.</p>" +
                        "<p>Equipe <b>FinanSee</b></p>" +
                        "</body></html>";

                // Envia o email (agora o método é estático)
                EmailSender.enviarEmail(email, assunto, corpo);

                // Como o envio ocorre em thread separada, podemos assumir sucesso se não lançar exceção
                runOnUiThread(() -> {
                    Snackbar.make(findViewById(android.R.id.content),
                            "Email enviado com sucesso!",
                            Snackbar.LENGTH_LONG).show();
                    exibirDialogCodigo(email);
                });

            } catch (Exception e) {
                runOnUiThread(() -> Snackbar.make(findViewById(android.R.id.content),
                        "Falha ao enviar o email!",
                        Snackbar.LENGTH_LONG).show());
                e.printStackTrace();
            }
        }).start();
    }


    private void exibirDialogCodigo(final String email) {
        MaterialAlertDialogBuilder builder = new MaterialAlertDialogBuilder(
                new ContextThemeWrapper(this, R.style.RoundedAlertDialogTheme)
        );
        builder.setTitle("Digite o código enviado ao seu e-mail");

        EditText inputCodigo = new EditText(this);
        inputCodigo.setHint("Código de 6 dígitos");
        inputCodigo.setInputType(InputType.TYPE_CLASS_NUMBER);
        inputCodigo.setPadding(32, 24, 32, 24);

        GradientDrawable border = new GradientDrawable();
        border.setCornerRadius(16);
        border.setStroke(2, ContextCompat.getColor(this, R.color.textGray));
        border.setColor(ContextCompat.getColor(this, R.color.colorBackgroundDialog));
        inputCodigo.setBackground(border);

        LinearLayout container = new LinearLayout(this);
        container.setOrientation(LinearLayout.VERTICAL);
        int margin = (int) TypedValue.applyDimension(
                TypedValue.COMPLEX_UNIT_DIP, 16, getResources().getDisplayMetrics()
        );
        container.setPadding(margin, 0, margin, 0);
        container.addView(inputCodigo);

        builder.setView(container);

        builder.setPositiveButton("CONFIRMAR", (dialog, which) -> {
            String codigoDigitado = inputCodigo.getText().toString().trim();

            // checa expiração
            if (System.currentTimeMillis() > codigoExpiracaoMillis) {
                Snackbar.make(findViewById(android.R.id.content),
                        "Código expirado. Solicite um novo.", Snackbar.LENGTH_LONG).show();
                return;
            }

            if (isCodigoValido(codigoDigitado)) {
                // Código correto → abre activity para redefinir senha
                Intent intent = new Intent(this, RedefinirSenhaActivity.class);
                intent.putExtra("email", email);
                startActivity(intent);
            } else {
                Snackbar.make(findViewById(android.R.id.content),
                        "Código incorreto!", Snackbar.LENGTH_LONG).show();
            }
        });

        builder.setNegativeButton("CANCELAR", (dialog, which) -> dialog.dismiss());

        // Reenviar código com cooldown
        builder.setNeutralButton("REENVIAR CÓDIGO", (dialog, which) -> {
            long now = System.currentTimeMillis();
            if (now - ultimoEnvioMillis < RELOAD_COOLDOWN_MS) {
                long waitSec = (RELOAD_COOLDOWN_MS - (now - ultimoEnvioMillis)) / 1000;
                Snackbar.make(findViewById(android.R.id.content),
                        "Aguarde " + waitSec + "s antes de reenviar.", Snackbar.LENGTH_LONG).show();
                return;
            }

            // gera novo código e atualiza expiração (mesmo tempo que foi usado no envio inicial)
            int minutosExpiracao = 3; // mantenha mesmo valor usado antes (ou armazene a preferência)
            gerarENotarCodigoComExpiracao(minutosExpiracao);

            // envia novamente
            enviarEmailRedefinicaoPersonalizado(email, codigoAtual);

            Snackbar.make(findViewById(android.R.id.content),
                    "Código reenviado.", Snackbar.LENGTH_LONG).show();
        });

        builder.show();
    }

    /* Código de Verificação para resetar senha */

    // Método que gera um código aleatório de 6 digitos para resetar a senha
    private String gerarCodigo6Digitos() {
        int codigo = 100000 + new Random().nextInt(900000);
        return String.valueOf(codigo);
    }

    private void gerarENotarCodigoComExpiracao(int minutosExpiracao) {
        codigoAtual = gerarCodigo6Digitos();
        codigoExpiracaoMillis = System.currentTimeMillis() + minutosExpiracao * 60_000L;
        ultimoEnvioMillis = System.currentTimeMillis();
    }

    // Método que valida se código é valido
    private boolean isCodigoValido(String codigoDigitado) {
        if (codigoAtual == null || codigoAtual.isEmpty()) return false;
        if (codigoDigitado == null) return false;
        long now = System.currentTimeMillis();
        if (now > codigoExpiracaoMillis) {
            return false; // expirou
        }
        return codigoAtual.equals(codigoDigitado);
    }


    // Redireciona para as páginas Termos de Uso
    public void redirectTermosDeUso(View view) {
        startActivity(new Intent(this, TermosDeUsoActivity.class));
        finish();
    }

    // Redireciona para tela de Cadastro
    public void redirectCadastrar(View view) {
        startActivity(new Intent(this, CadastroActivity.class));
        finish();
    }

    // Redireciona para tela Principal após login bem-sucedido
    public void abrirTelaPrincipal() {
        startActivity(new Intent(getApplicationContext(), PrincipalActivity.class));
        finish();
    }
}
