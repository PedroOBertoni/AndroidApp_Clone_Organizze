package com.aula.organizze.activity;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.view.ContextThemeWrapper;
import androidx.core.content.ContextCompat;

import com.aula.organizze.R;
import com.aula.organizze.config.ConfigFirebase;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.firebase.auth.FirebaseAuth;
import com.leinardi.android.speeddial.SpeedDialActionItem;
import com.leinardi.android.speeddial.SpeedDialView;

public class PrincipalActivity extends AppCompatActivity {

    // Referência para o Speed Dial (FAB com múltiplas ações)
    private SpeedDialView speedDialView;

    // Referência para a autenticação do Firebase
    private FirebaseAuth autenticacao;

    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_principal);

        // Configura a autenticação
        autenticacao = FirebaseAuth.getInstance();

        // Configura a Toolbar
        MaterialToolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayShowTitleEnabled(false); // remove o nome do app
        getSupportActionBar().setDisplayHomeAsUpEnabled(false); // remove o ícone de navegação

        // Configura o FAB
        speedDialView = findViewById(R.id.speedDial);

        // Adicionando item ao Speed Dial
        speedDialView.addActionItem(
                new SpeedDialActionItem.Builder(R.id.fab_despesa, R.drawable.ic_add_24dp)
                        .setLabel("Adicionar despesa")
                        .setFabImageTintColor(ContextCompat.getColor(this, R.color.white))
                        .setFabBackgroundColor(ContextCompat.getColor(this, R.color.colorPrimaryDespesa))
                        .create()
        );

        // Adicionando item ao Speed Dial
        speedDialView.addActionItem(
                new SpeedDialActionItem.Builder(R.id.fab_receita, R.drawable.ic_add_24dp)
                        .setLabel("Adicionar receita")
                        .setFabImageTintColor(ContextCompat.getColor(this, R.color.white))
                        .setFabBackgroundColor(ContextCompat.getColor(this, R.color.colorPrimaryReceita))
                        .create()
        );

        // Configura o listener para os itens do Speed Dial
        speedDialView.setOnActionSelectedListener(actionItem -> {
            int id = actionItem.getId();
            if (id == R.id.fab_despesa) {
                startActivity(new Intent(PrincipalActivity.this, DespesasActivity.class));
                speedDialView.close();
                return true;
            } else if (id == R.id.fab_receita) {
                startActivity(new Intent(PrincipalActivity.this, ReceitasActivity.class));
                speedDialView.close();
                return true;
            }
            return false;
        });
    }

    // Infla o menu, adicionando itens à barra de ações
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_toolbar, menu);
        return true;
    }

    // Handle menu item selections
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.action_logout) {
            new MaterialAlertDialogBuilder(
                    new ContextThemeWrapper(this, R.style.RoundedAlertDialogTheme)
            )
                    .setTitle("Sair da conta")
                    .setMessage("Tem certeza que deseja sair?")
                    .setPositiveButton("SIM", (dialog, which) -> {
                        autenticacao.signOut();
                        Intent intent = new Intent(this, LoginActivity.class);
                        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                        startActivity(intent);
                        finish();
                    })
                    .setNegativeButton("CANCELAR", (dialog, which) -> dialog.dismiss())
                    .show();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
}
