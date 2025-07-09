package com.jorge.meustitulos.view;

import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.jorge.meustitulos.R;
import com.jorge.meustitulos.model.Titulo;

import java.util.List;

public class TituloAdapter extends RecyclerView.Adapter<TituloAdapter.TituloViewHolder> {

    private List<Titulo> titulosList;
    private OnItemClickListener listener;

    public interface OnItemClickListener {
        void onItemClick(int position);
        void onItemLongClick(int position);
    }

    public void setOnItemClickListener(OnItemClickListener listener) {
        this.listener = listener;
    }

    public TituloAdapter(List<Titulo> titulosList) {
        this.titulosList = titulosList;
    }

    public void setTitulos(List<Titulo> newTitulos) {
        this.titulosList = newTitulos;
        notifyDataSetChanged();
    }

    public void removerItem(int position) {
        titulosList.remove(position);
        notifyItemRemoved(position);
        notifyItemRangeChanged(position, titulosList.size());
    }

    @NonNull
    @Override
    public TituloViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_titulo, parent, false);
        return new TituloViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull TituloViewHolder holder, int position) {
        Titulo titulo = titulosList.get(position);

        holder.textViewTitulo.setText(titulo.getTitulo());
        holder.textViewTipo.setText("Tipo: " + titulo.getTipo());
        holder.textViewGenero.setText("Gênero: " + titulo.getGenero());
        holder.textViewNota.setText("Nota: " + String.format("%.1f", titulo.getNota()));
        holder.textViewStatus.setText("Status: " + titulo.getStatus());
        holder.textViewComentario.setText("Comentário: " + titulo.getComentario());

        if (titulo.getImagemUri() != null && !titulo.getImagemUri().isEmpty()) {
            try {
                holder.imageViewCapa.setImageURI(Uri.parse(titulo.getImagemUri()));
            } catch (Exception e) {
                holder.imageViewCapa.setImageResource(R.drawable.placeholder_capa);
            }
        } else {
            holder.imageViewCapa.setImageResource(R.drawable.placeholder_capa);
        }

        holder.itemView.setOnClickListener(v -> {
            if (listener != null) {
                listener.onItemClick(position);
            }
        });

        holder.itemView.setOnLongClickListener(v -> {
            if (listener != null) {
                listener.onItemLongClick(position);
            }
            return true;
        });
    }

    @Override
    public int getItemCount() {
        return titulosList.size();
    }

    public static class TituloViewHolder extends RecyclerView.ViewHolder {
        TextView textViewTitulo, textViewTipo, textViewGenero, textViewNota, textViewStatus, textViewComentario;
        ImageView imageViewCapa;

        public TituloViewHolder(@NonNull View itemView) {
            super(itemView);
            textViewTitulo = itemView.findViewById(R.id.textViewTitulo);
            textViewTipo = itemView.findViewById(R.id.textViewTipo);
            textViewGenero = itemView.findViewById(R.id.textViewGenero);
            textViewNota = itemView.findViewById(R.id.textViewNota);
            textViewStatus = itemView.findViewById(R.id.textViewStatus);
            textViewComentario = itemView.findViewById(R.id.textViewComentario);
            imageViewCapa = itemView.findViewById(R.id.imageViewCapa);
        }
    }
}