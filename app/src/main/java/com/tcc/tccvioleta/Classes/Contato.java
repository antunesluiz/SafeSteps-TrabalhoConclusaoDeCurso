package com.tcc.tccvioleta.Classes;

import com.google.firebase.database.DatabaseReference;
import com.tcc.tccvioleta.Helpers.ConfiguracaoFireBase;

public class Contato {
    private String nome;
    private String telefone;

    public Contato(String nome, String telefone) {
        this.nome = nome;
        this.telefone = telefone;
    }

    public Contato() {
    }

    public String getNome() {
        return nome;
    }

    public void setNome(String nome) {
        this.nome = nome;
    }

    public String getTelefone() {
        return telefone;
    }

    public void setTelefone(String telefone) {
        this.telefone = telefone;
    }

    public void salvarDados(String id) {
        DatabaseReference firebase = ConfiguracaoFireBase.getFirebaseDatabase();
        firebase.child("Usuarios").child(id).child("Contatos").push().setValue(this);
    }
}
