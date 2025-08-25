package com.jorge.meustitulos.banco_de_dados;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import com.jorge.meustitulos.model.Titulo;
import com.jorge.meustitulos.model.Usuario;

public class MeusTitulosDB extends SQLiteOpenHelper {

    private static final String DB_NAME = "meustitulo.db";
    private static final int DB_VERSION = 5;

    private static final String TABLE_TITULOS = "Titulos";
    private static final String TABLE_USUARIOS = "Usuarios";

    private static final String KEY_ID = "id";

    private static final String KEY_TITULO = "titulo";
    private static final String KEY_TIPO = "tipo";
    private static final String KEY_GENERO = "genero";
    private static final String KEY_NOTA = "nota";
    private static final String KEY_STATUS = "status";
    private static final String KEY_COMENTARIO = "comentario";
    private static final String KEY_IMAGEM_URI = "imagemUri";
    private static final String KEY_USER_ID = "userId";

    private static final String KEY_NOME_COMPLETO = "nomeCompleto";
    private static final String KEY_EMAIL = "email";
    private static final String KEY_NOME_USUARIO = "nomeUsuario";
    private static final String KEY_SENHA = "senha";
    private static final String KEY_FOTO_PERFIL_URI = "fotoPerfilUri";


    public MeusTitulosDB(Context context) {
        super(context, DB_NAME, null, DB_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase sqLiteDatabase) {
        String CREATE_TITULOS_TABLE = "CREATE TABLE IF NOT EXISTS " + TABLE_TITULOS + "("
                + KEY_ID + " INTEGER PRIMARY KEY AUTOINCREMENT,"
                + KEY_TITULO + " TEXT,"
                + KEY_TIPO + " TEXT,"
                + KEY_GENERO + " TEXT,"
                + KEY_NOTA + " REAL,"
                + KEY_STATUS + " TEXT,"
                + KEY_COMENTARIO + " TEXT,"
                + KEY_IMAGEM_URI + " TEXT,"
                + KEY_USER_ID + " INTEGER" + ")";
        sqLiteDatabase.execSQL(CREATE_TITULOS_TABLE);

        String CREATE_USUARIOS_TABLE = "CREATE TABLE IF NOT EXISTS " + TABLE_USUARIOS + "("
                + KEY_ID + " INTEGER PRIMARY KEY AUTOINCREMENT,"
                + KEY_NOME_COMPLETO + " TEXT,"
                + KEY_EMAIL + " TEXT,"
                + KEY_NOME_USUARIO + " TEXT,"
                + KEY_SENHA + " TEXT,"
                + KEY_FOTO_PERFIL_URI + " TEXT" + ")";
        sqLiteDatabase.execSQL(CREATE_USUARIOS_TABLE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        // Lógica de upgrade para adicionar novas colunas sem perder dados
        if (oldVersion < newVersion) {
            if (oldVersion < 2) { // Adiciona 'genero' se a versão for menor que 2
                db.execSQL("ALTER TABLE " + TABLE_TITULOS + " ADD COLUMN " + KEY_GENERO + " TEXT DEFAULT ''");
            }
            if (oldVersion < 3) { // Adiciona 'imagemUri' se a versão for menor que 3
                db.execSQL("ALTER TABLE " + TABLE_TITULOS + " ADD COLUMN " + KEY_IMAGEM_URI + " TEXT DEFAULT ''");
            }
            if (oldVersion < 4) { // Adiciona 'fotoPerfilUri' se a versão for menor que 4
                db.execSQL("ALTER TABLE " + TABLE_USUARIOS + " ADD COLUMN " + KEY_FOTO_PERFIL_URI + " TEXT DEFAULT ''");
            }
            if (oldVersion < 5) { // Adiciona 'userId' se a versão for menor que 5
                db.execSQL("ALTER TABLE " + TABLE_TITULOS + " ADD COLUMN " + KEY_USER_ID + " INTEGER DEFAULT -1");
            }
        } else {
            // Se a versão antiga for maior que a nova, recria as tabelas (perde dados)
            db.execSQL("DROP TABLE IF EXISTS " + TABLE_TITULOS);
            db.execSQL("DROP TABLE IF EXISTS " + TABLE_USUARIOS);
            onCreate(db);
        }
    }

    public long salvarObjeto(String tabela, ContentValues dados){
        SQLiteDatabase db = this.getWritableDatabase();
        long id = db.insert(tabela, null, dados);
        db.close();
        return id;
    }

    // Remover título por ID e USER_ID
    public int removerTitulo(int id, int userId) {
        SQLiteDatabase db = this.getWritableDatabase();
        int rowsAffected = db.delete(TABLE_TITULOS, KEY_ID + " = ? AND " + KEY_USER_ID + " = ?", new String[]{String.valueOf(id), String.valueOf(userId)});
        db.close();
        return rowsAffected;
    }

    // Atualizar status do título por ID e USER_ID
    public int atualizarTituloStatus(int id, String novoStatus, int userId) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(KEY_STATUS, novoStatus);

        int rowsAffected = db.update(TABLE_TITULOS, values, KEY_ID + " = ? AND " + KEY_USER_ID + " = ?", new String[]{String.valueOf(id), String.valueOf(userId)});
        db.close();
        return rowsAffected;
    }

    // Atualizar título completo por ID e USER_ID
    public int atualizarTitulo(Titulo titulo) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(KEY_TITULO, titulo.getTitulo());
        values.put(KEY_TIPO, titulo.getTipo());
        values.put(KEY_GENERO, titulo.getGenero());
        values.put(KEY_NOTA, titulo.getNota());
        values.put(KEY_STATUS, titulo.getStatus());
        values.put(KEY_COMENTARIO, titulo.getComentario());
        values.put(KEY_IMAGEM_URI, titulo.getImagemUri());
        values.put(KEY_USER_ID, titulo.getUserId()); // Garante que o userId seja atualizado também

        int rowsAffected = db.update(TABLE_TITULOS, values, KEY_ID + " = ? AND " + KEY_USER_ID + " = ?", new String[]{String.valueOf(titulo.getId()), String.valueOf(titulo.getUserId())});
        db.close();
        return rowsAffected;
    }

    public int atualizarUsuario(Usuario usuario) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(KEY_NOME_COMPLETO, usuario.getNomeCompleto());
        values.put(KEY_EMAIL, usuario.getEmail());
        values.put(KEY_NOME_USUARIO, usuario.getNomeUsuario());
        values.put(KEY_SENHA, usuario.getSenha());
        values.put(KEY_FOTO_PERFIL_URI, usuario.getFotoPerfilUri());

        int rowsAffected = db.update(TABLE_USUARIOS, values, KEY_ID + " = ?", new String[]{String.valueOf(usuario.getId())});
        db.close();
        return rowsAffected;
    }

    public int removerUsuario(int id) {
        SQLiteDatabase db = this.getWritableDatabase();
        int rowsAffected = db.delete(TABLE_USUARIOS, KEY_ID + " = ?", new String[]{String.valueOf(id)});
        db.close();
        return rowsAffected;
    }

    // Buscar título por ID (não precisa de userId aqui, pois o ID já é único)
    public Cursor buscarTituloPorId(int id) {
        SQLiteDatabase db = this.getReadableDatabase();
        return db.rawQuery("SELECT * FROM " + TABLE_TITULOS + " WHERE " + KEY_ID + " = ?", new String[]{String.valueOf(id)});
    }

    public Cursor buscarUsuarioPorNomeUsuario(String nomeUsuario) {
        SQLiteDatabase db = this.getReadableDatabase();
        return db.rawQuery("SELECT * FROM " + TABLE_USUARIOS + " WHERE " + KEY_NOME_USUARIO + " = ?", new String[]{nomeUsuario});
    }

    // Buscar títulos filtrados por userId
    public Cursor buscarTitulosPorUsuario(int userId) {
        SQLiteDatabase db = this.getReadableDatabase();
        return db.rawQuery("SELECT * FROM " + TABLE_TITULOS + " WHERE " + KEY_USER_ID + " = ?", new String[]{String.valueOf(userId)});
    }

    // Método buscarTitulos()
    public Cursor buscarTitulos() {
        SQLiteDatabase db = this.getReadableDatabase();
        return db.rawQuery("SELECT * FROM " + TABLE_TITULOS, null);
    }

    public Cursor buscarUsuarios() {
        SQLiteDatabase db = this.getReadableDatabase();
        return db.rawQuery("SELECT * FROM " + TABLE_USUARIOS, null);
    }

    public Cursor autenticarUsuario(String nomeUsuario, String senha) {
        SQLiteDatabase db = this.getReadableDatabase();
        return db.rawQuery("SELECT * FROM " + TABLE_USUARIOS + " WHERE " + KEY_NOME_USUARIO + " = ? AND " + KEY_SENHA + " = ?",
                new String[]{nomeUsuario, senha});
    }
}