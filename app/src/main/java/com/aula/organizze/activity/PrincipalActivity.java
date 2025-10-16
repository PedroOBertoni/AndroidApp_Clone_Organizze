package com.aula.organizze.activity;;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;

import androidx.appcompat.app.AlertDialog;
import androidx.core.content.ContextCompat;
import androidx.appcompat.app.AppCompatActivity;

import com.aula.organizze.R;
import com.aula.organizze.config.ConfigFirebase;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.firebase.auth.FirebaseAuth;
import com.leinardi.android.speeddial.SpeedDialActionItem;
import com.leinardi.android.speeddial.SpeedDialView;

public class PrincipalActivity extends AppCompatActivity {

    private SpeedDialView speedDialView;
    private FirebaseAuth autenticacao;

    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_principal);

        // Inicializa o FirebaseAuth
        autenticacao = FirebaseAuth.getInstance();

        // Configura a Toolbar
        MaterialToolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayShowTitleEnabled(false);

        // Ícone de menu (três barras ou seta)
        toolbar.setNavigationOnClickListener(view -> {
            // Quando o ícone for clicado, abre o menu
            abrirMenuSair();
        });

        // Configura o FAB (Floating Action Button) com menu de opções
        speedDialView = findViewById(R.id.speedDial);

        // Adiciona item "Despesa" ao FAB
        speedDialView.addActionItem(
                new SpeedDialActionItem.Builder(R.id.fab_despesa, R.drawable.ic_add_24dp)
                        .setLabel("Adicionar despesa")
                        .setFabImageTintColor(ContextCompat.getColor(this, R.color.white))
                        .setFabBackgroundColor(ContextCompat.getColor(this, R.color.colorPrimaryDespesa))
                        .create()
        );

        // Adiciona item "Receita" ao FAB
        speedDialView.addActionItem(
                new SpeedDialActionItem.Builder(R.id.fab_receita, R.drawable.ic_add_24dp)
                        .setLabel("Adicionar receita")
                        .setFabImageTintColor(ContextCompat.getColor(this, R.color.white))
                        .setFabBackgroundColor(ContextCompat.getColor(this, R.color.colorPrimaryReceita))
                        .create()
        );

        // Listener para clique nos itens do FAB
        speedDialView.setOnActionSelectedListener(new SpeedDialView.OnActionSelectedListener() {
            @Override
            public boolean onActionSelected(SpeedDialActionItem actionItem) {
                int id = actionItem.getId();
                if (id == R.id.fab_despesa) {
                    // Abre a Activity de adicionar despesa
                    startActivity(new Intent(PrincipalActivity.this, DespesasActivity.class));

                    // fecha o menu com animação e informa que já tratamos o fechamento
                    speedDialView.close();
                    return true; // true: impõe que já tratamos o fechamento (evita close sem animação)

                } else if (id == R.id.fab_receita) {
                    // Abre a Activity de adicionar receita
                    startActivity(new Intent(PrincipalActivity.this, ReceitasActivity.class));
                    speedDialView.close();
                    return true;

                }
                return false;
            }
        });

        // Opcional: escutar mudança de estado (open/close)
        speedDialView.setOnChangeListener(new SpeedDialView.OnChangeListener() {
            @Override
            public boolean onMainActionSelected() {
                return false;
            }

            @Override
            public void onToggleChanged(boolean isOpen) {
            }
        });
    }

    private void abrirMenuSair() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Sair da conta");
        builder.setMessage("Deseja realmente sair da conta?");
        builder.setCancelable(false);

        builder.setPositiveButton("Sim", (dialog, which) -> {
            autenticacao = ConfigFirebase.getFirebaseAutenticacao();

            autenticacao.signOut();
            // Redireciona para a tela de login, por exemplo:
            Intent intent = new Intent(getApplicationContext(), LoginActivity.class);
            startActivity(intent);
            finish();
        });

        builder.setNegativeButton("Cancelar", (dialog, which) -> dialog.dismiss());

        builder.create().show();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_toolbar, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.action_logout) {
            new AlertDialog.Builder(this)
                    .setTitle("Sair da conta")
                    .setMessage("Tem certeza que deseja sair?")
                    .setPositiveButton("Sim", (dialog, which) -> {
                        autenticacao.signOut();
                        Intent intent = new Intent(this, LoginActivity.class);
                        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                        startActivity(intent);
                        finish();
                    })
                    .setNegativeButton("Cancelar", (dialog, which) -> dialog.dismiss())
                    .show();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

}
