package com.aula.organizze.adapter;

import android.annotation.SuppressLint;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.aula.organizze.R;
import com.aula.organizze.model.Movimentacao;

import java.util.ArrayList;

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
        Movimentacao movimentacao = movimentacoes.get(position);

        // Título — usa sempre a cor textPrimary do tema
        holder.titulo.setText(movimentacao.getTitulo());
        holder.titulo.setTextColor(ContextCompat.getColor(context, R.color.textPrimary));

        // Categoria e Data
        holder.categoria.setText(movimentacao.getCategoria());
        holder.data.setText(movimentacao.getData());

        // Valor formatado
        holder.valor.setText(String.format("R$ %.2f", movimentacao.getValor()));

        // Define cor e prefixo de acordo com o tipo
        if (movimentacao.getTipo().equalsIgnoreCase("D")) {
            // Despesa
            holder.valor.setTextColor(ContextCompat.getColor(context, R.color.colorPrimaryDespesa));
            holder.prefixo.setText("-");
            holder.prefixo.setTextColor(ContextCompat.getColor(context, R.color.colorPrimaryDespesa));
        } else if (movimentacao.getTipo().equalsIgnoreCase("R")) {
            // Receita
            holder.valor.setTextColor(ContextCompat.getColor(context, R.color.colorPrimary));
            holder.prefixo.setText("+");
            holder.prefixo.setTextColor(ContextCompat.getColor(context, R.color.colorPrimary));
        } else {
            // Tipo indefinido — fallback
            holder.valor.setTextColor(ContextCompat.getColor(context, R.color.textPrimary));
            holder.prefixo.setText("");
        }
    }

    @Override
    public int getItemCount() {
        // Retorna o tamanho da lista de movimentações
        return movimentacoes.size();
    }

    public static class MyViewHolder extends RecyclerView.ViewHolder {

        // Componentes do layout
        TextView titulo, categoria, data, valor, prefixo, infoExtra;

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
        }
    }
}
