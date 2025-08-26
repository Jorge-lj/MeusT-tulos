package com.jorge.meustitulos.controller;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;

import com.jorge.meustitulos.banco_de_dados.MeusTitulosDB;
import com.jorge.meustitulos.model.Titulo;

import java.util.ArrayList;
import java.util.List;

public class TitulosController {

    private final MeusTitulosDB db;
    private final Context context;

    public TitulosController(Context context) {
        this.context = context;
        this.db = new MeusTitulosDB(context);
    }

    public long salvarTitulo(Titulo titulo){
        ContentValues dados = new ContentValues();
        dados.put("titulo", titulo.getTitulo());
        dados.put("tipo", titulo.getTipo());
        dados.put("genero", titulo.getGenero());
        dados.put("nota", titulo.getNota());
        dados.put("status", titulo.getStatus());
        dados.put("comentario", titulo.getComentario());
        dados.put("imagemUri", titulo.getImagemUri());
        dados.put("userId", titulo.getUserId()); // Salva o userId do título

        return db.salvarObjeto("Titulos", dados);
    }

    public boolean removerTitulo(int idTitulo, int userId) { // Adicionado userId
        int linhasAfetadas = db.removerTitulo(idTitulo, userId); // Passa userId para o DB
        return linhasAfetadas > 0;
    }

    public boolean atualizarTituloStatus(int idTitulo, String novoStatus, int userId) { // Adicionado userId
        int linhasAfetadas = db.atualizarTituloStatus(idTitulo, novoStatus, userId); // Passa userId para o DB
        return linhasAfetadas > 0;
    }

    public boolean atualizarTitulo(Titulo titulo) {
        // O userId já está no objeto Titulo, será usado na atualização do DB
        int linhasAfetadas = db.atualizarTitulo(titulo);
        return linhasAfetadas > 0;
    }

    public Titulo buscarTituloPorId(int id) {
        Cursor cursor = db.buscarTituloPorId(id);
        Titulo titulo = null;
        if (cursor != null && cursor.moveToFirst()) {
            titulo = new Titulo();
            titulo.setId(cursor.getInt(cursor.getColumnIndexOrThrow("id")));
            titulo.setTitulo(cursor.getString(cursor.getColumnIndexOrThrow("titulo")));
            titulo.setTipo(cursor.getString(cursor.getColumnIndexOrThrow("tipo")));
            titulo.setGenero(cursor.getString(cursor.getColumnIndexOrThrow("genero")));
            titulo.setNota(cursor.getDouble(cursor.getColumnIndexOrThrow("nota")));
            titulo.setStatus(cursor.getString(cursor.getColumnIndexOrThrow("status")));
            titulo.setComentario(cursor.getString(cursor.getColumnIndexOrThrow("comentario")));
            titulo.setImagemUri(cursor.getString(cursor.getColumnIndexOrThrow("imagemUri")));
            titulo.setUserId(cursor.getInt(cursor.getColumnIndexOrThrow("userId"))); // Preenche o userId
            cursor.close();
        }
        return titulo;
    }

    public List<Titulo> buscarTitulosDoUsuario(int userId) {
        List<Titulo> titulosList = new ArrayList<>();
        Cursor cursor = db.buscarTitulosPorUsuario(userId); // Chama o método do DB com userId

        if (cursor != null && cursor.moveToFirst()) {
            do {
                Titulo titulo = new Titulo();
                titulo.setId(cursor.getInt(cursor.getColumnIndexOrThrow("id")));
                titulo.setTitulo(cursor.getString(cursor.getColumnIndexOrThrow("titulo")));
                titulo.setTipo(cursor.getString(cursor.getColumnIndexOrThrow("tipo")));
                titulo.setGenero(cursor.getString(cursor.getColumnIndexOrThrow("genero")));
                titulo.setNota(cursor.getDouble(cursor.getColumnIndexOrThrow("nota")));
                titulo.setStatus(cursor.getString(cursor.getColumnIndexOrThrow("status")));
                titulo.setComentario(cursor.getString(cursor.getColumnIndexOrThrow("comentario")));
                titulo.setImagemUri(cursor.getString(cursor.getColumnIndexOrThrow("imagemUri")));
                titulo.setUserId(cursor.getInt(cursor.getColumnIndexOrThrow("userId"))); // Preenche o userId
                titulosList.add(titulo);
            } while (cursor.moveToNext());
            cursor.close();
        }
        return titulosList;
    }

    // O método buscarTodosTitulos() original pode ser mantido ou removido se não for mais usado
    public List<Titulo> buscarTodosTitulos() {
        List<Titulo> titulosList = new ArrayList<>();
        Cursor cursor = db.buscarTitulos();

        if (cursor != null && cursor.moveToFirst()) {
            do {
                Titulo titulo = new Titulo();
                titulo.setId(cursor.getInt(cursor.getColumnIndexOrThrow("id")));
                titulo.setTitulo(cursor.getString(cursor.getColumnIndexOrThrow("titulo")));
                titulo.setTipo(cursor.getString(cursor.getColumnIndexOrThrow("tipo")));
                titulo.setGenero(cursor.getString(cursor.getColumnIndexOrThrow("genero")));
                titulo.setNota(cursor.getDouble(cursor.getColumnIndexOrThrow("nota")));
                titulo.setStatus(cursor.getString(cursor.getColumnIndexOrThrow("status")));
                titulo.setComentario(cursor.getString(cursor.getColumnIndexOrThrow("comentario")));
                titulo.setImagemUri(cursor.getString(cursor.getColumnIndexOrThrow("imagemUri")));
                titulo.setUserId(cursor.getInt(cursor.getColumnIndexOrThrow("userId"))); // Preenche o userId
                titulosList.add(titulo);
            } while (cursor.moveToNext());
            cursor.close();
        }
        return titulosList;
    }
}
