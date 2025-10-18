package com.aula.organizze.activity;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.Window;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.view.ContextThemeWrapper;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.aula.organizze.R;
import com.aula.organizze.adapter.AdapterMovimentacao;
import com.aula.organizze.config.ConfigFirebase;
import com.aula.organizze.model.Movimentacao;
import com.google.android.material.appbar.AppBarLayout;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.ValueEventListener;
import com.leinardi.android.speeddial.SpeedDialActionItem;
import com.leinardi.android.speeddial.SpeedDialView;
import com.prolificinteractive.materialcalendarview.CalendarDay;
import com.prolificinteractive.materialcalendarview.MaterialCalendarView;
import com.prolificinteractive.materialcalendarview.OnMonthChangedListener;

import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;

public class PrincipalActivity extends AppCompatActivity {

    // Componentes
    private LinearLayout layoutHeaderSaldo;
    private TextView textSaldo, textTotalDespesas, textTotalReceitas;
    private RecyclerView recyclerMovimentos;

    // Adapter
    private AdapterMovimentacao adapterMovimentacao;
    private ArrayList<Movimentacao> movimentacoes = new ArrayList<>();

    // Firebase
    private DatabaseReference databaseReference;
    private FirebaseAuth autenticacao;
    private DatabaseReference usuarioRef;

    // Listeners
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

        // Configurações iniciais Firebase
        autenticacao = FirebaseAuth.getInstance();
        databaseReference = ConfigFirebase.getFirebaseDatabase();

        // Inicializa componentes
        layoutHeaderSaldo = findViewById(R.id.layoutHeaderSaldo);
        textSaldo = findViewById(R.id.textSaldo);
        textTotalDespesas = findViewById(R.id.textTotalDespesas);
        textTotalReceitas = findViewById(R.id.textTotalReceitas);

        // Inicializa Recycler View
        recyclerMovimentos = findViewById(R.id.recyclerMovimentacoes);

        // Configura Toolbar
        toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayShowTitleEnabled(false);
        getSupportActionBar().setDisplayHomeAsUpEnabled(false);

        // Configura FAB (SpeedDial)
        speedDialView = findViewById(R.id.speedDial);

        // Adiciona botão de Adicionar Despesa ao SpeedDial
        speedDialView.addActionItem(
                new SpeedDialActionItem.Builder(R.id.fab_despesa, R.drawable.ic_add_24dp)
                        .setLabel("Adicionar despesa")
                        .setFabImageTintColor(ContextCompat.getColor(this, R.color.white))
                        .setFabBackgroundColor(ContextCompat.getColor(this, R.color.colorPrimaryDespesa))
                        .create()
        );

        // Adiciona botão de Adicionar Receita ao SpeedDial
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

        // Configura calendário
        MaterialCalendarView calendarView = findViewById(R.id.calendarView);

        // Define mês atual
        Calendar cal = Calendar.getInstance();
        SimpleDateFormat sdf = new SimpleDateFormat("MM-yyyy");
        mesAnoSelecionado = sdf.format(cal.getTime());

        // Listener para detectar mudança de mês
        calendarView.setOnMonthChangedListener(new OnMonthChangedListener() {
            @Override
            public void onMonthChanged(MaterialCalendarView widget, CalendarDay date) {
                // Converte CalendarDay para java.util.Calendar
                int day = date.getDay();
                int month = date.getMonth(); // mês base 1
                int year = date.getYear();

                //  Instancia o callendario e define o ano/mês selecionado
                Calendar cal = Calendar.getInstance();
                cal.set(Calendar.YEAR, year);
                cal.set(Calendar.MONTH, month - 1); // java Calendar é 0-based
                cal.set(Calendar.DAY_OF_MONTH, day);

                // Formata mês/ano
                SimpleDateFormat sdf = new SimpleDateFormat("MM-yyyy");
                mesAnoSelecionado = sdf.format(cal.getTime());

                // Remove listener antigo (opcional)
                if (valueEventListenerMovimentacoes != null) {
                    // Recupera UID do usuário logado
                    String uidUsuario = autenticacao.getCurrentUser().getUid();

                    // Remove listener de movimentações do mês anterior
                    DatabaseReference movRef = databaseReference
                            .child("movimentacoes")
                            .child(uidUsuario)
                            .child(mesAnoSelecionado);
                    movRef.removeEventListener(valueEventListenerMovimentacoes);
                }

                // Recarrega RecyclerView com novo mês
                recuperarMovimentacoes();
            }
        });

    }

    @Override
    protected void onStart() {
        super.onStart();
        // Recarrega resumo e movimentações ao iniciar a activity
        recuperarResumo();
        recuperarMovimentacoes();
    }

    @Override
    protected void onStop() {
        super.onStop();
        if (usuarioRef != null && valueEventListenerUsuario != null)
            // Remove listener de usuário
            usuarioRef.removeEventListener(valueEventListenerUsuario);

        if (valueEventListenerMovimentacoes != null) {
            // Recupera UID do usuário logado
            String uidUsuario = autenticacao.getCurrentUser().getUid();

            // Remove listener de movimentações
            DatabaseReference movimentacaoRef = databaseReference.child("movimentacoes").child(uidUsuario).child(mesAnoSelecionado);
            movimentacaoRef.removeEventListener(valueEventListenerMovimentacoes);
        }
    }

    private void recuperarResumo() {
        try {
            // Recupera UID do usuário logado
            String uidUsuario = autenticacao.getCurrentUser().getUid();
            usuarioRef = databaseReference.child("usuarios").child(uidUsuario);

            if (autenticacao.getCurrentUser() == null) {
                // Usuário não autenticado, redireciona para a tela de login e encerrar a activity atual
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
                    // Log detalhado do erro
                    Log.e("FIREBASE", "Erro ao carregar dados do usuário: " + error.getMessage(), error.toException());
                    Toast.makeText(PrincipalActivity.this, "Erro ao carregar dados do usuário", Toast.LENGTH_SHORT).show();
                }
            });
        } catch (Exception e) {
            Log.e("FIREBASE", "Erro inesperado ao configurar listener de usuário", e);
        }
    }

    private void recuperarMovimentacoes() {
        // Recupera UID do usuário logado
        String uidUsuario = autenticacao.getCurrentUser().getUid();

        // Corrigido: agora lê as movimentações dentro de cada mês ("movimentacoes/uid/10-2025")
        DatabaseReference movRef = databaseReference
                .child("movimentacoes")
                .child(uidUsuario)
                .child(mesAnoSelecionado);

        valueEventListenerMovimentacoes = movRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                // Limpa lista antes de adicionar novos dados
                movimentacoes.clear();

                for (DataSnapshot ds : snapshot.getChildren()) {
                    // Recupera movimentação
                    Movimentacao movimentacao = ds.getValue(Movimentacao.class);
                    if (movimentacao != null) {
                        // Define o ID da movimentação
                        movimentacao.setId(ds.getKey());
                        movimentacoes.add(movimentacao);
                    }
                }

                // Notifica o adapter sobre a mudança de dados e chama o cálculo do resumo
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
        // Zera totais antes do cálculo
        double totalReceitas = 0.0;
        double totalDespesas = 0.0;

        // Calcula total de receitas e despesas
        for (Movimentacao m : movimentacoes) {
            if ("R".equals(m.getTipo())) {
                totalReceitas += m.getValor();
            } else if ("D".equals(m.getTipo())) {
                totalDespesas += m.getValor();
            }
        }

        // Calcula saldo
        double saldo = totalReceitas - totalDespesas;
        resumoUsuario = saldo;

        // Atualiza interface
        formatandoSaldoTotalReceitasTotalDespesas(saldo, totalDespesas, totalReceitas);
        alterarCorCabecalhoSaldo(saldo);
    }

    public void formatandoSaldoTotalReceitasTotalDespesas(double saldo, double totalDespesas, double totalReceitas) {
        // Definindo formatação
        DecimalFormat df = new DecimalFormat("###,###,##0.00");

        // Aplicando formatação no saldo no totalDespesas e totalReceitas
        if(saldo < 0){
            saldo *= -1; // Inverte o sinal pois ele será adicionado antes do R$
            textSaldo.setText("- R$ " + df.format(saldo));
        } else{
            textSaldo.setText("R$ " + df.format(saldo));
        }

        // Formata total de despesas e receitas
        textTotalDespesas.setText("- R$ " + df.format(totalDespesas));
        textTotalReceitas.setText("+ R$ " + df.format(totalReceitas));
    }

    @SuppressLint("ResourceAsColor")
    public void alterarCorCabecalhoSaldo( double saldo ) {

        // Altera cor do cabeçalho e toolbar conforme saldo
        int cor = (saldo < 0)
                ? ContextCompat.getColor(this, R.color.colorPrimaryDespesa)
                : ContextCompat.getColor(this, R.color.colorPrimary);
        toolbar.setBackground(new ColorDrawable(cor));
        layoutHeaderSaldo.setBackground(new ColorDrawable(cor));

        // Altera também a cor do AppBarLayout se existir
        AppBarLayout appBarLayout = findViewById(R.id.appBarLayout);
        if (appBarLayout != null) {
            appBarLayout.setBackgroundColor(cor);
        }

        // Altera cor da status bar conforme saldo
        Window window = getWindow();
        window.setStatusBarColor(cor);
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
            // Confirmação de logout por meio de um AlertDialog
            new MaterialAlertDialogBuilder(
                    new ContextThemeWrapper(this, R.style.RoundedAlertDialogTheme)
            )
                    .setTitle("Sair da conta")
                    .setMessage("Tem certeza que deseja sair?")
                    .setPositiveButton("SIM", (dialog, which) -> {
                        // Se clicar em sim faz logout
                        autenticacao.signOut();

                        // Depois redireciona para a tela de login
                        Intent intent = new Intent(this, LoginActivity.class);
                        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                        startActivity(intent);

                        // E por fim encerra a activity atual
                        finish();
                    })
                    .setNegativeButton("CANCELAR", (dialog, which) -> dialog.dismiss())
                    .show();
            return true;
        }
        // Caso não seja logout, processa normalmente
        return super.onOptionsItemSelected(item);
    }
}
