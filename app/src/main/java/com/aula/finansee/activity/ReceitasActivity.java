package com.aula.finansee.activity;

import android.annotation.SuppressLint;
import android.app.DatePickerDialog;
import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.text.Editable;
import android.text.TextWatcher;
import android.text.InputType;
import android.view.ContextThemeWrapper;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.NumberPicker;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import com.aula.finansee.R;
import com.aula.finansee.model.Movimentacao;
import com.aula.finansee.model.Recorrencia;
import com.aula.finansee.utils.FirebaseErrorHandler;
import com.aula.finansee.utils.NetworkUtils;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.snackbar.Snackbar;
import com.google.android.material.textfield.TextInputEditText;

import java.text.NumberFormat;
import java.text.ParseException;
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

        /* Verifica se não está conectado a internet */
        if (!NetworkUtils.isNetworkAvailable(this)) {
            // Avisa o usuário usando Snackbar
            Snackbar snackbar = Snackbar.make(findViewById(android.R.id.content),
                    "Você está offline. Conecte-se à internet para adicionar uma movimentação.",
                    Snackbar.LENGTH_LONG);

            // Define a cor do texto do botão de ação
            snackbar.setActionTextColor(getColor(R.color.colorPrimaryDarkDespesa));

            // E mostra botão FECHAR que finaliza a activity
            snackbar.setAction("FECHAR", v -> {
                finish();
            }).show();

            // Finaliza a Activity automaticamente após 3 segundos
            new Handler(Looper.getMainLooper()).postDelayed(this::finish, 3000);

            return;
        }
        else{
            // Se estiver com internet foca automaticamente no campo de valor e abre o teclado
            editTextValor.requestFocus();
            getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_VISIBLE);
        }

        // Impede mover o cursor manualmente na hora de digitar o valor, mas ainda permite digitar normalmente
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

        /* TextWatchers */

        /* TextWatcher que implementa a máscara do valor, onde vai se digitando os centavos e eles vão empurrando para as
        outras casas */
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

        // TextWatcher que implementa a máscara para digitar a data
        // variáveis de controle do TextWatcher
        final boolean[] isUpdating = {false};
        final String maskPattern = "##/##/####"; // Máscara de data (dd/MM/yyyy)

        editTextData.addTextChangedListener(new TextWatcher() {

            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                // Nenhuma ação necessária antes da mudança
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                // Nenhuma ação necessária durante a digitação
            }

            @Override
            public void afterTextChanged(Editable s) {
                if (isUpdating[0]) return;
                // Torna o isUpdating verdadeiro
                isUpdating[0] = true;

                // Remove qualquer caractere que não seja número
                String digits = s.toString().replaceAll("[^\\d]", "");

                // Limita a quantidade máxima de dígitos (8 no formato ddMMyyyy)
                if (digits.length() > 8) {
                    digits = digits.substring(0, 8);
                }

                // Aplica a máscara "##/##/####"
                StringBuilder formatted = new StringBuilder();
                int digitIndex = 0;
                for (int i = 0; i < maskPattern.length(); i++) {
                    char maskChar = maskPattern.charAt(i);
                    if (maskChar == '#') {
                        if (digitIndex < digits.length()) {
                            formatted.append(digits.charAt(digitIndex));
                            digitIndex++;
                        } else {
                            break; // Sai se não houver mais dígitos
                        }
                    } else {
                        if (digitIndex < digits.length()) {
                            formatted.append(maskChar);
                        } else {
                            break; // O break serve para evitar adicionar barras extras no final
                        }
                    }
                }

                // Atualiza o texto do EditText
                String newText = formatted.toString();
                editTextData.removeTextChangedListener(this);
                editTextData.setText(newText);
                editTextData.setSelection(newText.length()); // Coloca o cursor no fim
                editTextData.addTextChangedListener(this);

                // Torna o isUpdating falso novamente
                isUpdating[0] = false;
            }
        });

        /* OnClickListeners */

        // Clique no FAB do calendário -> abre seletor de data
        fabCalendario.setOnClickListener(view -> {
            // Limpa o campo de texto de data
            editTextData.setText(null);

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
                Snackbar.make(view,
                        "Receita adicionada com sucesso!",
                        Snackbar.LENGTH_SHORT).show();

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
        new MaterialAlertDialogBuilder(
                new ContextThemeWrapper(this, R.style.RoundedAlertDialogTheme)
        )
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

        // Validação da data, text se está vazia
        if (data.isEmpty()) {
            Snackbar.make(view,
                    "Informe uma Data.",
                    Snackbar.LENGTH_SHORT).show();
            return false;
        }

        // Validação da data, verifica se é válida
        if (!ehDataValida(data)) {
            Snackbar.make(view,
                    "A data informada é inválida!",
                    Snackbar.LENGTH_SHORT).show();
            return false;
        }

        // Validação do valor
        if (valorInvalido) {
            Snackbar.make(view,
                    "O valor informado é inválido!",
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
                        Snackbar.make(view, "Receita adicionada com sucesso!",
                                Snackbar.LENGTH_LONG).show();
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

    private boolean ehDataValida(String dateStr) {
        if (dateStr == null || dateStr.length() != 10) {
            return false;
        }

        // 1. Define o formato e o Locale
        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());

        // 2. CRUCIAL: setLenient(false)
        // Isso garante que o SimpleDateFormat NÃO aceite datas "flexíveis"
        // como 30/02/2025 (que ele converteria para 02/03/2025 por padrão).
        // Ele forçará a verificação estrita do calendário.
        sdf.setLenient(false);

        try {
            // Tenta fazer o parse (conversão de String para Date)
            sdf.parse(dateStr);
            return true; // Se não lançar exceção, a data é válida
        } catch (ParseException e) {
            // Se lançar ParseException, significa que a data não é válida (ex: dia 32, mês 13 ou 30/02)
            return false;
        }
    }
}