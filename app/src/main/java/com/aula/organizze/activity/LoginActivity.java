package com.aula.organizze.activity;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.aula.organizze.R;
import com.aula.organizze.config.ConfigFirebase;
import com.aula.organizze.model.Usuario;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.snackbar.Snackbar;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException;
import com.google.firebase.auth.FirebaseAuthInvalidUserException;

public class LoginActivity extends AppCompatActivity {

    // componentes da interface
    private EditText campoEmail, campoSenha;
    private TextInputLayout layoutEmail, layoutSenha;
    private Button buttonEntra;

    // objeto para autenticação do Firebase
    private FirebaseAuth autenticacao;
    private Usuario usuario;

    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_login);

        // Recuperar componentes da interface pelo ID
        // EditText
        campoEmail = findViewById(R.id.editEmailLogin);
        campoSenha = findViewById(R.id.editSenhaLogin);

        // TextInputLayout
        layoutEmail = findViewById(R.id.layoutEmailLogin);
        layoutSenha = findViewById(R.id.layoutSenhaLogin);

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

    // Redireciona para as páginas Termos de Uso
    public void redirectTermosDeUso(View view) {
        startActivity(new Intent(this, CadastroActivity.class));
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
