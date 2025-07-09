package com.jorge.meustitulos.model;

public class Usuario {

    private int id;
    private String nomeCompleto;
    private String email;
    private String nomeUsuario;
    private String senha;
    private String fotoPerfilUri;

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getNomeCompleto() {
        return nomeCompleto;
    }

    public void setNomeCompleto(String nomeCompleto) {
        this.nomeCompleto = nomeCompleto;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getNomeUsuario() {
        return nomeUsuario;
    }

    public void setNomeUsuario(String nomeUsuario) {
        this.nomeUsuario = nomeUsuario;
    }

    public String getSenha() {
        return senha;
    }

    public void setSenha(String senha) {
        this.senha = senha;
    }

    public String getFotoPerfilUri() {
        return fotoPerfilUri;
    }

    public void setFotoPerfilUri(String fotoPerfilUri) {
        this.fotoPerfilUri = fotoPerfilUri;
    }
}
