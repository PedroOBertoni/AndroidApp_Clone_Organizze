package com.aula.finansee.activity;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;

import com.aula.finansee.R;
import com.aula.finansee.config.ConfigFirebase;
import com.aula.finansee.model.Usuario;
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

            // Chama o método de validação do preenchimento dos campos
            validaPreenchimentoDosCampos(textoNome, textoEmail, textoSenha);
        });
    }

    public void validaPreenchimentoDosCampos(String nome, String email, String senha) {

        // Limpa erros anteriores
        layoutNome.setError(null);
        layoutEmail.setError(null);
        layoutSenha.setError(null);

        // Variáveis para controle de validação e exceção
        boolean valido = true;
        String excecao;

        if (nome.isEmpty()) {
            // Validação de campo de nome vazio
            excecao = "Preencha o nome!";
            layoutNome.setError(excecao);

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

        if (valido) {
            cadastrarUsuario(email, senha, nome);
        }
    }

    public void cadastrarUsuario(String email, String senha, String nome) {
        // Criando objeto usuário
        usuario = new Usuario();

        // Definindo seu nome e email
        usuario.setNome(nome);
        usuario.setEmail(email);

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
                            layoutSenha.setError(excecao);

                        } catch (FirebaseAuthInvalidCredentialsException e) {
                            // E-mail inválido
                            excecao = "Por favor, digite um e-mail válido!";
                            layoutEmail.setError(excecao);

                        } catch (FirebaseAuthUserCollisionException e) {
                            // E-mail já cadastrado
                            excecao = "Esta conta já foi cadastrada!";
                            layoutEmail.setError(excecao);

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
        startActivity(new Intent(this, CadastroActivity.class));
        finish();
    }

    // Redireciona para a tela de Login
    public void redirectEntrar(View view){
        startActivity(new Intent(this, LoginActivity.class));
        finish();
    }
}