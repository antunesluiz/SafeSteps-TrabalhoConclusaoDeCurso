package com.tcc.tccvioleta.Classes;

public class Ocorrencia {
    private String endereco;
    private String hora;
    private String data;
    private String tipo;

    public Ocorrencia() {

    }

    public Ocorrencia(String endereco, String hora, String data, String tipo) {
        this.endereco = endereco;
        this.hora = hora;
        this.data = data;
        this.tipo = tipo;
    }

    public String getEndereco() {
        return endereco;
    }

    public String getHora() {
        return hora;
    }

    public String getData() {
        return data;
    }

    public String getTipo() {
        return tipo;
    }
}
