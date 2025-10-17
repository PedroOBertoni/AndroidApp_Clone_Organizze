package com.aula.organizze.activity;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.view.ContextThemeWrapper;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.aula.organizze.R;
import com.aula.organizze.adapter.AdapterMovimentacao;
import com.aula.organizze.config.ConfigFirebase;
import com.aula.organizze.model.Movimentacao;
import com.aula.organizze.model.Recorrencia;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.ValueEventListener;
import com.leinardi.android.speeddial.SpeedDialActionItem;
import com.leinardi.android.speeddial.SpeedDialView;

import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;

public class PrincipalActivity extends AppCompatActivity {

    private TextView textSaldo, textTotalDespesas, textTotalReceitas;
    private RecyclerView recyclerMovimentos;

    private AdapterMovimentacao adapterMovimentacao;
    private ArrayList<Movimentacao> movimentacoes = new ArrayList<>();

    private DatabaseReference databaseReference;
    private FirebaseAuth autenticacao;
    private DatabaseReference usuarioRef;
    private ValueEventListener valueEventListenerUsuario;
    private ValueEventListener valueEventListenerMovimentacoes;

    private double resumoUsuario = 0.0;
    private String mesAnoSelecionado;

    private SpeedDialView speedDialView;
    private MaterialToolbar toolbar;

    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_principal);

        autenticacao = FirebaseAuth.getInstance();
        databaseReference = ConfigFirebase.getFirebaseDatabase();

        textSaldo = findViewById(R.id.textSaldo);
        textTotalDespesas = findViewById(R.id.textTotalDespesas);
        textTotalReceitas = findViewById(R.id.textTotalReceitas);
        recyclerMovimentos = findViewById(R.id.recyclerMovimentacoes);

        // Configura Toolbar
        toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayShowTitleEnabled(false);
        getSupportActionBar().setDisplayHomeAsUpEnabled(false);

        // Configura FAB (SpeedDial)
        speedDialView = findViewById(R.id.speedDial);

        speedDialView.addActionItem(
                new SpeedDialActionItem.Builder(R.id.fab_despesa, R.drawable.ic_add_24dp)
                        .setLabel("Adicionar despesa")
                        .setFabImageTintColor(ContextCompat.getColor(this, R.color.white))
                        .setFabBackgroundColor(ContextCompat.getColor(this, R.color.colorPrimaryDespesa))
                        .create()
        );

        speedDialView.addActionItem(
                new SpeedDialActionItem.Builder(R.id.fab_receita, R.drawable.ic_add_24dp)
                        .setLabel("Adicionar receita")
                        .setFabImageTintColor(ContextCompat.getColor(this, R.color.white))
                        .setFabBackgroundColor(ContextCompat.getColor(this, R.color.colorPrimaryReceita))
                        .create()
        );

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

        // Configura RecyclerView
        adapterMovimentacao = new AdapterMovimentacao(movimentacoes, this);
        recyclerMovimentos.setLayoutManager(new LinearLayoutManager(this));
        recyclerMovimentos.setHasFixedSize(true);
        recyclerMovimentos.setAdapter(adapterMovimentacao);

        // Define mês atual (ex: 10-2025)
        Calendar cal = Calendar.getInstance();
        SimpleDateFormat sdf = new SimpleDateFormat("MM-yyyy");
        mesAnoSelecionado = sdf.format(cal.getTime());
    }

    @Override
    protected void onStart() {
        super.onStart();
        recuperarResumo();
        recuperarMovimentacoes();
    }

    @Override
    protected void onStop() {
        super.onStop();
        if (usuarioRef != null && valueEventListenerUsuario != null)
            usuarioRef.removeEventListener(valueEventListenerUsuario);

        if (valueEventListenerMovimentacoes != null) {
            String uidUsuario = autenticacao.getCurrentUser().getUid();
            DatabaseReference movimentacaoRef = databaseReference.child("movimentacoes").child(uidUsuario).child(mesAnoSelecionado);
            movimentacaoRef.removeEventListener(valueEventListenerMovimentacoes);
        }
    }

    private void recuperarResumo() {
        try {
            String uidUsuario = autenticacao.getCurrentUser().getUid();
            usuarioRef = databaseReference.child("usuarios").child(uidUsuario);

            if (autenticacao.getCurrentUser() == null) {
                Toast.makeText(this, "Sessão expirada. Faça login novamente.", Toast.LENGTH_SHORT).show();
                startActivity(new Intent(this, LoginActivity.class));
                finish();
                return;
            }

            valueEventListenerUsuario = usuarioRef.addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    if (snapshot.exists()) {
                        // Aqui futuramente você pode recuperar saldo, receitas, etc.
                    } else {
                        Log.w("FIREBASE", "Nenhum dado encontrado para o usuário: " + uidUsuario);
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {
                    Log.e("FIREBASE", "Erro ao carregar dados do usuário: " + error.getMessage(), error.toException());
                    Toast.makeText(PrincipalActivity.this, "Erro ao carregar dados do usuário", Toast.LENGTH_SHORT).show();
                }
            });
        } catch (Exception e) {
            Log.e("FIREBASE", "Erro inesperado ao configurar listener de usuário", e);
        }
    }

    private void recuperarMovimentacoes() {
        String uidUsuario = autenticacao.getCurrentUser().getUid();

        // Corrigido: agora lê as movimentações dentro de cada mês ("movimentacoes/uid/10-2025")
        DatabaseReference movRef = databaseReference
                .child("movimentacoes")
                .child(uidUsuario)
                .child(mesAnoSelecionado);

        valueEventListenerMovimentacoes = movRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                movimentacoes.clear();

                for (DataSnapshot ds : snapshot.getChildren()) {
                    Movimentacao movimentacao = ds.getValue(Movimentacao.class);
                    if (movimentacao != null) {
                        movimentacao.setId(ds.getKey());
                        movimentacoes.add(movimentacao);
                    }
                }

                adapterMovimentacao.notifyDataSetChanged();
                calcularResumo();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(PrincipalActivity.this, "Erro ao carregar movimentações", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void calcularResumo() {
        double totalReceitas = 0.0;
        double totalDespesas = 0.0;

        for (Movimentacao m : movimentacoes) {
            if ("R".equals(m.getTipo())) {
                totalReceitas += m.getValor();
            } else if ("D".equals(m.getTipo())) {
                totalDespesas += m.getValor();
            }
        }

        double saldo = totalReceitas - totalDespesas;
        resumoUsuario = saldo;

        // Definindo formatação
        DecimalFormat df = new DecimalFormat("###,###,##0.00");

        // Aplicando formatação no saldo, totalDespesas e totalReceitas
        textSaldo.setText("R$ " + df.format(saldo));
        textTotalDespesas.setText("- R$ " + df.format(totalDespesas));
        textTotalReceitas.setText("+ R$ " + df.format(totalReceitas));

        // Altera cor do cabeçalho conforme saldo
        int cor = (saldo < 0)
                ? ContextCompat.getColor(this, R.color.colorPrimaryDespesa)
                : ContextCompat.getColor(this, R.color.colorPrimary);
        toolbar.setBackground(new ColorDrawable(cor));
    }

    /* Menu Toolbar */
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_toolbar, menu);
        return true;
    }

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
