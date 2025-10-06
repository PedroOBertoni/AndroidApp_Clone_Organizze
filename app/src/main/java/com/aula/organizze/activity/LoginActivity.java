package com.aula.organizze.activity;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

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
import com.google.firebase.auth.FirebaseAuthUserCollisionException;
import com.google.firebase.auth.FirebaseAuthWeakPasswordException;

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

        buttonEntra.setOnClickListener(v -> {
            // Captura os textos no momento do clique
            String textoEmail = campoEmail.getText().toString().trim();
            String textoSenha = campoSenha.getText().toString().trim();

            validaPreenchimentoDosCampos(textoEmail, textoSenha);
        });
    }

    public void validaPreenchimentoDosCampos(String email, String senha) {

        // Limpa erros anteriores
        layoutEmail.setError(null);
        layoutSenha.setError(null);

        boolean valido = true;
        String excecao;

        if (email.isEmpty()) {
            excecao = "Preencha o e-mail!";
            layoutEmail.setError(excecao);

            Snackbar.make(findViewById(android.R.id.content),
                    excecao,
                    Snackbar.LENGTH_LONG).show();

            valido = false;
        }

        if (senha.isEmpty()) {
            excecao = "Preencha a senha!";
            layoutSenha.setError(excecao);

            Snackbar.make(findViewById(android.R.id.content),
                    excecao,
                    Snackbar.LENGTH_LONG).show();

            valido = false;

        } else if (senha.length() < 6) {
            excecao = "A senha deve ter no mínimo 6 caracteres!";
            layoutSenha.setError(excecao);

            Snackbar.make(findViewById(android.R.id.content),
                    excecao,
                    Snackbar.LENGTH_LONG).show();

            valido = false;
        }

        if (valido) {
            validarLoginUsuario(email, senha);
        }
    }

    public void validarLoginUsuario(String email, String senha) {
        usuario = new Usuario();

        usuario.setEmail(email);
        usuario.setSenha(senha);

        autenticacao = ConfigFirebase.getFirebaseAutenticacao();
        autenticacao.signInWithEmailAndPassword(
                usuario.getEmail(),
                usuario.getSenha()
        ).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
            @Override
            public void onComplete(@NonNull Task<AuthResult> task) {
                if ( task.isSuccessful() ){

                    // Redireciona para a tela principal
                    abrirTelaPrincipal();

                } else {
                    String excecao;

                    try {
                        throw task.getException();

                    } catch (FirebaseAuthInvalidUserException e) {
                        excecao = "Usuário digitado não está cadastrado!";
                        layoutEmail.setError(excecao);

                    } catch (FirebaseAuthInvalidCredentialsException e) {
                        excecao = "E-mail e/ou senha não correspondem a um usuário cadastrado!";
                        layoutSenha.setError(excecao);

                    } catch (Exception e) {
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

    public void redirectTermosDeUso(View view){
        startActivity(new Intent(this, CadastroActivity.class));
        finish();
    }

    public void redirectCadastrar(View view){
        startActivity(new Intent(this, CadastroActivity.class));
        finish();
    }

    public void abrirTelaPrincipal() {
        startActivity(new Intent(getApplicationContext(), PrincipalActivity.class));
        finish();
    }
}