package com.tcc.tccvioleta.Classes;

import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.Exclude;
import com.tcc.tccvioleta.Helpers.ConfiguracaoFireBase;

public class Usuario {
    private String id;
    private String nome;
    private String email;
    private String senha;
    private String telefone;

    public Usuario() {

    }

    public Usuario(String nome, String email, String senha) {
        this.nome = nome;
        this.email = email;
        this.senha = senha;
    }

    public void salvarDados() {
        DatabaseReference firebase = ConfiguracaoFireBase.getFirebaseDatabase();
        firebase.child("Usuarios").child(id).setValue(this);
    }

    @Exclude
    public String getId() {
        return id;
    }

    public String getNome() {
        return nome;
    }

    public void setNome(String nome) {
        this.nome = nome;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public void setSenha(String senha) {
        this.senha = senha;
    }

    @Exclude
    public String getSenha() {
        return senha;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getTelefone() {
        return telefone;
    }

    public void setTelefone(String telefone) {
        this.telefone = telefone;
    }
}