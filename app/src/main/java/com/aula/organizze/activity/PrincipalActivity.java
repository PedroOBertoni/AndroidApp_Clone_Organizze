package com.aula.organizze.activity;

import android.content.Intent;
import android.os.Bundle;
import androidx.core.content.ContextCompat;
import androidx.appcompat.app.AppCompatActivity;

import com.aula.organizze.R;
import com.leinardi.android.speeddial.SpeedDialActionItem;
import com.leinardi.android.speeddial.SpeedDialView;

public class PrincipalActivity extends AppCompatActivity {

    private SpeedDialView speedDialView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_principal);

        speedDialView = findViewById(R.id.speedDial);

        // Adiciona item "Despesa"
        speedDialView.addActionItem(
                new SpeedDialActionItem.Builder(R.id.fab_despesa, R.drawable.ic_add_24dp)
                        .setLabel("Adicionar despesa")
                        .setFabBackgroundColor(ContextCompat.getColor(this, R.color.colorButtonDespesa)) // ex: cor
                        .create()
        );

        // Adiciona item "Receita"
        speedDialView.addActionItem(
                new SpeedDialActionItem.Builder(R.id.fab_receita, R.drawable.ic_add_24dp)
                        .setLabel("Adicionar receita")
                        .setFabBackgroundColor(ContextCompat.getColor(this, R.color.colorButtonReceita)) // ex: cor
                        .create()
        );

        // Listener para clique nos itens
        speedDialView.setOnActionSelectedListener(new SpeedDialView.OnActionSelectedListener() {
            @Override
            public boolean onActionSelected(SpeedDialActionItem actionItem) {
                int id = actionItem.getId();
                if (id == R.id.fab_despesa) {
                    /* Abre a Activity/diálogo de adicionar despesa
                    startActivity(new Intent(PrincipalActivity.this, AddDespesaActivity.class)); */

                    // fecha o menu com animação e informa que já tratamos o fechamento
                    speedDialView.close();
                    return true; // true: impõe que já tratamos o fechamento (evita close sem animação)
                } else if (id == R.id.fab_receita) {
                    /* startActivity(new Intent(PrincipalActivity.this, AddReceitaActivity.class)); */
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
                // Se quiser tratar clique no FAB principal (além de abrir o menu)
                // return true para manter aberto, false para comportamento padrão.
                return false;
            }

            @Override
            public void onToggleChanged(boolean isOpen) {
                // isOpen == true quando abre; aqui dá pra animar toolbar, esconder outros controles etc.
            }
        });
    }
}
