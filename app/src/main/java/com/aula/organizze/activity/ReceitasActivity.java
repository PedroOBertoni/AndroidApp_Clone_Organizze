package com.aula.organizze.activity;

import android.annotation.SuppressLint;
import android.app.DatePickerDialog;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.text.InputType;
import android.view.View;
import android.view.WindowManager;
import android.widget.Toast;
import android.widget.EditText;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.aula.organizze.R;
import com.aula.organizze.model.Movimentacao;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.snackbar.Snackbar;
import com.google.android.material.textfield.TextInputEditText;

import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;

public class ReceitasActivity extends AppCompatActivity {

    // componentes da interface
    private TextInputEditText editTextTitulo, editTextDescricao, editTextCategoria, editTextData;
    private EditText editTextValor;
    private FloatingActionButton fabCalendario, fabConfirmar;

    // flag para evitar loop no TextWatcher
    private boolean isUpdating = false;
    private final Locale locale = new Locale("pt", "BR");

    // objeto movimentacao para salvar os dados
    private Movimentacao movimentacao;

    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_receitas);

        // Vincula os elementos do layout
        editTextTitulo = findViewById(R.id.editTextTituloReceita);
        editTextDescricao = findViewById(R.id.editTextDescricaoReceita);
        editTextCategoria = findViewById(R.id.editTextCategoriaReceita);
        editTextData = findViewById(R.id.editTextDataReceita);
        fabCalendario = findViewById(R.id.fabCalendarioReceita);
        fabConfirmar = findViewById(R.id.floatingActionButtonConfirmarReceita);
        editTextValor = findViewById(R.id.editTextValorReceita);

        // Foca automaticamente no campo de valor e abre o teclado
        editTextValor.requestFocus();
        getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_VISIBLE);

        // Impede mover o cursor manualmente, mas ainda permite digitar normalmente
        editTextValor.setOnTouchListener((v, event) -> {
            int length = editTextValor.getText().length();
            editTextValor.setSelection(length);
            v.performClick(); // mantém comportamento padrão (abre teclado)
            return false; // permite foco e digitação
        });

        // Impede seleção de texto
        editTextValor.setLongClickable(false);
        editTextValor.setTextIsSelectable(false);


        // Ajuste: garante teclado numérico (opcional, também configure no XML)
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
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                // não usado
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                // não usado
            }

            @Override
            public void afterTextChanged(Editable s) {
                if (isUpdating) return;

                isUpdating = true;

                // Pega apenas os dígitos do texto (remove "R$", espaços, pontuação etc)
                String digits = s.toString().replaceAll("[^\\d]", "");

                // Se não houver dígitos, considera 0
                if (digits.isEmpty()) {
                    digitacaoContinua("0");
                    isUpdating = false;
                    return;
                }

                try {
                    // Converte para centavos (long para evitar perda)
                    long cents = Long.parseLong(digits);

                    // Converte para valor em reais (double apenas para formatação)
                    double valor = cents / 100.0;

                    // Formata para moeda pt-BR (ex: R$ 1.234,56)
                    String formatted = NumberFormat.getCurrencyInstance(locale).format(valor);

                    // Atualiza o EditText com o texto formatado e move o cursor para o fim
                    editTextValor.setText(formatted);
                    editTextValor.setSelection(formatted.length());
                } catch (NumberFormatException e) {
                    // Em caso raro de overflow/parsing
                    digitacaoContinua("0");
                }

                isUpdating = false;
            }
        });

        // Clique no FAB do calendário -> abre seletor de data
        fabCalendario.setOnClickListener(v -> {
            int year = calendar.get(Calendar.YEAR);
            int month = calendar.get(Calendar.MONTH);
            int day = calendar.get(Calendar.DAY_OF_MONTH);

            DatePickerDialog datePicker = new DatePickerDialog(this,
                    (view, selectedYear, selectedMonth, selectedDay) -> {
                        calendar.set(selectedYear, selectedMonth, selectedDay);
                        editTextData.setText(sdf.format(calendar.getTime()));
                    },
                    year, month, day);
            datePicker.show();
        });

        // Clique no campo de categoria -> abre lista de opções
        editTextCategoria.setOnClickListener(v -> {
            String[] categorias = {"Salário", "Freelancee e Serviços", "Investimentos", "Presentes", "Vendas", "Outros"};
            new MaterialAlertDialogBuilder(this, R.style.RoundedDialogReceita)
                    .setTitle("Selecione uma categoria")
                    .setItems(categorias, (dialog, which) -> editTextCategoria.setText(categorias[which]))
                    .show();
        });

        // Clique no FAB confirma -> realiza validação e tenta salvar
        fabConfirmar.setOnClickListener( view -> {
            if (validarCampos(view)) {
                // Todos os campos válidos → salva e limpa
                salvarReceita(view);
                limparCampos();

                Snackbar.make(view, "Receita adicionada com sucesso!", Snackbar.LENGTH_SHORT).show();
            }
        });
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

    private Double formatandoValor(EditText editTextValor) {
        String valor = editTextValor.getText().toString()
                .replace("R$", "")
                .replaceAll("\\s", "")
                .replaceAll("\\.", "")
                .replace(",", ".")
                .replaceAll("[\\u00A0\\s]", "") // remove espaços normais e não quebráveis
                .trim();

        return Double.parseDouble(valor);
    }

    private boolean validarCampos(View view) {
        String titulo = editTextTitulo.getText().toString().trim();
        String categoria = editTextCategoria.getText().toString().trim();
        String data = editTextData.getText().toString().trim();

        // Verifica se o valor não é menor ou igual a zero
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

        // Todos os campos válidos
        return true;
    }

    public void salvarReceita(View view) {
        // Formatando o valor
        Double valorRecuperado = formatandoValor(editTextValor);

        // Instanciando a classe movimentacao
        movimentacao = new Movimentacao();

        // Aplicando os valores ao objeto movimentacao
        movimentacao.setValor(valorRecuperado);
        movimentacao.setTitulo(editTextTitulo.getText().toString());
        movimentacao.setDescricao(editTextDescricao.getText().toString());
        movimentacao.setCategoria(editTextCategoria.getText().toString());
        movimentacao.setData(editTextData.getText().toString());
        movimentacao.setTipo("D");

        // chamando método salvar da classe movimentacao
        movimentacao.salvar();
    }

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

        // Coloca o foco no primeiro campo
        editTextTitulo.requestFocus();
    }
}