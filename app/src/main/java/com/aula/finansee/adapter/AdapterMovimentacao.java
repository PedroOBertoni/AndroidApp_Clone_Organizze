package com.aula.finansee.adapter;

import android.annotation.SuppressLint;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.aula.finansee.R;
import com.aula.finansee.activity.PrincipalActivity;
import com.aula.finansee.model.Movimentacao;
import com.aula.finansee.model.Recorrencia;

import org.threeten.bp.LocalDate;
import org.threeten.bp.format.DateTimeFormatter;
import org.threeten.bp.format.DateTimeParseException;

import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.Locale;

public class AdapterMovimentacao extends RecyclerView.Adapter<AdapterMovimentacao.MyViewHolder> {

    // Lista de movimentações e contexto
    private final ArrayList<Movimentacao> movimentacoes;
    private final Context context;

    // Construtor
    public AdapterMovimentacao(ArrayList<Movimentacao> movimentacoes, Context context) {
        this.movimentacoes = movimentacoes;
        this.context = context;
    }

    @NonNull
    @Override
    public MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        // Infla o layout do item da lista
        View itemLista = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.adapter_movimentacao, parent, false);

        // Retorna o ViewHolder com o layout inflado
        return new MyViewHolder(itemLista);
    }

    @SuppressLint("SetTextI18n")
    @Override
    public void onBindViewHolder(@NonNull MyViewHolder holder, int position) {
        // Instancia um objeto movimentação com base na posição
        Movimentacao movimentacao = movimentacoes.get(position);

        /* Exibição dos Dados */

        // Título
        holder.titulo.setText(movimentacao.getTitulo());
        holder.titulo.setTextColor(ContextCompat.getColor(context, R.color.textPrimary));

        // Categoria
        holder.categoria.setText(movimentacao.getCategoria());

        // Data formatada (usando ThreeTenABP)
        try {
            LocalDate data = LocalDate.parse(movimentacao.getData(), DateTimeFormatter.ISO_LOCAL_DATE);
            String dataFormatada = data.format(DateTimeFormatter.ofPattern("dd/MM/yyyy"));
            holder.data.setText(dataFormatada);
        } catch (DateTimeParseException e) {
            holder.data.setText(movimentacao.getData());
        }

        // Valor formatado (padrão BR)
        NumberFormat formatoBR = NumberFormat.getCurrencyInstance(new Locale("pt", "BR"));
        holder.valor.setText(formatoBR.format(movimentacao.getValor()));

        // Tipo (Receita ou Despesa)
        if (movimentacao.getTipo().equalsIgnoreCase("D")) {
            holder.valor.setTextColor(ContextCompat.getColor(context, R.color.colorPrimaryDespesa));
            holder.prefixo.setText("-");
            holder.prefixo.setTextColor(ContextCompat.getColor(context, R.color.colorPrimaryDespesa));
        } else if (movimentacao.getTipo().equalsIgnoreCase("R")) {
            holder.valor.setTextColor(ContextCompat.getColor(context, R.color.colorPrimaryReceita));
            holder.prefixo.setText("+");
            holder.prefixo.setTextColor(ContextCompat.getColor(context, R.color.colorPrimaryReceita));
        } else {
            holder.valor.setTextColor(ContextCompat.getColor(context, R.color.textPrimary));
            holder.prefixo.setText("");
        }

        // Recorrência
        Recorrencia rec = movimentacao.getRecorrencia();
        if (rec != null && rec.getTipo() != null) {
            if ("parcelada".equalsIgnoreCase(rec.getTipo())) {
                Integer atual = rec.getParcelaAtual();
                Integer total = rec.getParcelasTotais();
                if (atual != null && total != null) {
                    holder.textQuantParcelas.setText(atual + "/" + total);
                    holder.textQuantParcelas.setVisibility(View.VISIBLE);
                } else {
                    holder.textQuantParcelas.setVisibility(View.GONE);
                }
            } else if ("fixa".equalsIgnoreCase(rec.getTipo())) {
                holder.textQuantParcelas.setText("Fixo");
                holder.textQuantParcelas.setVisibility(View.VISIBLE);
            } else {
                holder.textQuantParcelas.setVisibility(View.GONE);
            }
        } else {
            holder.textQuantParcelas.setVisibility(View.GONE);
        }

        /* onClickListener */

        holder.itemView.setOnClickListener(v -> {
            if (context instanceof PrincipalActivity) {
                ((PrincipalActivity) context).exibirDialogEditarOuExcluir(movimentacao, holder.getAdapterPosition());
            }
        });

    }

    @Override
    public int getItemCount() {
        // Retorna o tamanho da lista de movimentações
        return movimentacoes.size();
    }

    public static class MyViewHolder extends RecyclerView.ViewHolder {

        // Componentes do layout
        TextView titulo, categoria, data, valor, prefixo, infoExtra, textQuantParcelas;

        // Construtor
        public MyViewHolder(@NonNull View itemView) {
            // Inicializa os componentes
            super(itemView);

            // Referências dos componentes
            titulo = itemView.findViewById(R.id.textTitulo);
            categoria = itemView.findViewById(R.id.textCategoria);
            data = itemView.findViewById(R.id.textData);
            valor = itemView.findViewById(R.id.textValor);
            prefixo = itemView.findViewById(R.id.textPrefixo);
            infoExtra = itemView.findViewById(R.id.textInfoExtra);
            textQuantParcelas = itemView.findViewById(R.id.textQuantParcelas);
        }
    }
}
