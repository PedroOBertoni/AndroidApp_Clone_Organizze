package com.aula.organizze.activity;

import android.annotation.SuppressLint;
import android.app.DatePickerDialog;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.text.InputType;
import android.view.View;
import android.widget.Toast;
import android.widget.EditText;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.aula.organizze.R;
import com.aula.organizze.model.Movimentacao;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.textfield.TextInputEditText;

import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;

public class DespesasActivity extends AppCompatActivity {

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
        setContentView(R.layout.activity_despesas);

        // Vincula os elementos do layout
        editTextTitulo = findViewById(R.id.editTextTituloDespesa);
        editTextDescricao = findViewById(R.id.editTextDescricaoDespesa);
        editTextCategoria = findViewById(R.id.editTextCategoriaDespesa);
        editTextData = findViewById(R.id.editTextDataDespesa);
        fabCalendario = findViewById(R.id.fabCalendarioDespesa);
        fabConfirmar = findViewById(R.id.floatingActionButtonConfirmarDespesa);
        editTextValor = findViewById(R.id.editTextValorDespesa);

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
            String[] categorias = {"Assinaturas e Serviços", "Compras", "Alimentação", "Transporte", "Lazer", "Outros"};
            new MaterialAlertDialogBuilder(this, R.style.RoundedDialogDespesa)
                    .setTitle("Selecione uma categoria")
                    .setItems(categorias, (dialog, which) -> editTextCategoria.setText(categorias[which]))
                    .show();
        });
    }

    // Helper para setar "R$ 0,00" ou qualquer centavos em string de dígitos (ex: "0" ou "12")
    private void digitacaoContinua(String digitsOnly) {
        try {
            long cents = Long.parseLong(digitsOnly);
            double valor = cents / 100.0;
            String formatted = NumberFormat.getCurrencyInstance(locale).format(valor);
            editTextValor.setText(formatted);
            editTextValor.setSelection(formatted.length());
        } catch (NumberFormatException e) {
            editTextValor.setText(NumberFormat.getCurrencyInstance(locale).format(0.0));
            editTextValor.setSelection(editTextValor.getText().length());
        }
    }

    public void salvarDespesa(View view){
        // Formatando o valor
        String valorRecuperado = editTextValor.getText().toString()
                .replace("R$", "")
                .replaceAll("\\s", "")
                .replaceAll("\\.", "")
                .replace(",", ".")
                .replaceAll("[\\u00A0\\s]", "") // remove espaços normais e não quebráveis
                .trim();

        // Instanciando a classe movimentacao
        movimentacao = new Movimentacao();

        // Aplicando os valores ao objeto movimentacao
        movimentacao.setValor( Double.parseDouble(valorRecuperado));
        movimentacao.setTitulo( editTextTitulo.getText().toString());
        movimentacao.setDescricao( editTextDescricao.getText().toString());
        movimentacao.setCategoria( editTextCategoria.getText().toString());
        movimentacao.setData( editTextData.getText().toString());
        movimentacao.setTipo( "D" );

        // chamando método salvar da classe movimentacao
        movimentacao.salvar();
    }
}
