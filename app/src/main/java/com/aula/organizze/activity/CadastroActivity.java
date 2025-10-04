package com.aula.organizze.activity;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.aula.organizze.R;
import com.aula.organizze.config.ConfigFirebase;
import com.aula.organizze.model.Usuario;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException;
import com.google.firebase.auth.FirebaseAuthUserCollisionException;
import com.google.firebase.auth.FirebaseAuthWeakPasswordException;

public class CadastroActivity extends AppCompatActivity {

    private EditText campoNome, campoEmail, campoSenha;
    private Button buttonCadastrar;
    private FirebaseAuth autenticacao;
    private Usuario usuario;

    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_cadastro);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.buttonCadastrar), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        // Recuperar componentes da interface pelo ID
        // EditText
        campoNome = findViewById(R.id.editNomeCadastro);
        campoEmail = findViewById(R.id.editEmailCadastro);
        campoSenha = findViewById(R.id.editSenhaCadastro);

        // Button
        buttonCadastrar = findViewById(R.id.buttonCadastrar);

        // Capturando apenas o texto dos campos
        String textoNome = campoNome.getText().toString();
        String textoEmail = campoEmail.getText().toString();
        String textoSenha = campoSenha.getText().toString();

        buttonCadastrar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                validarCadastroUsuario(textoNome, textoEmail, textoSenha);
                cadastrarUsuario();
            }

        });
    }

    public void validarCadastroUsuario(String nome, String email, String senha){

        if( !nome.isEmpty() ){ // Nome não pode estar vazio
            if( !email.isEmpty() ){ // Email não pode estar vazio
                if( !senha.isEmpty() || senha.length() >= 6 ){ // Senha deve ter no mínimo 6 caracteres

                    // Se estiver tudo preenchido, instanciar o objeto usuário e chamar o método para cadastrar
                    cadastrarUsuario();

                }else{
                    Toast.makeText(CadastroActivity.this,
                            "Preencha a senha, mínimo 6 caracteres!",
                            Toast.LENGTH_SHORT).show();

                }
            }else{
                Toast.makeText(CadastroActivity.this,
                        "Preencha o email!",
                        Toast.LENGTH_SHORT).show();

            }
        }else{
            Toast.makeText(CadastroActivity.this,
                    "Preencha o nome!",
                    Toast.LENGTH_SHORT).show();

        }

    }

    public void cadastrarUsuario(){
        // Instanciar o objeto usuário
        usuario = new Usuario();

        // Recuperar os dados dos campos e atribuir ao objeto usuário
        usuario.setNome(campoNome.getText().toString());
        usuario.setEmail(campoEmail.getText().toString());
        usuario.setSenha(campoSenha.getText().toString());

        // Cadastrar usuário no Firebase
        autenticacao = ConfigFirebase.getFirebaseAutenticacao();
        autenticacao.createUserWithEmailAndPassword(
                usuario.getEmail(), usuario.getSenha()

        ).addOnCompleteListener(this, task -> {;
            if(task.isSuccessful()){
                Toast.makeText(CadastroActivity.this,
                        "Usuário cadastrado com sucesso!",
                        Toast.LENGTH_SHORT).show();
                startActivity(new Intent(getApplicationContext(), MainActivity.class));
                finish();

            }else{ // Aqui iremos tratar as exceções
                String excecao = "";

                try{
                    throw task.getException(); // Lançar a exceção para tratar

                } catch( FirebaseAuthWeakPasswordException e ){ // Senha fraca
                    excecao = "Digite uma senha mais forte!";

                } catch( FirebaseAuthInvalidCredentialsException e ){ // Email inválido
                    excecao = "Por favor, digite um email válido!";

                } catch ( FirebaseAuthUserCollisionException e ){ // Email já cadastrado
                    excecao = "Esta conta já foi cadastrada!";

                } catch ( Exception e ){
                    excecao = "Erro ao cadastrar usuário: " + e.getMessage();
                    e.printStackTrace();

                }

                Toast.makeText(CadastroActivity.this,
                        excecao,
                        Toast.LENGTH_SHORT).show();

            }
        });
    }

    public void redirectTermosDeUso(View view){
        startActivity(new Intent(this, CadastroActivity.class));
    }

    public void redirectEntrar(View view){
        startActivity(new Intent(this, LoginActivity.class));
    }
}