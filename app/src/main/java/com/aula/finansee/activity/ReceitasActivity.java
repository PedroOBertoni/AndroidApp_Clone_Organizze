package com.aula.finansee.activity;

import android.annotation.SuppressLint;
import android.app.DatePickerDialog;
import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.text.InputType;
import android.util.Log;
import android.view.ContextThemeWrapper;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.NumberPicker;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import com.aula.finansee.R;
import com.aula.finansee.model.Movimentacao;
import com.aula.finansee.model.Recorrencia;
import com.aula.finansee.utils.FirebaseErrorHandler;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.snackbar.Snackbar;
import com.google.android.material.textfield.TextInputEditText;

import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;

public class ReceitasActivity extends AppCompatActivity {

    // Componentes da interface
    private TextInputEditText editTextTitulo, editTextDescricao, editTextCategoria, editTextData;
    private EditText editTextValor;
    private FloatingActionButton fabCalendario, fabConfirmar;
    private Button buttonFixo, buttonParcelado;
    private LinearLayout linearParcelamentoInfo;
    private TextView textParcelasInfo;
    private View buttonRemoverParcelamento;

    // Flag para evitar loop no TextWatcher
    private boolean isUpdating = false;
    private final Locale locale = new Locale("pt", "BR");

    // Estado dos modos
    private boolean modoFixoAtivo = false;
    private boolean modoParceladoAtivo = false;
    private Integer quantParcelas = null; // null = não definido
    private String frequencia = null;     // null = não definido

    // Cores para estilo dos botões
    private int corTextoInativo;
    private int corTextoAtivo;

    // Objeto movimentacao para salvar os dados
    private Movimentacao movimentacao;

    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_receitas);

        // Inicializa cores
        corTextoInativo = ContextCompat.getColor(this, R.color.textPrimary);
        corTextoAtivo = ContextCompat.getColor(this, R.color.colorAccentReceita);

        // Vincula os elementos do layout
        editTextTitulo = findViewById(R.id.editTextTituloReceita);
        editTextDescricao = findViewById(R.id.editTextDescricaoReceita);
        editTextCategoria = findViewById(R.id.editTextCategoriaReceita);
        editTextData = findViewById(R.id.editTextDataReceita);
        fabCalendario = findViewById(R.id.fabCalendarioReceita);
        fabConfirmar = findViewById(R.id.floatingActionButtonConfirmarReceita);
        editTextValor = findViewById(R.id.editTextValorReceita);

        // Modos Fixo e Parcelado
        buttonFixo = findViewById(R.id.buttonFixoReceita);
        buttonParcelado = findViewById(R.id.buttonParceladoReceita);
        linearParcelamentoInfo = findViewById(R.id.linearParcelamentoInfoReceita);
        textParcelasInfo = findViewById(R.id.textParcelasInfoReceita);
        buttonRemoverParcelamento = findViewById(R.id.buttonRemoverParcelamentoReceita);

        // Aplica o background selector
        buttonFixo.setBackgroundResource(R.drawable.button_receita_selector);
        buttonParcelado.setBackgroundResource(R.drawable.button_receita_selector);

        // Define estado inicial (Fixo e Parcelados inativos)
        atualizarEstiloBotao(buttonFixo, false);
        atualizarEstiloBotao(buttonParcelado, false);

        // Remove efeitos visuais e sonoros padrão dos botões para evitar piscadas ao clicar
        // Botão Fixo:
        buttonFixo.setBackgroundTintList(null);
        buttonFixo.setStateListAnimator(null);
        buttonFixo.setSoundEffectsEnabled(false);

        // Botão Parcelado:
        buttonParcelado.setBackgroundTintList(null);
        buttonParcelado.setStateListAnimator(null);
        buttonParcelado.setSoundEffectsEnabled(false);


        // Foca automaticamente no campo de valor e abre o teclado
        editTextValor.requestFocus();
        getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_VISIBLE);

        // Impede mover o cursor manualmente, mas ainda permite digitar normalmente
        editTextValor.setOnTouchListener((view, event) -> {
            int length = editTextValor.getText().length();
            editTextValor.setSelection(length);
            view.performClick(); // mantém comportamento padrão (abre teclado)
            return false; // permite foco e digitação
        });

        // Impede seleção de texto
        editTextValor.setLongClickable(false);
        editTextValor.setTextIsSelectable(false);

        // Garante teclado numérico
        editTextValor.setInputType(InputType.TYPE_CLASS_NUMBER);

        // Define data atual como padrão
        Calendar calendar = Calendar.getInstance();
        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
        editTextData.setText(sdf.format(calendar.getTime()));

        // Inicializa o campo com R$ 0,00
        digitacaoContinua("0");

        // TextWatcher que implementa o comportamento "digitar centavos e empurrar"
        editTextValor.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {}

            @Override
            public void afterTextChanged(Editable s) {
                if (isUpdating) return;

                // Sinaliza que está atualizando para evitar loop
                isUpdating = true;
                String digits = s.toString().replaceAll("[^\\d]", "");


                // Se não houver dígitos, define como "0"
                if (digits.isEmpty()) {
                    digitacaoContinua("0");
                    isUpdating = false;
                    return;
                }

                // Converte dígitos para valor em centavos
                try {
                    // Converte para long
                    long cents = Long.parseLong(digits);
                    double valor = cents / 100.0;

                    // Formata o valor como moeda
                    String formatted = NumberFormat.getCurrencyInstance(locale).format(valor);

                    // Atualiza o campo com o valor formatado
                    editTextValor.setText(formatted);
                    editTextValor.setSelection(formatted.length());

                } catch (NumberFormatException e) {
                    // Em caso de erro, redefine para R$ 0,00
                    digitacaoContinua("0");
                }

                isUpdating = false;
            }
        });

        /* OnClickListeners */

        // Clique no FAB do calendário -> abre seletor de data
        fabCalendario.setOnClickListener(view -> {
            // Obtém data atual para inicializar o DatePickerDialog
            Calendar cal = Calendar.getInstance();

            // Recupera valores atuais do calendário
            int year = cal.get(Calendar.YEAR);
            int month = cal.get(Calendar.MONTH);
            int day = cal.get(Calendar.DAY_OF_MONTH);

            // Cria DatePickerDialog para buscar a data da receita
            DatePickerDialog datePicker = new DatePickerDialog(this,
                    (v, selectedYear, selectedMonth, selectedDay) -> {
                        cal.set(selectedYear, selectedMonth, selectedDay);
                        editTextData.setText(new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).format(cal.getTime()));
                    },
                    year, month, day);
            datePicker.show();
        });

        // Clique no campo de categoria -> abre lista de opções
        editTextCategoria.setOnClickListener(view -> {
            // Define as categorias disponíveis
            String[] categorias = {"Salário e Renda Fixa", "Investimentos", "Vendas e Serviços", "Presentes e Doações", "Reembolsos e Restituições", "Outros"};

            // Cria e exibe o alertDialog de seleção das opções definidas acima
            new MaterialAlertDialogBuilder(
                    new ContextThemeWrapper(this, R.style.RoundedAlertDialogTheme)
            )
                    .setTitle("Selecione uma categoria")
                    .setItems(categorias, (dialog, which) -> editTextCategoria.setText(categorias[which]))
                    .show();
        });

        // Clique nos botões de modo
        buttonFixo.setOnClickListener(view -> {
            // Desativa modo Fixo se já estiver ativo e depois ativa novamente
            if (modoFixoAtivo) {
                desativarModoFixo();
            } else {
                ativarModoFixo();
            }
        });

        buttonParcelado.setOnClickListener(view -> {
            // // Desativa modo Parcela se já estiver ativo e depois ativa novamente
            if (modoParceladoAtivo) {
                desativarModoParcelado();

            } else {
                ativarModoParcelado();
            }
        });

        // Clique no botão de remover parcelamento/frequência
        buttonRemoverParcelamento.setOnClickListener(view -> {
            // Desativa qualquer modo ativo
            if (modoFixoAtivo) {
                desativarModoFixo();

            } else if (modoParceladoAtivo) {
                desativarModoParcelado();
            }
        });

        // Clique no FAB confirma -> realiza validação e tenta salvar
        fabConfirmar.setOnClickListener(view -> {
            // Valida campos, salva receita, apresenta snackbar e fecha activity
            if (validarCampos(view)) {
                salvarReceita(view);
                limparCampos();
                Snackbar.make(view, "Receita adicionada com sucesso!", Snackbar.LENGTH_SHORT).show();

                new android.os.Handler().postDelayed(() -> {
                    finish(); // Fecha a activity após 2 segundos
                }, 1500); // 1500 milissegundos = 1,5 segundos
            }
        });
    }

    /* Atualiza o estilo visual do botão de modo (Fixo/Parcelado) e
     * Adiciona borda colorAccentReceita quando ativo */
    private void atualizarEstiloBotao(Button botao, boolean ativo) {
        // Instancia as cores a serem usadas
        int corPrimaria = ContextCompat.getColor(this, R.color.colorAccentReceita);
        int corBranca = ContextCompat.getColor(this, android.R.color.white);

        // Cria um GradientDrawable para personalizar o fundo do botão
        GradientDrawable drawable = new GradientDrawable();
        drawable.setCornerRadius(16f); // bordas arredondadas suaves
        drawable.setStroke(3, corPrimaria);

        // Define as cores conforme o estado ativo/inativo
        if (ativo) {
            drawable.setColor(corPrimaria); // fundo cheio
            botao.setTextColor(corBranca);
        } else {
            drawable.setColor(Color.TRANSPARENT); // fundo transparente
            botao.setTextColor(corPrimaria);
        }

        // Aplica o drawable como fundo do botão
        botao.setBackground(drawable);
    }

    /* Ativa o modo Fixo: abre diálogo para escolher frequência */
    private void ativarModoFixo() {
        // Desativa modo parcelado se estiver ativo
        if (modoParceladoAtivo) {
            desativarModoParcelado();
        }

        // Define array com as opções de frequência
        String[] opcoesExibicao = {"Diário", "Semanal", "Quinzenal", "Mensal", "Semestral", "Anual"};
        String[] opcoesValor = {"diario", "semanal", "quinzenal", "mensal", "semestral", "anual"};

        // Cria alertDialog para escolher a frequência entre as opções definidas acima
        new MaterialAlertDialogBuilder(
                new ContextThemeWrapper(this, R.style.RoundedAlertDialogTheme)
        )
                .setTitle("Frequência da receita fixa")
                .setItems(opcoesExibicao, (dialog, which) -> {
                    frequencia = opcoesValor[which];
                    modoFixoAtivo = true;
                    modoParceladoAtivo = false;
                    atualizarEstiloBotao(buttonFixo, true);
                    atualizarEstiloBotao(buttonParcelado, false);
                    atualizarInfoModo("Fixo: " + opcoesExibicao[which]);
                })
                .setNegativeButton("Cancelar", (dialog, which) -> {
                    // Não ativa nenhum modo
                })
                .show();
    }

    /* Ativa o modo Parcelado: abre NumberPicker (2 a 24 parcelas) */
    private void ativarModoParcelado() {
        // Desativa modo fixo se estiver ativo
        if (modoFixoAtivo) {
            desativarModoFixo();
        }

        // Configura NumberPicker
        NumberPicker numberPicker = new NumberPicker(this);
        numberPicker.setMinValue(2);
        numberPicker.setMaxValue(24);
        numberPicker.setValue(quantParcelas != null ? quantParcelas : 2);

        // Cria alertDialog para escolher a quantidade de parcelas com o NumberPicker
        new AlertDialog.Builder(this)
                .setTitle("Número de parcelas")
                .setView(numberPicker)
                .setPositiveButton("OK", (dialog, which) -> {
                    quantParcelas = numberPicker.getValue();
                    modoParceladoAtivo = true;
                    modoFixoAtivo = false;
                    atualizarEstiloBotao(buttonFixo, false);
                    atualizarEstiloBotao(buttonParcelado, true);
                    atualizarInfoModo("Parcelado: " + quantParcelas + "x");
                })
                .setNegativeButton("Cancelar", (dialog, which) -> {
                    // Não ativa nenhum modo
                })
                .show();
    }

    /* Desativa o modo Fixo */
    private void desativarModoFixo() {
        // Redefine variáveis
        modoFixoAtivo = false;
        frequencia = null;

        // Atualiza estilo do botão
        atualizarEstiloBotao(buttonFixo, false);
        linearParcelamentoInfo.setVisibility(View.GONE);
    }

    /* Desativa o modo Parcelado */
    private void desativarModoParcelado() {
        // Redefine variáveis
        modoParceladoAtivo = false;
        quantParcelas = null;

        // Atualiza estilo do botão
        atualizarEstiloBotao(buttonParcelado, false);
        linearParcelamentoInfo.setVisibility(View.GONE);
    }

    /* Atualiza o texto e visibilidade do layout de informações do modo ativo */
    private void atualizarInfoModo(String texto) {
        textParcelasInfo.setText(texto);
        linearParcelamentoInfo.setVisibility(View.VISIBLE);
    }

    // Helper para setar "R$ 0,00" ou qualquer centavos em string de dígitos (ex: "0" ou "12")
    private void digitacaoContinua(String digitosSomente) {
        try {
            // Converte dígitos para valor em double
            long cents = Long.parseLong(digitosSomente);
            double valor = cents / 100.0;

            // Formata e atualiza o campo
            String formatted = NumberFormat.getCurrencyInstance(locale).format(valor);
            editTextValor.setText(formatted);
            editTextValor.setSelection(formatted.length());

        } catch (NumberFormatException e) {
            // Em caso de erro, define como R$ 0,00
            editTextValor.setText(NumberFormat.getCurrencyInstance(locale).format(0.0));
            editTextValor.setSelection(editTextValor.getText().length());
        }
    }

    /* Converte o texto formatado do campo de valor para Double */
    private Double formatandoValor(EditText editTextValor) {
        // Remove formatação de R$, espaços indevidos, etc e converte para Double
        String valor = editTextValor.getText().toString()
                .replace("R$", "")
                .replaceAll("\\s", "")
                .replaceAll("\\.", "")
                .replace(",", ".")
                .replaceAll("[\\u00A0\\s]", "")
                .trim();

        return valor.isEmpty() ? 0.0 : Double.parseDouble(valor);
    }

    /* Valida os campos antes de salvar */
    private boolean validarCampos(View view) {
        // Recupera valores dos campos
        String titulo = editTextTitulo.getText().toString().trim();
        String categoria = editTextCategoria.getText().toString().trim();
        String data = editTextData.getText().toString().trim();

        boolean valorInvalido = formatandoValor(editTextValor) <= 0;

        // Validação do título
        if (titulo.isEmpty()) {
            Snackbar.make(view,
                    "Preencha o campo Título.",
                    Snackbar.LENGTH_SHORT).show();
            return false;
        }

        // Validação da categoria
        if (categoria.isEmpty()) {
            Snackbar.make(view,
                    "Selecione uma Categoria.",
                    Snackbar.LENGTH_SHORT).show();
            return false;
        }

        // Validação da data
        if (data.isEmpty()) {
            Snackbar.make(view,
                    "Informe uma Data.",
                    Snackbar.LENGTH_SHORT).show();
            return false;
        }

        // Validação do valor
        if (valorInvalido) {
            Snackbar.make(view,
                    "Informe um Valor válido.",
                    Snackbar.LENGTH_SHORT).show();
            return false;
        }

        // Se algum modo estiver ativo, garantir que os dados foram definidos
        if (modoFixoAtivo && frequencia == null) {
            Snackbar.make(view,
                    "Selecione uma frequência para a receita fixa.",
                    Snackbar.LENGTH_SHORT).show();
            return false;
        }

        // Se modo parcelado ativo, garantir que número de parcelas foi definido
        if (modoParceladoAtivo && quantParcelas == null) {
            Snackbar.make(view,
                    "Informe o número de parcelas.",
                    Snackbar.LENGTH_SHORT).show();
            return false;
        }

        return true;
    }

    public void salvarReceita(View view) {
        // Primeiro verifica se o aparelho está conectado a uma rede ativa
        if (!FirebaseErrorHandler.checkConnectionAndNotify(this, "salvar receita")) {
            return;
        }

        // Validando os campos obrigatórios
        if (!validarCampos(view)) {
            return;
        }

        // Formatando o valor
        Double valorRecuperado = formatandoValor(editTextValor);

        // Criar objeto movimentação e define os seus atributos
        Movimentacao movimentacao = new Movimentacao();
        movimentacao.setValor(valorRecuperado);
        movimentacao.setTitulo(editTextTitulo.getText().toString().trim());
        movimentacao.setDescricao(editTextDescricao.getText().toString().trim());
        movimentacao.setCategoria(editTextCategoria.getText().toString().trim());
        movimentacao.setData(editTextData.getText().toString().trim());
        movimentacao.setTipo("R");
        movimentacao.setStatus("ativa");

        // Criar objeto de recorrência (se houver)
        Recorrencia recorrencia = null;

        if (modoFixoAtivo) {
            // Configura dados da recorrência fixa
            recorrencia = new Recorrencia();
            recorrencia.setTipo("fixa");
            recorrencia.setParcelaAtual(null);
            recorrencia.setParcelasTotais(null);
            recorrencia.setFim(null);
        }
        else if (modoParceladoAtivo) {
            // Configura dados da recorrência parcelada
            recorrencia = new Recorrencia();
            recorrencia.setTipo("parcelada");
            recorrencia.setParcelaAtual(1);
            recorrencia.setParcelasTotais(quantParcelas);

            try {
                // Calcula a data de fim somando meses à data inicial
                SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
                Calendar cal = Calendar.getInstance();

                // Define a data inicial
                cal.setTime(sdf.parse(editTextData.getText().toString()));
                cal.add(Calendar.MONTH, quantParcelas - 1);

                // Define a data de fim formatada
                recorrencia.setFim(sdf.format(cal.getTime()));
            } catch (Exception e) {
                // Em caso de erro, define fim como null
                e.printStackTrace();
                recorrencia.setFim(null);
            }
        }

        // Se tiver recorrência, define na movimentação
        if (recorrencia != null) {
            movimentacao.setRecorrencia(recorrencia);
        }

        // a própria classe Movimentacao cuida do caminho correto que será salvo no Firebase
        movimentacao.salvar()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        // Apresenta o sucesso na snackBar
                        Snackbar.make(view, "Receita adicionada com sucesso!", Snackbar.LENGTH_LONG).show();
                    } else {
                        // Erro de rede, timeout ou parsing de data irá cair abaixo
                        FirebaseErrorHandler.handleTaskFailure(this, task.getException(), "salvar receita");
                    }
                });
    }

    /* Limpa todos os campos após salvar */
    private void limparCampos() {
        // Limpa texto dos campos principais
        editTextTitulo.setText("");
        editTextDescricao.setText("");
        editTextCategoria.setText("");

        // Redefine a data para o dia atual
        Calendar calendar = Calendar.getInstance();
        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
        editTextData.setText(sdf.format(calendar.getTime()));

        // Reinicia o campo de valor com R$ 0,00
        digitacaoContinua("0");

        // Reinicia modos
        modoFixoAtivo = false;
        modoParceladoAtivo = false;
        frequencia = null;
        quantParcelas = null;
        linearParcelamentoInfo.setVisibility(View.GONE);
        atualizarEstiloBotao(buttonFixo, false);
        atualizarEstiloBotao(buttonParcelado, false);

        // Coloca o foco no primeiro campo
        editTextTitulo.requestFocus();
    }
}