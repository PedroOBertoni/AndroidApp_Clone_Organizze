package com.aula.organizze.activity;

import android.annotation.SuppressLint;
import android.app.DatePickerDialog;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.text.InputType;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.NumberPicker;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import com.aula.organizze.R;
import com.aula.organizze.model.Movimentacao;
import com.aula.organizze.model.Parcelas;
import com.aula.organizze.model.Recorrencia;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.snackbar.Snackbar;
import com.google.android.material.textfield.TextInputEditText;

import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;

public class DespesasActivity extends AppCompatActivity {

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
        setContentView(R.layout.activity_despesas);

        // Inicializa cores
        corTextoInativo = ContextCompat.getColor(this, R.color.textPrimary);
        corTextoAtivo = ContextCompat.getColor(this, R.color.colorAccentDespesa);

        // Vincula os elementos do layout
        editTextTitulo = findViewById(R.id.editTextTituloDespesa);
        editTextDescricao = findViewById(R.id.editTextDescricaoDespesa);
        editTextCategoria = findViewById(R.id.editTextCategoriaDespesa);
        editTextData = findViewById(R.id.editTextDataDespesa);
        fabCalendario = findViewById(R.id.fabCalendarioDespesa);
        fabConfirmar = findViewById(R.id.floatingActionButtonConfirmarDespesa);
        editTextValor = findViewById(R.id.editTextValorDespesa);

        buttonFixo = findViewById(R.id.buttonFixoDespesa);
        buttonParcelado = findViewById(R.id.buttonParceladoDespesa);
        linearParcelamentoInfo = findViewById(R.id.linearParcelamentoInfoDespesa);
        textParcelasInfo = findViewById(R.id.textParcelasInfoDespesa);
        buttonRemoverParcelamento = findViewById(R.id.buttonRemoverParcelamentoDespesa);

        // Aplica o background selector
        buttonFixo.setBackgroundResource(R.drawable.button_despesa_selector);
        buttonParcelado.setBackgroundResource(R.drawable.button_despesa_selector);

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

                isUpdating = true;
                String digits = s.toString().replaceAll("[^\\d]", "");

                if (digits.isEmpty()) {
                    digitacaoContinua("0");
                    isUpdating = false;
                    return;
                }

                try {
                    long cents = Long.parseLong(digits);
                    double valor = cents / 100.0;
                    String formatted = NumberFormat.getCurrencyInstance(locale).format(valor);

                    editTextValor.setText(formatted);
                    editTextValor.setSelection(formatted.length());
                } catch (NumberFormatException e) {
                    digitacaoContinua("0");
                }

                isUpdating = false;
            }
        });

        /* OnClickListeners */

        // Clique no FAB do calendário -> abre seletor de data
        fabCalendario.setOnClickListener(view -> {
            Calendar cal = Calendar.getInstance();
            int year = cal.get(Calendar.YEAR);
            int month = cal.get(Calendar.MONTH);
            int day = cal.get(Calendar.DAY_OF_MONTH);

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
            String[] categorias = {"Assinaturas e Serviços", "Compras", "Alimentação", "Transporte", "Lazer", "Outros"};
            new MaterialAlertDialogBuilder(this, R.style.RoundedDialogDespesa)
                    .setTitle("Selecione uma categoria")
                    .setItems(categorias, (dialog, which) -> editTextCategoria.setText(categorias[which]))
                    .show();
        });

        // Clique nos botões de modo
        buttonFixo.setOnClickListener(view -> {
            if (modoFixoAtivo) {
                desativarModoFixo();
            } else {
                ativarModoFixo();
            }
        });

        buttonParcelado.setOnClickListener(view -> {
            if (modoParceladoAtivo) {
                desativarModoParcelado();
            } else {
                ativarModoParcelado();
            }
        });

        // Clique no botão de remover parcelamento/frequência
        buttonRemoverParcelamento.setOnClickListener(view -> {
            if (modoFixoAtivo) {
                desativarModoFixo();
            } else if (modoParceladoAtivo) {
                desativarModoParcelado();
            }
        });

        // Clique no FAB confirma -> realiza validação e tenta salvar
        fabConfirmar.setOnClickListener(view -> {
            if (validarCampos(view)) {
                salvarDespesa(view);
                limparCampos();
                Snackbar.make(view, "Despesa adicionada com sucesso!", Snackbar.LENGTH_SHORT).show();
            }
        });
    }

    /* Atualiza o estilo visual do botão de modo (Fixo/Parcelado) e
     * Adiciona borda colorAccentDespesa quando ativo */
    private void atualizarEstiloBotao(Button botao, boolean ativo) {
        int corPrimaria = ContextCompat.getColor(this, R.color.colorAccentDespesa);
        int corBranca = ContextCompat.getColor(this, android.R.color.white);

        GradientDrawable drawable = new GradientDrawable();
        drawable.setCornerRadius(16f); // bordas arredondadas suaves
        drawable.setStroke(3, corPrimaria);

        if (ativo) {
            drawable.setColor(corPrimaria); // fundo cheio
            botao.setTextColor(corBranca);
        } else {
            drawable.setColor(Color.TRANSPARENT); // fundo transparente
            botao.setTextColor(corPrimaria);
        }

        botao.setBackground(drawable);
    }

    /* Ativa o modo Fixo: abre diálogo para escolher frequência */
    private void ativarModoFixo() {
        // Desativa modo parcelado se estiver ativo
        if (modoParceladoAtivo) {
            desativarModoParcelado();
        }

        String[] opcoesExibicao = {"Diário", "Semanal", "Quinzenal", "Mensal"};
        String[] opcoesValor = {"diario", "semanal", "quinzenal", "mensal"};

        new MaterialAlertDialogBuilder(this, R.style.RoundedDialogDespesa)
                .setTitle("Frequência da despesa fixa")
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

        NumberPicker numberPicker = new NumberPicker(this);
        numberPicker.setMinValue(2);
        numberPicker.setMaxValue(24);
        numberPicker.setValue(quantParcelas != null ? quantParcelas : 2);

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
        modoFixoAtivo = false;
        frequencia = null;
        atualizarEstiloBotao(buttonFixo, false);
        linearParcelamentoInfo.setVisibility(View.GONE);
    }

    /* Desativa o modo Parcelado */
    private void desativarModoParcelado() {
        modoParceladoAtivo = false;
        quantParcelas = null;
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
            long cents = Long.parseLong(digitosSomente);
            double valor = cents / 100.0;
            String formatted = NumberFormat.getCurrencyInstance(locale).format(valor);
            editTextValor.setText(formatted);
            editTextValor.setSelection(formatted.length());
        } catch (NumberFormatException e) {
            editTextValor.setText(NumberFormat.getCurrencyInstance(locale).format(0.0));
            editTextValor.setSelection(editTextValor.getText().length());
        }
    }

    /* Converte o texto formatado do campo de valor para Double */
    private Double formatandoValor(EditText editTextValor) {
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
        String titulo = editTextTitulo.getText().toString().trim();
        String categoria = editTextCategoria.getText().toString().trim();
        String data = editTextData.getText().toString().trim();

        boolean valorInvalido = formatandoValor(editTextValor) <= 0;

        if (titulo.isEmpty()) {
            Snackbar.make(view, "Preencha o campo Título.", Snackbar.LENGTH_SHORT).show();
            return false;
        }
        if (categoria.isEmpty()) {
            Snackbar.make(view, "Selecione uma Categoria.", Snackbar.LENGTH_SHORT).show();
            return false;
        }
        if (data.isEmpty()) {
            Snackbar.make(view, "Informe uma Data.", Snackbar.LENGTH_SHORT).show();
            return false;
        }
        if (valorInvalido) {
            Snackbar.make(view, "Informe um Valor válido.", Snackbar.LENGTH_SHORT).show();
            return false;
        }

        // Se algum modo estiver ativo, garantir que os dados foram definidos
        if (modoFixoAtivo && frequencia == null) {
            Snackbar.make(view, "Selecione uma frequência para a despesa fixa.", Snackbar.LENGTH_SHORT).show();
            return false;
        }
        if (modoParceladoAtivo && quantParcelas == null) {
            Snackbar.make(view, "Informe o número de parcelas.", Snackbar.LENGTH_SHORT).show();
            return false;
        }

        return true;
    }

    public void salvarDespesa(View view) {
        // Validando os campos obrigatórios
        if (!validarCampos(view)) {
            return;
        }

        // Formatando o valor
        Double valorRecuperado = formatandoValor(editTextValor);

        // Criar objeto movimentação
        Movimentacao movimentacao = new Movimentacao();
        movimentacao.setValor(valorRecuperado);
        movimentacao.setTitulo(editTextTitulo.getText().toString().trim());
        movimentacao.setDescricao(editTextDescricao.getText().toString().trim());
        movimentacao.setCategoria(editTextCategoria.getText().toString().trim());
        movimentacao.setData(editTextData.getText().toString().trim());
        movimentacao.setTipo("D");
        movimentacao.setStatus("ativa");

        // configurando a recorrência (fixa)
        if (modoFixoAtivo) {
            Recorrencia recorrencia = new Recorrencia();
            recorrencia.setTipo(frequencia);

            // Definindo uma data final
            recorrencia.setFim(null);

            movimentacao.setRecorrencia(recorrencia);
        }

        // Configurando o parcelamento
        else if (modoParceladoAtivo) {
            Parcelas parcelas = new Parcelas();
            parcelas.setTotal(quantParcelas);
            parcelas.setAtual(1); // Primeira parcela

            // Calcula a data da última parcela (baseado na data inicial)
            try {
                SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy");
                Calendar cal = Calendar.getInstance();
                cal.setTime(sdf.parse(editTextData.getText().toString()));

                cal.add(Calendar.MONTH, quantParcelas - 1);
                String dataFinal = sdf.format(cal.getTime());
                parcelas.setFim(dataFinal);
            } catch (Exception e) {
                e.printStackTrace();
            }

            movimentacao.setParcelas(parcelas);

            // Registrando a recorrência como mensal para manter consistência
            Recorrencia recorrencia = new Recorrencia("mensal", parcelas.getFim());
            movimentacao.setRecorrencia(recorrencia);
        }

        // Salvando a movimentação no Firebase
        movimentacao.salvar();

        // 7️⃣ Feedback visual
        Toast.makeText(this, "Despesa salva com sucesso!", Toast.LENGTH_SHORT).show();

        // Limpa os campos após salvar
        limparCampos();
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