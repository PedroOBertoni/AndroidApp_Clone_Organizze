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
import com.aula.organizze.model.Recorrencia;
import com.google.android.material.appbar.AppBarLayout;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.snackbar.Snackbar;
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

import org.threeten.bp.LocalDate;
import org.threeten.bp.YearMonth;
import org.threeten.bp.format.DateTimeFormatter;
import org.threeten.bp.format.DateTimeParseException;
import org.threeten.bp.temporal.ChronoUnit;

import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;
import java.util.Locale;

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

    private double saldoUsuario = 0.0;
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

        // Ordena as movimentações por valor absoluto (decrescente)
        Collections.sort(movimentacoes, new Comparator<Movimentacao>() {
            @Override
            public int compare(Movimentacao m1, Movimentacao m2) {
                double valor1 = Math.abs(m1.getValor());
                double valor2 = Math.abs(m2.getValor());
                // ordem decrescente
                return Double.compare(valor2, valor1);
            }
        });

        // Configura o Adapter
        adapterMovimentacao = new AdapterMovimentacao(movimentacoes, this);

        // Configura a RecyclerView
        recyclerMovimentos.setLayoutManager(new LinearLayoutManager(this));
        recyclerMovimentos.setHasFixedSize(true);

        // Define o Adapter para a RecyclerView
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
        // Recarrega o saldo e movimentações ao iniciar a activity
        recuperarSaldo();
        recuperarMovimentacoes();
    }

    @Override
    protected void onStop() {
        super.onStop();

        FirebaseAuth auth = FirebaseAuth.getInstance();
        if (auth.getCurrentUser() != null) {
            String idUsuario = auth.getCurrentUser().getUid();

            if (usuarioRef != null && valueEventListenerUsuario != null) {
                usuarioRef.removeEventListener(valueEventListenerUsuario);
            }
        } else {
            Log.w("PrincipalActivity", "onStop: usuário não está logado, listener não será removido.");
        }
    }


    private void recuperarSaldo() {
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
                    // Adiciona erro ao Log
                    Log.e("FIREBASE", "Erro ao carregar dados do usuário: " + error.getMessage(), error.toException());

                    // Verifica se o usuário está logado
                    if (autenticacao.getCurrentUser() != null) {
                        
                        // Mostra mensagem de erro em uma snackBar
                        Snackbar.make(findViewById(android.R.id.content),
                                        "Erro ao carregar dados do usuário",
                                        Snackbar.LENGTH_SHORT)
                                .show();
                    } else {
                        // Caso contrário apenas explica que o usuário fez logout e que o erro foi ignorado
                        Log.i("FIREBASE", "onCancelled chamado após logout — ignorado.");
                    }
                }

            });
        } catch (Exception e) {
            Log.e("FIREBASE", "Erro inesperado ao configurar listener de usuário", e);
        }
    }

    // Método que recupera movimentações do Firebase para depois serem exibidas no RecyclerView
    private void recuperarMovimentacoes() {
        // Recupera UID do usuário logado
        String uidUsuario = autenticacao.getCurrentUser().getUid();

        // Referencia para as movimentações do usuário
        DatabaseReference movRef = databaseReference
                .child("movimentacoes")
                .child(uidUsuario);

        // Adiciona listener para recuperar movimentações
        valueEventListenerMovimentacoes = movRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                movimentacoes.clear();

                // Percorre os meses
                for (DataSnapshot mesSnapshot : snapshot.getChildren()) {

                    // Percorre as movimentações do mês
                    for (DataSnapshot ds : mesSnapshot.getChildren()) {

                        // Converte DataSnapshot em Movimentacao
                        Movimentacao mov = ds.getValue(Movimentacao.class);

                        if (mov != null) {
                            // Define o ID da movimentação
                            mov.setId(ds.getKey());

                            // Verifica se a movimentação deve ser exibida no mês selecionado
                            if (deveExibirMovimentacaoNoMes(mov, mesAnoSelecionado)) {

                                // Ajusta a movimentação para o mês selecionado (parcelada ou fixa)
                                Movimentacao ajustada = ajustarMovimentacaoParaMes(mov, mesAnoSelecionado);

                                // Adiciona a movimentação ajustada à lista
                                if (ajustada != null) movimentacoes.add(ajustada);
                            }
                        }
                    }
                }

                /* Chama método para ordenar as movimentações da RecyclerView com base no valor absoluto antes
                    de notificar o adapter da mudança de dados */
                ordernaMovimentacoesValorAbsoluto();

                // Notifica o adapter sobre a mudança de dados e recalcula o saldo
                adapterMovimentacao.notifyDataSetChanged();
                calcularSaldo();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                // Adiciona erro ao Log
                Log.e("FIREBASE", "Erro ao carregar movimentações: " + error.getMessage(), error.toException());

                // Verifica se o usuário ta logado
                if (autenticacao.getCurrentUser() != null) {

                    // Se sim apresenta o erro em uma snackbar
                    Snackbar.make(findViewById(android.R.id.content),
                                    "Erro ao carregar movimentações",
                                    Snackbar.LENGTH_SHORT)
                            .show();
                } else {
                    // Caso contrário apenas explica que o usuário fez logout e que o erro foi ignorado
                    Log.i("FIREBASE", "onCancelled chamado após logout — ignorado.");
                }
            }
        });
    }

    // Método que verifica se a movimentação deve ser exibida ou não no mês atual selecionado
    private boolean deveExibirMovimentacaoNoMes(Movimentacao mov, String mesAnoSelecionado) {
        Recorrencia rec = mov.getRecorrencia();
        if (rec == null || rec.getTipo() == null) {
            // Normal: aparece só no mês da data
            return getMesAno(mov.getData()).equals(mesAnoSelecionado);
        }

        String tipo = rec.getTipo().toLowerCase(Locale.ROOT);
        LocalDate dataInicio = parseData(mov.getData());
        YearMonth mesSel = parseMesAno(mesAnoSelecionado);

        if ("parcelada".equals(tipo)) {
            int total = rec.getParcelasTotais() != null ? rec.getParcelasTotais() : 1;
            YearMonth inicio = YearMonth.from(dataInicio);

            for (int i = 0; i < total; i++) {
                YearMonth parcelaMes = inicio.plusMonths(i);
                if (parcelaMes.equals(mesSel)) return true;
            }
            return false;
        }

        if ("fixa".equals(tipo)) {
            YearMonth inicio = YearMonth.from(dataInicio);
            return !mesSel.isBefore(inicio); // aparece a partir da dataInicio
        }

        return getMesAno(mov.getData()).equals(mesAnoSelecionado);
    }

    // Método que ajusta as movimentações parceladas e fixas para o mês atual selecionado
    private Movimentacao ajustarMovimentacaoParaMes(Movimentacao mov, String mesAnoSelecionado) {
        Recorrencia rec = mov.getRecorrencia();
        if (rec == null || rec.getTipo() == null) return mov;

        String tipo = rec.getTipo().toLowerCase(Locale.ROOT);
        LocalDate dataInicio = parseData(mov.getData());
        YearMonth mesSel = parseMesAno(mesAnoSelecionado);

        Movimentacao copia = new Movimentacao();
        copia.setId(mov.getId());
        copia.setCategoria(mov.getCategoria());
        copia.setDescricao(mov.getDescricao());
        copia.setTitulo(mov.getTitulo());
        copia.setTipo(mov.getTipo());
        copia.setStatus(mov.getStatus());
        copia.setData(mov.getData());

        if ("parcelada".equals(tipo)) {
            int total = rec.getParcelasTotais() != null ? rec.getParcelasTotais() : 1;
            YearMonth inicio = YearMonth.from(dataInicio);
            int diff = (int) ChronoUnit.MONTHS.between(inicio, mesSel);

            if (diff >= 0 && diff < total) {
                double valorParcela = mov.getValor() / total;
                copia.setValor(valorParcela);

                Recorrencia r = new Recorrencia();
                r.setTipo("parcelada");
                r.setParcelaAtual(diff + 1);
                r.setParcelasTotais(total);
                r.setFim(rec.getFim());
                copia.setRecorrencia(r);
            } else {
                return null;
            }
        } else if ("fixa".equals(tipo)) {
            copia.setValor(mov.getValor());
            Recorrencia r = new Recorrencia();
            r.setTipo("fixa");
            copia.setRecorrencia(r);
        } else {
            return mov;
        }

        return copia;
    }

    // Método que orderna as movimentações da RecyclerView com base em seu valor absoluto
    public void ordernaMovimentacoesValorAbsoluto(){
        // Orderna as movimentações utilizando o Comparator
        Collections.sort(movimentacoes, new Comparator<Movimentacao>() {
            @Override
            public int compare(Movimentacao m1, Movimentacao m2) {

                // Primeiro transforma os valores em absoluto, ignorando o sinal (positivo ou negativo)
                double valor1 = Math.abs(m1.getValor());
                double valor2 = Math.abs(m2.getValor());

                // E depois compara em ordem Decrescente
                return Double.compare(valor2, valor1);
            }
        });
    }

    /* Métodos para formatação de Datas */
    // Método para converter String em LocalDate
    private LocalDate parseData(String data) {
        try {
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy", Locale.getDefault());
            return LocalDate.parse(data, formatter);
        } catch (DateTimeParseException e) {
            e.printStackTrace();
            return LocalDate.now();
        }
    }

    // Método para converter String mesAno em YearMonth
    private YearMonth parseMesAno(String mesAno) {
        try {
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("MM-yyyy", Locale.getDefault());
            return YearMonth.parse(mesAno, formatter);
        } catch (DateTimeParseException e) {
            e.printStackTrace();
            return YearMonth.now();
        }
    }

    // Método para obter String mesAno a partir de uma data
    private String getMesAno(String data) {
        LocalDate d = parseData(data);
        DateTimeFormatter fmt = DateTimeFormatter.ofPattern("MM-yyyy", Locale.getDefault());
        return d.format(fmt);
    }


    // Método para calcular o saldo financeiro do usuário
    private void calcularSaldo() {
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
        saldoUsuario = saldo;

        // Atualiza interface
        formatandoSaldoTotalReceitasTotalDespesas(saldo, totalDespesas, totalReceitas);
        alterarCorCabecalhoSaldo(saldo);
    }

    // Método que formata o saldo, total de despesas e total de receitas
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

    // Método que altera a cor do cabeçalho do saldo conforme o valor do saldo
    @SuppressLint("ResourceAsColor")
    public void alterarCorCabecalhoSaldo( double saldo ) {

        // Define a cor com base no saldo, se ele for negativo será colorPrimaryDespesa, se for positivo colorPrimary
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
                        // Se clicar em sim, remove os listeners se o usuário ainda existe
                        if (usuarioRef != null && valueEventListenerUsuario != null) {
                            usuarioRef.removeEventListener(valueEventListenerUsuario);
                        }

                        // Faz logout
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
