package com.aula.organizze.activity;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.os.Looper;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import android.os.Handler;
import androidx.core.view.WindowInsetsCompat;

import com.aula.organizze.R;
import com.aula.organizze.config.ConfigFirebase;
import com.aula.organizze.model.Usuario;
import com.google.android.material.snackbar.Snackbar;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException;
import com.google.firebase.auth.FirebaseAuthUserCollisionException;
import com.google.firebase.auth.FirebaseAuthWeakPasswordException;

public class CadastroActivity extends AppCompatActivity {

    // componentes da interface
    private EditText campoNome, campoEmail, campoSenha;
    private TextInputLayout layoutNome, layoutEmail, layoutSenha;
    private Button buttonCadastra;

    // objeto para autenticação do Firebase
    private FirebaseAuth autenticacao;
    private Usuario usuario;

    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_cadastro);

        // Recuperar componentes da interface pelo ID
        // EditText
        campoNome = findViewById(R.id.editNomeCadastro);
        campoEmail = findViewById(R.id.editEmailCadastro);
        campoSenha = findViewById(R.id.editSenhaCadastro);

        // TextInputLayout
        layoutNome = findViewById(R.id.layoutNomeCadastro);
        layoutEmail = findViewById(R.id.layoutEmailCadastro);
        layoutSenha = findViewById(R.id.layoutSenhaCadastro);

        // Button
        buttonCadastra = findViewById(R.id.buttonCadastra);

        buttonCadastra.setOnClickListener(v -> {
            // Captura os textos no momento do clique
            String textoNome = campoNome.getText().toString().trim();
            String textoEmail = campoEmail.getText().toString().trim();
            String textoSenha = campoSenha.getText().toString().trim();

            validaPreenchimentoDosCampos(textoNome, textoEmail, textoSenha);
        });
    }

    public void validaPreenchimentoDosCampos(String nome, String email, String senha) {

        // Limpa erros anteriores
        layoutNome.setError(null);
        layoutEmail.setError(null);
        layoutSenha.setError(null);

        boolean valido = true;
        String excecao;

        if (nome.isEmpty()) {
            excecao = "Preencha o nome!";
            layoutNome.setError(excecao);

            Snackbar.make(findViewById(android.R.id.content),
                    excecao,
                    Snackbar.LENGTH_LONG).show();

            valido = false;
        }

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
            cadastrarUsuario(email, senha, nome);
        }
    }

    public void cadastrarUsuario(String email, String senha, String nome) {
        usuario = new Usuario();
        usuario.setNome(nome);
        usuario.setEmail(email);
        usuario.setSenha(senha);

        autenticacao = ConfigFirebase.getFirebaseAutenticacao();
        autenticacao.createUserWithEmailAndPassword(email, senha)
                .addOnCompleteListener(this, task -> {
                    if ( task.isSuccessful() ) {

                        abrirTelaLogin();

                    } else {
                        String excecao;

                        try {
                            throw task.getException();

                        } catch (FirebaseAuthWeakPasswordException e) {
                            excecao = "Digite uma senha mais forte!";
                            layoutSenha.setError(excecao);

                        } catch (FirebaseAuthInvalidCredentialsException e) {
                            excecao = "Por favor, digite um e-mail válido!";
                            layoutEmail.setError(excecao);

                        } catch (FirebaseAuthUserCollisionException e) {
                            excecao = "Esta conta já foi cadastrada!";
                            layoutEmail.setError(excecao);

                        } catch (Exception e) {
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

    public void redirectTermosDeUso(View view){
        startActivity(new Intent(this, CadastroActivity.class));
        finish();
    }

    public void redirectEntrar(View view){
        startActivity(new Intent(this, LoginActivity.class));
        finish();
    }

    public void abrirTelaLogin() {
        startActivity(new Intent(getApplicationContext(), LoginActivity.class));
        finish();
    }
}